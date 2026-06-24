package com.gfxtool.roblox.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gfxtool.roblox.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Hướng dẫn & Về app", color = OnSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // App identity card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceDark,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("🎮", fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "RBX GFX Tool",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = OnSurface,
                        ),
                    )
                    Text(
                        "v1.0.0  •  Android 10+",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMuted,
                    )
                }
            }

            // How it works
            InfoCard(
                title = "Cách hoạt động",
                icon  = Icons.Default.Info,
            ) {
                StepItem("1", "App ghi file ClientAppSettings.json vào thư mục Roblox")
                StepItem("2", "Roblox đọc file này khi khởi động và áp dụng cài đặt đồ họa")
                StepItem("3", "Chế độ Root ghi trực tiếp vào /data/data/com.roblox.client (bypass scoped storage)")
            }

            // Mode guide
            InfoCard(
                title = "Chế độ FILE vs ROOT",
                icon  = Icons.Default.Settings,
            ) {
                ModeRow(
                    mode  = "FILE",
                    color = Color(0xFF4CAF50),
                    desc  = "Không cần root. Ghi vào Android/data — cần cấp quyền MANAGE_EXTERNAL_STORAGE."
                )
                Spacer(Modifier.height(8.dp))
                ModeRow(
                    mode  = "ROOT",
                    color = AccentPrimary,
                    desc  = "Cần Magisk/KernelSU. Ghi vào /data/data, hoạt động cả khi Roblox đang mở."
                )
            }

            // Preset guide
            InfoCard(
                title = "Preset",
                icon  = Icons.Default.Bookmark,
            ) {
                BulletItem("⚡ Performance — tắt hầu hết hiệu ứng, tối đa FPS")
                BulletItem("⚖️ Balanced — cài đặt mặc định cân bằng")
                BulletItem("💎 Ultra — bật tất cả, Future Lighting, 60 FPS stable")
                BulletItem("📌 Custom — giữ lâu chip để xóa preset tự tạo")
            }

            // Warning
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2A1A00),
                border = BorderStroke(1.dp, Color(0xFF5A3A00)),
            ) {
                Row(modifier = Modifier.padding(14.dp)) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFFFAB40), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Mở Roblox ít nhất một lần trước khi áp dụng để app tạo thư mục cần thiết. " +
                        "Config có hiệu lực từ lần khởi động Roblox tiếp theo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFCC80),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun InfoCard(
    title   : String,
    icon    : ImageVector,
    content : @Composable ColumnScope.() -> Unit,
) {
    Surface(shape = RoundedCornerShape(14.dp), color = SurfaceDark) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = AccentPrimary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = OnSurface,
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun StepItem(step: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
        Surface(shape = RoundedCornerShape(4.dp), color = AccentPrimary) {
            Text(
                step,
                style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color    = Color.White,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
    }
}

@Composable
private fun BulletItem(text: String) {
    Text(
        text,
        style    = MaterialTheme.typography.bodySmall,
        color    = OnSurfaceMuted,
        modifier = Modifier.padding(vertical = 2.dp),
    )
}

@Composable
private fun ModeRow(mode: String, color: Color, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
            Text(
                mode,
                style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color    = color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(desc, style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
    }
}
