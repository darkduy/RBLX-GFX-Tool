package com.gfxtool.roblox.service

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.view.WindowManager.LayoutParams.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.gfxtool.roblox.MainActivity
import com.gfxtool.roblox.R
import com.gfxtool.roblox.ui.theme.*

/**
 * Foreground service hiển thị floating button trên Roblox.
 * Cho phép quick-apply config mà không cần rời game.
 */
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View

    companion object {
        const val CHANNEL_ID = "gfx_overlay_channel"
        const val NOTIF_ID   = 1001

        fun start(context: Context) {
            context.startForegroundService(Intent(context, OverlayService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showOverlay()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            try { windowManager.removeView(overlayView) } catch (_: Exception) {}
        }
    }

    // ── Overlay view ──────────────────────────────────────────────

    private fun showOverlay() {
        val params = WindowManager.LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT,
            TYPE_APPLICATION_OVERLAY,
            FLAG_NOT_FOCUSABLE or FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16
            y = 200
        }

        // ComposeView inside Service requires LifecycleOwner trick
        val lifecycleOwner = ServiceLifecycleOwner()
        lifecycleOwner.start()

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            setContent {
                GfxToolTheme {
                    OverlayWidget(
                        onOpen   = { openMainApp() },
                        onDismiss = { stopSelf() },
                    )
                }
            }
        }

        // Drag support
        var initialX = 0; var initialY = 0
        var touchX = 0f; var touchY = 0f
        overlayView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y
                    touchX = event.rawX; touchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (touchX - event.rawX).toInt()
                    params.y = initialY + (event.rawY - touchY).toInt()
                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                else -> false
            }
        }

        windowManager.addView(overlayView, params)
    }

    private fun openMainApp() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        )
    }

    // ── Notification ─────────────────────────────────────────────

    private fun buildNotification(): Notification {
        val intent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GFX Tool đang chạy")
            .setContentText("Chạm để mở cài đặt")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentIntent(intent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "GFX Overlay",
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}

// ── Overlay UI ────────────────────────────────────────────────────

@Composable
private fun OverlayWidget(onOpen: () -> Unit, onDismiss: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.End) {
        if (expanded) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceDark.copy(alpha = 0.95f),
                modifier = Modifier.padding(bottom = 6.dp),
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    OverlayAction("⚡ Performance") { onOpen() }
                    OverlayAction("⚖️ Balanced")    { onOpen() }
                    OverlayAction("💎 Ultra")       { onOpen() }
                    Divider(color = Outline, modifier = Modifier.padding(vertical = 4.dp))
                    OverlayAction("⚙️ Mở app")     { onOpen() }
                    OverlayAction("✕  Đóng")       { onDismiss() }
                }
            }
        }

        // FAB
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(AccentPrimary)
                .clickable { expanded = !expanded },
            contentAlignment = Alignment.Center,
        ) {
            Text("GFX", color = Color.White, fontSize = 11.sp, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
private fun OverlayAction(label: String, onClick: () -> Unit) {
    Text(
        label,
        style    = MaterialTheme.typography.bodySmall,
        color    = OnSurface,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    )
}

// ── LifecycleOwner shim for Service ──────────────────────────────

private class ServiceLifecycleOwner :
    LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry   = LifecycleRegistry(this)
    private val store               = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry get() = savedStateController.savedStateRegistry

    fun start() {
        savedStateController.performAttach()
        savedStateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }
}
