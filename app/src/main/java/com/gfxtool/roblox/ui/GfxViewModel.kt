package com.gfxtool.roblox.ui

import android.app.Application
import android.os.Build
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gfxtool.roblox.data.model.*
import com.gfxtool.roblox.data.repository.ApplyResult
import com.gfxtool.roblox.data.repository.GfxRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UiState(
    val settings: GfxSettings       = GfxSettings(),
    val presets: List<UserPreset>    = emptyList(),
    val applyMode: ApplyMode         = ApplyMode.FILE_CONFIG,
    val isRootAvailable: Boolean     = false,
    val isRobloxRunning: Boolean     = false,
    val isApplying: Boolean          = false,
    val snackMessage: String?        = null,
    val exportedFilePath: String?    = null,
    val hasStoragePermission: Boolean = true,
)

class GfxViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = GfxRepository(app)

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.settingsFlow,
                repo.presetsFlow,
                repo.applyModeFlow,
            ) { settings, presets, mode -> Triple(settings, presets, mode) }
            .collect { (settings, presets, mode) ->
                _ui.update { it.copy(settings = settings, presets = presets, applyMode = mode) }
            }
        }
        checkRoot()
        checkStoragePermission()
        pollRobloxStatus()
    }

    // ── Settings mutations ────────────────────────────────────────

    fun updateSettings(block: GfxSettings.() -> GfxSettings) {
        viewModelScope.launch {
            val updated = _ui.value.settings.block()
            _ui.update { it.copy(settings = updated) }
            repo.saveSettings(updated)
        }
    }

    fun applyPreset(preset: GfxSettings) = updateSettings { preset }

    // ── Apply config ──────────────────────────────────────────────

    fun applyConfig() {
        viewModelScope.launch {
            _ui.update { it.copy(isApplying = true) }

            // Cảnh báo nếu Roblox đang mở
            val warningPrefix = if (_ui.value.isRobloxRunning)
                "⚠️ Roblox đang chạy — khởi động lại game để áp dụng!\n" else ""

            val result = repo.applyConfig(_ui.value.settings, _ui.value.applyMode)

            _ui.update {
                it.copy(
                    isApplying   = false,
                    snackMessage = warningPrefix + when (result) {
                        is ApplyResult.Success       -> "✅ Đã ghi config!\n${result.path}"
                        is ApplyResult.Error         -> "❌ ${result.message}"
                        ApplyResult.RootNotAvailable -> "⚠️ Root không khả dụng. Vui lòng cấp quyền SU."
                    }
                )
            }
        }
    }

    // ── Export / Share ────────────────────────────────────────────

    fun exportConfig() {
        viewModelScope.launch {
            val result = repo.exportToDownloads(_ui.value.settings)
            _ui.update {
                it.copy(
                    snackMessage     = when (result) {
                        is ApplyResult.Success -> "📁 Đã xuất: ${result.path}"
                        is ApplyResult.Error   -> "❌ ${result.message}"
                        else                   -> null
                    },
                    exportedFilePath = if (result is ApplyResult.Success) result.path else null,
                )
            }
        }
    }

    fun clearExportedPath() = _ui.update { it.copy(exportedFilePath = null) }

    // ── Presets ───────────────────────────────────────────────────

    fun savePreset(name: String) {
        viewModelScope.launch {
            repo.savePreset(name, _ui.value.settings)
            _ui.update { it.copy(snackMessage = "💾 Đã lưu preset \"$name\"") }
        }
    }

    fun deletePreset(id: String) {
        viewModelScope.launch { repo.deletePreset(id) }
    }

    // ── Apply mode ────────────────────────────────────────────────

    fun setApplyMode(mode: ApplyMode) {
        viewModelScope.launch { repo.setApplyMode(mode) }
    }

    // ── Misc ──────────────────────────────────────────────────────

    fun clearSnack() = _ui.update { it.copy(snackMessage = null) }

    fun checkStoragePermission() {
        val has = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            Environment.isExternalStorageManager()
        else true
        _ui.update { it.copy(hasStoragePermission = has) }
    }

    private fun checkRoot() {
        viewModelScope.launch {
            _ui.update { it.copy(isRootAvailable = repo.isRootAvailable()) }
        }
    }

    /**
     * Poll mỗi 5s kiểm tra Roblox có đang chạy không (chỉ khi root khả dụng).
     */
    private fun pollRobloxStatus() {
        viewModelScope.launch {
            while (true) {
                if (_ui.value.isRootAvailable) {
                    _ui.update { it.copy(isRobloxRunning = repo.isRobloxRunning()) }
                }
                delay(5_000)
            }
        }
    }
}