package com.gfxtool.roblox.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gfxtool.roblox.ui.theme.*

/**
 * Màn hình hướng dẫn user cấp đủ quyền.
 * Hiển thị khi app phát hiện còn thiếu quyền cần thiết.
 */
@Composable
fun PermissionGuideSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current

    val hasAllFiles = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        Environment.isExternalStorageManager() else true
    val hasOverlay  = Settings.canDrawOverlays(context)

    Surface(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = SurfaceDark,
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Cấp quyền cần thiết",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnSurface,
            )
            Text(
                "App cần các quyền sau để hoạt động đúng cách.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
            )

            PermissionItem(
                icon        = Icons.Default.FolderOpen,
                title       = "Quản lý tất cả file",
                description = "Đọc/ghi thư mục Roblox trong Android/data",
                granted     = hasAllFiles,
                onGrant     = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        context.startActivity(
                            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                        )
                    }
                },
            )

            Spacer(Modifier.height(12.dp))

            PermissionItem(
                icon        = Icons.Default.Layers,
                title       = "Hiển thị đè lên app khác",
                description = "Cho phép floating button trên Roblox",
                granted     = hasOverlay,
                onGrant     = {
                    context.startActivity(
                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                    )
                },
            )

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick  = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceMuted),
                ) { Text("Bỏ qua") }

                Button(
                    onClick  = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                ) { Text("Xong") }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PermissionItem(
    icon        : ImageVector,
    title       : String,
    description : String,
    granted     : Boolean,
    onGrant     : () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint     = if (granted) Color(0xFF4CAF50) else AccentPrimary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = OnSurface,
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceMuted,
                )
            }
            Spacer(Modifier.width(8.dp))
            if (granted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint     = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp),
                )
            } else {
                TextButton(
                    onClick = onGrant,
                    colors  = ButtonDefaults.textButtonColors(contentColor = AccentPrimary),
                ) { Text("Cấp", style = MaterialTheme.typography.labelMedium) }
            }
        }
    }
}
