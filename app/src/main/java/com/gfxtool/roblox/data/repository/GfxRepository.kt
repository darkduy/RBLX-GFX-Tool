package com.gfxtool.roblox.data.repository

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gfxtool.roblox.data.model.*
import com.gfxtool.roblox.domain.RobloxDetector
import com.gfxtool.roblox.domain.RootCommandRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

private val Context.dataStore: DataStore<Preferences>
    by preferencesDataStore(name = "gfx_settings")

sealed class ApplyResult {
    data class Success(val path: String) : ApplyResult()
    data class Error(val message: String) : ApplyResult()
    object RootNotAvailable : ApplyResult()
}

class GfxRepository(private val context: Context) {

    private val gson = Gson()

    private object Keys {
        val SETTINGS_JSON = stringPreferencesKey("settings_json")
        val PRESETS_JSON  = stringPreferencesKey("presets_json")
        val APPLY_MODE    = stringPreferencesKey("apply_mode")
    }

    private val robloxPackages = listOf(
        "com.roblox.client",
        "com.roblox.client2",
    )

    private val configFileName = "ClientAppSettings.json"

    // ── Settings ──────────────────────────────────────────────────

    val settingsFlow: Flow<GfxSettings> = context.dataStore.data.map { prefs ->
        val json = prefs[Keys.SETTINGS_JSON]
        if (json != null) {
            try { gson.fromJson(json, GfxSettings::class.java) }
            catch (e: Exception) { GfxSettings() }
        } else GfxSettings()
    }

    suspend fun saveSettings(settings: GfxSettings) {
        context.dataStore.edit { it[Keys.SETTINGS_JSON] = gson.toJson(settings) }
    }

    // ── Presets ───────────────────────────────────────────────────

    val presetsFlow: Flow<List<UserPreset>> = context.dataStore.data.map { prefs ->
        val json = prefs[Keys.PRESETS_JSON] ?: return@map emptyList()
        try {
            val type = object : TypeToken<List<UserPreset>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun savePreset(name: String, settings: GfxSettings) {
        val current = presetsFlow.first().toMutableList()
        current.add(UserPreset(UUID.randomUUID().toString(), name, settings))
        context.dataStore.edit { it[Keys.PRESETS_JSON] = gson.toJson(current) }
    }

    suspend fun deletePreset(id: String) {
        val current = presetsFlow.first().filter { it.id != id }
        context.dataStore.edit { it[Keys.PRESETS_JSON] = gson.toJson(current) }
    }

    // ── Apply Mode ────────────────────────────────────────────────

    val applyModeFlow: Flow<ApplyMode> = context.dataStore.data.map { prefs ->
        try { ApplyMode.valueOf(prefs[Keys.APPLY_MODE] ?: ApplyMode.FILE_CONFIG.name) }
        catch (e: Exception) { ApplyMode.FILE_CONFIG }
    }

    suspend fun setApplyMode(mode: ApplyMode) {
        context.dataStore.edit { it[Keys.APPLY_MODE] = mode.name }
    }

    // ── Root detection ────────────────────────────────────────────

    suspend fun isRootAvailable(): Boolean = RootCommandRunner.isAvailable()
    suspend fun isRobloxRunning(): Boolean = RootCommandRunner.isRobloxRunning()

    // ── Apply config ──────────────────────────────────────────────

    suspend fun applyConfig(
        settings: GfxSettings,
        mode: ApplyMode,
    ): ApplyResult = withContext(Dispatchers.IO) {
        val json = RobloxConfigMapper.toJson(settings)
        when (mode) {
            ApplyMode.FILE_CONFIG -> applyViaFile(json)
            ApplyMode.ROOT_SHELL  -> applyViaRoot(json)
        }
    }

    /**
     * Ghi config file vào thư mục Roblox.
     *
     * Android 11+ chặn hoàn toàn truy cập Android/data qua File API thông thường
     * trừ khi đã cấp quyền MANAGE_EXTERNAL_STORAGE (Quản lý tất cả file).
     * Nếu chưa cấp quyền, trả lỗi kèm hướng dẫn — người dùng nên chuyển sang
     * chế độ ROOT nếu thiết bị đã root.
     */
    private fun applyViaFile(json: String): ApplyResult {
        val errors = mutableListOf<String>()

        // ── Thử 1: MANAGE_EXTERNAL_STORAGE (Android 11+) hoặc Android 10 direct ──
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
            Environment.isExternalStorageManager()
        ) {
            val external = Environment.getExternalStorageDirectory()
            for (pkg in robloxPackages) {
                val candidates = listOf(
                    File(external, "Android/data/$pkg/files/$configFileName"),
                    File(external, "Android/data/$pkg/files/LocalStorage/$configFileName"),
                )
                for (file in candidates) {
                    try {
                        file.parentFile?.mkdirs()
                        file.writeText(json, Charsets.UTF_8)
                        return ApplyResult.Success(file.absolutePath)
                    } catch (e: Exception) {
                        errors.add("${file.path}: ${e.message}")
                    }
                }
            }
        } else {
            errors.add("Chưa cấp quyền MANAGE_EXTERNAL_STORAGE (Quản lý tất cả file)")
        }

        // ── Kết quả: thất bại, hướng dẫn rõ ràng ───────────────────
        val detail = errors.joinToString("\n")
        return ApplyResult.Error(
            buildString {
                append("Không thể ghi file config.\n\n")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    !Environment.isExternalStorageManager()
                ) {
                    append("Cần cấp quyền:\n")
                    append("Cài đặt → Ứng dụng → RBLX GFX Tool\n")
                    append("→ Quyền → Quản lý tất cả file → Bật\n\n")
                }
                append("Hoặc chuyển sang chế độ ROOT nếu thiết bị đã root.\n")
                if (detail.isNotBlank()) append("\nChi tiết: $detail")
            }
        )
    }

    private suspend fun applyViaRoot(json: String): ApplyResult {
        if (!RootCommandRunner.isAvailable()) return ApplyResult.RootNotAvailable

        val packages = RobloxDetector.detect(context)
        if (packages.isEmpty()) {
            return ApplyResult.Error("Không tìm thấy Roblox trên thiết bị")
        }

        var lastError = "Ghi file thất bại"
        var successPath: String? = null

        for (info in packages) {
            for (path in RobloxDetector.configPaths(info.packageName)) {
                val result = RootCommandRunner.writeFile(path, json)
                if (result.isSuccess) {
                    successPath = path
                    break
                } else {
                    lastError = result.stderr.ifBlank { "exit ${result.exitCode}" }
                }
            }
            if (successPath != null) break
        }

        return if (successPath != null) ApplyResult.Success(successPath)
        else ApplyResult.Error("Root error: $lastError")
    }

    suspend fun exportToDownloads(settings: GfxSettings): ApplyResult = withContext(Dispatchers.IO) {
        try {
            val dir  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(dir, "RobloxGFX_Backup_${System.currentTimeMillis()}.json")
            file.writeText(RobloxConfigMapper.toJson(settings), Charsets.UTF_8)
            ApplyResult.Success(file.absolutePath)
        } catch (e: Exception) {
            ApplyResult.Error("Xuất thất bại: ${e.message}")
        }
    }
}