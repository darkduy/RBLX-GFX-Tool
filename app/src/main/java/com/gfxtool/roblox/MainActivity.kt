package com.gfxtool.roblox

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.gfxtool.roblox.service.OverlayService
import com.gfxtool.roblox.ui.AppNavHost
import com.gfxtool.roblox.ui.GfxViewModel
import com.gfxtool.roblox.ui.theme.BackgroundDark
import com.gfxtool.roblox.ui.theme.GfxToolTheme

class MainActivity : ComponentActivity() {

    private val vm: GfxViewModel by viewModels()

    // Permission launchers
    private val storagePermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* result handled via re-check */ }

    private val overlayPermLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* result handled via re-check */ }

    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* result handled via re-check */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNecessaryPermissions()

        setContent {
            GfxToolTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = BackgroundDark,
                ) {
                    AppNavHost(vm = vm)
                }
            }
        }
    }

    // ── Permissions ───────────────────────────────────────────────

    private fun requestNecessaryPermissions() {
        // Android 13+ notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storagePermLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        }

        // MANAGE_EXTERNAL_STORAGE (Android 11+) — required for Roblox data folder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                manageStorageLauncher.launch(
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                )
            }
        } else {
            storagePermLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            )
        }

        // Overlay permission (for floating button)
        if (!Settings.canDrawOverlays(this)) {
            overlayPermLauncher.launch(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check quyền khi user quay lại từ Settings
        vm.checkStoragePermission()
    }

    /**
     * Bật / tắt overlay service từ UI (có thể gọi sau khi user cấp quyền).
     */
    fun toggleOverlay(enable: Boolean) {
        if (enable) {
            if (Settings.canDrawOverlays(this)) {
                OverlayService.start(this)
            }
        } else {
            OverlayService.stop(this)
        }
    }
}