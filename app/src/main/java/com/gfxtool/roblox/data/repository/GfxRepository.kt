package com.gfxtool.roblox.data.repository

import android.content.Context
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

/**
 * Kết quả thao tác áp dụng config.
 */
sealed class ApplyResult {
    data class Success(val path: String)    : ApplyResult()
    data class Error(val message: String)   : ApplyResult()
    object RootNotAvailable                 : ApplyResult()
}

class GfxRepository(private val context: Context) {

    private val gson = Gson()

    // ── DataStore keys ────────────────────────────────────────────
    private object Keys {
        val SETTINGS_JSON   = stringPreferencesKey("settings_json")
        val PRESETS_JSON    = stringPreferencesKey("presets_json")
        val APPLY_MODE      = stringPreferencesKey("apply_mode")
    }

    // ── Roblox paths ──────────────────────────────────────────────
    /**
     * Các đường dẫn có thể có của Roblox trên Android.
     * Roblox thay đổi package name theo phiên bản nên cần thử nhiều path.
     */
    private val robloxPackages = listOf(
        "com.roblox.client",
        "com.roblox.client2",
    )

    private val configFileName = "ClientAppSettings.json"

    private fun getRobloxConfigPaths(): List<File> {
        val paths = mutableListOf<File>()

        // External storage approach (Android 10, scoped storage)
        val external = Environment.getExternalStorageDirectory()
        robloxPackages.forEach { pkg ->
            paths.add(File(external, "Android/data/$pkg/files/$configFileName"))
            paths.add(File(external, "Android/data/$pkg/files/LocalStorage/$configFileName"))
        }

        // Internal data (root required)
        robloxPackages.forEach { pkg ->
            paths.add(File("/data/data/$pkg/files/$configFileName"))
        }

        return paths
    }

    // ── Settings persistence ──────────────────────────────────────

    val settingsFlow: Flow<GfxSettings> = context.dataStore.data.map { prefs ->
        val json = prefs[Keys.SETTINGS_JSON]
        if (json != null) {
            try { gson.fromJson(json, GfxSettings::class.java) }
            catch (e: Exception) { GfxSettings() }
        } else GfxSettings()
    }

    suspend fun saveSettings(settings: GfxSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SETTINGS_JSON] = gson.toJson(settings)
        }
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
        current.add(UserPreset(id = UUID.randomUUID().toString(), name = name, settings = settings))
        context.dataStore.edit { prefs ->
            prefs[Keys.PRESETS_JSON] = gson.toJson(current)
        }
    }

    suspend fun deletePreset(id: String) {
        val current = presetsFlow.first().filter { it.id != id }
        context.dataStore.edit { prefs ->
            prefs[Keys.PRESETS_JSON] = gson.toJson(current)
        }
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

    /** Cảnh báo nếu Roblox đang chạy (config chỉ có hiệu lực khi restart). */
    suspend fun isRobloxRunning(): Boolean = RootCommandRunner.isRobloxRunning()

    // ── Apply config ──────────────────────────────────────────────

    /**
     * Áp dụng cài đặt bằng cách ghi file JSON vào thư mục Roblox.
     *
     * Tự động thử FILE_CONFIG trước; nếu không ghi được và root khả dụng
     * thì chuyển sang ROOT_SHELL.
     */
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

    private fun applyViaFile(json: String): ApplyResult {
        val targets = getRobloxConfigPaths()
            .filter { it.path.contains("Android/data") }   // scoped storage only

        var lastError = "Không tìm thấy thư mục cài đặt Roblox"
        var successPath: String? = null

        for (file in targets) {
            try {
                file.parentFile?.mkdirs()
                if (file.parentFile?.exists() == true) {
                    file.writeText(json, Charsets.UTF_8)
                    successPath = file.absolutePath
                }
            } catch (e: Exception) {
                lastError = e.message ?: "Lỗi không xác định"
            }
        }

        return if (successPath != null) {
            ApplyResult.Success(successPath)
        } else {
            ApplyResult.Error(
                "$lastError\n\nHãy mở Roblox ít nhất một lần để tạo thư mục, " +
                "hoặc cấp quyền MANAGE_EXTERNAL_STORAGE trong Cài đặt."
            )
        }
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

    /**
     * Xuất file JSON ra bộ nhớ Downloads để user backup hoặc chia sẻ.
     */
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
