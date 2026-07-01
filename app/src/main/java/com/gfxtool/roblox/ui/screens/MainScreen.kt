package com.gfxtool.roblox.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gfxtool.roblox.data.model.*
import com.gfxtool.roblox.ui.GfxViewModel
import com.gfxtool.roblox.ui.components.*
import com.gfxtool.roblox.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: GfxViewModel, onNavigateToAbout: () -> Unit) {
    val ui by vm.ui.collectAsState()
    val s  = ui.settings

    var saveDialogOpen by remember { mutableStateOf(false) }
    var presetName     by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(ui.snackMessage) {
        ui.snackMessage?.let {
            snackbarHost.showSnackbar(it, duration = SnackbarDuration.Short)
            vm.clearSnack()
        }
    }

    // Share exported file via system share sheet
    LaunchedEffect(ui.exportedFilePath) {
        ui.exportedFilePath?.let { path ->
            val file = java.io.File(path)
            val uri  = androidx.core.content.FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", file,
            )
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Chia sẻ GFX Config"))
            vm.clearExportedPath()
        }
    }

    Scaffold(
        containerColor  = BackgroundDark,
        snackbarHost    = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "RBLX",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color      = AccentPrimary,
                            ),
                        )
                        Text(
                            " GFX Tool",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Light,
                                color      = OnSurface,
                            ),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                ),
                actions = {
                    // Hướng dẫn sử dụng
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Hướng dẫn",
                            tint = OnSurfaceMuted,
                        )
                    }

                    // Root indicator
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (ui.isRootAvailable) Color(0xFF1A3A1A) else Color(0xFF3A1A1A),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (ui.isRootAvailable) Color(0xFF4CAF50)
                                        else Color(0xFFFF5370)
                                    )
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                if (ui.isRootAvailable) "Root" else "No Root",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (ui.isRootAvailable) Color(0xFF4CAF50) else Color(0xFFFF5370),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceDark,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Apply mode selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Chế độ áp dụng",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceMuted,
                        )
                        Row {
                            ApplyModeChip(
                                "File",
                                selected = ui.applyMode == ApplyMode.FILE_CONFIG,
                                onClick  = { vm.setApplyMode(ApplyMode.FILE_CONFIG) },
                            )
                            Spacer(Modifier.width(6.dp))
                            ApplyModeChip(
                                "Root",
                                selected  = ui.applyMode == ApplyMode.ROOT_SHELL,
                                onClick   = { vm.setApplyMode(ApplyMode.ROOT_SHELL) },
                                enabled   = ui.isRootAvailable,
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Export button
                        OutlinedButton(
                            onClick = { vm.exportConfig() },
                            shape   = RoundedCornerShape(14.dp),
                            border  = BorderStroke(1.dp, Outline),
                            colors  = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface),
                            modifier = Modifier.height(52.dp).weight(1f),
                        ) {
                            Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Xuất", style = MaterialTheme.typography.bodySmall)
                        }

                        // Save preset
                        OutlinedButton(
                            onClick = { saveDialogOpen = true },
                            shape   = RoundedCornerShape(14.dp),
                            border  = BorderStroke(1.dp, Outline),
                            colors  = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface),
                            modifier = Modifier.height(52.dp).weight(1f),
                        ) {
                            Icon(Icons.Default.Bookmark, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Lưu", style = MaterialTheme.typography.bodySmall)
                        }

                        // Apply
                        ApplyButton(
                            onClick   = { vm.applyConfig() },
                            isLoading = ui.isApplying,
                            modifier  = Modifier.weight(2f),
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Roblox running warning ────────────────────────────
            if (ui.isRobloxRunning) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                        .background(Color(0xFF2A1500))
                        .border(1.dp, Color(0xFF6A3500), androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null,
                            tint = Color(0xFFFFAB40), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Roblox đang chạy — khởi động lại game sau khi áp dụng",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFCC80),
                        )
                    }
                }
            }

            // ── Storage permission warning ────────────────────────────
            if (!ui.hasStoragePermission) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                        .background(Color(0xFF1A0A2A))
                        .border(1.dp, AccentPrimary.copy(alpha = 0.6f),
                            androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Warning, null,
                            tint = AccentPrimary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Thiếu quyền truy cập file",
                                style = MaterialTheme.typography.bodySmall
                                    .copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                color = OnSurface,
                            )
                            Text(
                                "Cần quyền Quản lý tất cả file để ghi config Roblox",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceMuted,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    context.startActivity(
                                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = AccentPrimary),
                        ) {
                            Text("Cấp quyền",
                                style = MaterialTheme.typography.labelSmall
                                    .copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                        }
                    }
                }
            }

            // ── Quick Presets ───────────────────────────────────
            PresetRow(
                userPresets = ui.presets,
                onBuiltin   = { vm.applyPreset(it) },
                onUser      = { vm.applyPreset(it.settings) },
                onDelete    = { vm.deletePreset(it.id) },
            )

            // ── Resolution & FPS ─────────────────────────────────
            SectionCard("Độ phân giải & FPS", Icons.Default.Tune) {
                LabeledSlider(
                    label         = "Độ phân giải",
                    value         = s.renderResolutionScale,
                    onValueChange = { vm.updateSettings { copy(renderResolutionScale = it) } },
                    valueRange    = 0.5f..1.0f,
                    steps         = 9,
                    displayValue  = "%.0f%%".format(s.renderResolutionScale * 100),
                )
                Spacer(Modifier.height(8.dp))
                SegmentPicker(
                    label         = "Giới hạn FPS",
                    options       = GfxSettings.FPS_LABELS,
                    selectedIndex = s.fpsCapIndex,
                    onSelect      = { vm.updateSettings { copy(fpsCapIndex = it) } },
                )
                Spacer(Modifier.height(8.dp))
                LabeledSlider(
                    label         = "Chất lượng đồ họa",
                    value         = s.graphicsQuality.toFloat(),
                    onValueChange = { vm.updateSettings { copy(graphicsQuality = it.toInt()) } },
                    valueRange    = 1f..10f,
                    steps         = 8,
                    displayValue  = "${s.graphicsQuality}/10",
                )
            }

            // ── Shadows ──────────────────────────────────────────
            SectionCard("Bóng đổ (Shadows)", Icons.Default.WbSunny) {
                ToggleRow(
                    label           = "Bật bóng đổ",
                    checked         = s.shadowsEnabled,
                    onCheckedChange = { vm.updateSettings { copy(shadowsEnabled = it) } },
                )
                if (s.shadowsEnabled) {
                    Spacer(Modifier.height(8.dp))
                    SegmentPicker(
                        label         = "Chất lượng bóng",
                        options       = GfxSettings.QUALITY_LABELS,
                        selectedIndex = s.shadowQuality,
                        onSelect      = { vm.updateSettings { copy(shadowQuality = it) } },
                    )
                }
            }

            // ── Textures ──────────────────────────────────────────
            SectionCard("Texture", Icons.Default.Layers) {
                ToggleRow(
                    label           = "Bật texture",
                    checked         = s.texturesEnabled,
                    onCheckedChange = { vm.updateSettings { copy(texturesEnabled = it) } },
                )
                if (s.texturesEnabled) {
                    Spacer(Modifier.height(8.dp))
                    SegmentPicker(
                        label         = "Chất lượng texture",
                        options       = GfxSettings.QUALITY_LABELS.drop(1),
                        selectedIndex = s.textureQuality,
                        onSelect      = { vm.updateSettings { copy(textureQuality = it) } },
                    )
                }
            }

            // ── Particles ─────────────────────────────────────────
            SectionCard("Particles & Hiệu ứng", Icons.Default.AutoAwesome) {
                ToggleRow("Particles",  s.particlesEnabled,  { vm.updateSettings { copy(particlesEnabled  = it) } })
                ToggleRow("Vụ nổ",      s.explosionsEnabled, { vm.updateSettings { copy(explosionsEnabled = it) } })
                ToggleRow("Mây",        s.cloudsEnabled,     { vm.updateSettings { copy(cloudsEnabled     = it) } })
                ToggleRow("Cỏ",         s.grassRenderingEnabled, { vm.updateSettings { copy(grassRenderingEnabled = it) } })
            }

            // ── Lighting ──────────────────────────────────────────
            SectionCard("Ánh sáng (Lighting)", Icons.Default.LightMode) {
                SegmentPicker(
                    label         = "Công nghệ ánh sáng",
                    options       = GfxSettings.LIGHTING_OPTIONS,
                    selectedIndex = s.lightingTechnologyIndex,
                    onSelect      = { vm.updateSettings { copy(lightingTechnologyIndex = it) } },
                )
                Spacer(Modifier.height(8.dp))
                ToggleRow("Ambient Occlusion", s.ambientOcclusion, { vm.updateSettings { copy(ambientOcclusion = it) } },
                    subtitle = "Yêu cầu Future Lighting")
                ToggleRow("Bloom",             s.bloomEnabled,     { vm.updateSettings { copy(bloomEnabled     = it) } })
                ToggleRow("Sun Rays",           s.sunRaysEnabled,   { vm.updateSettings { copy(sunRaysEnabled   = it) } })
                ToggleRow("Depth of Field",     s.depthOfFieldEnabled, { vm.updateSettings { copy(depthOfFieldEnabled = it) } })
            }

            // ── Fog ───────────────────────────────────────────────
            SectionCard("Sương mù (Fog)", Icons.Default.Cloud) {
                ToggleRow("Bật sương mù", s.fogEnabled, { vm.updateSettings { copy(fogEnabled = it) } })
                if (s.fogEnabled) {
                    Spacer(Modifier.height(8.dp))
                    LabeledSlider(
                        label         = "Bắt đầu",
                        value         = s.fogStart,
                        onValueChange = { vm.updateSettings { copy(fogStart = it) } },
                        valueRange    = 0f..10000f,
                        displayValue  = "%.0f".format(s.fogStart),
                    )
                    Spacer(Modifier.height(4.dp))
                    LabeledSlider(
                        label         = "Kết thúc",
                        value         = s.fogEnd,
                        onValueChange = { vm.updateSettings { copy(fogEnd = it) } },
                        valueRange    = 1000f..100000f,
                        displayValue  = "%.0f".format(s.fogEnd),
                    )
                }
            }

            // ── Post Processing ────────────────────────────────────
            SectionCard("Hậu kỳ (Post Processing)", Icons.Default.Palette) {
                ToggleRow("Post Processing",    s.postProcessingEnabled, { vm.updateSettings { copy(postProcessingEnabled = it) } })
                ToggleRow("Anti-Aliasing",      s.antiAliasingEnabled,   { vm.updateSettings { copy(antiAliasingEnabled   = it) } })
                ToggleRow("Phản chiếu nước",    s.waterReflectionsEnabled, { vm.updateSettings { copy(waterReflectionsEnabled = it) } })
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    // ── Save preset dialog ────────────────────────────────────────
    if (saveDialogOpen) {
        AlertDialog(
            onDismissRequest = { saveDialogOpen = false },
            containerColor   = SurfaceDark,
            title  = { Text("Lưu Preset", color = OnSurface) },
            text   = {
                OutlinedTextField(
                    value         = presetName,
                    onValueChange = { presetName = it },
                    label         = { Text("Tên preset") },
                    singleLine    = true,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = AccentPrimary,
                        unfocusedBorderColor = Outline,
                        focusedLabelColor    = AccentPrimary,
                        cursorColor          = AccentPrimary,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (presetName.isNotBlank()) {
                            vm.savePreset(presetName.trim())
                            presetName     = ""
                            saveDialogOpen = false
                        }
                    },
                ) { Text("Lưu", color = AccentPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { saveDialogOpen = false }) {
                    Text("Hủy", color = OnSurfaceMuted)
                }
            },
        )
    }
}

// ── Preset row component ─────────────────────────────────────────

@Composable
private fun PresetRow(
    userPresets : List<com.gfxtool.roblox.data.model.UserPreset>,
    onBuiltin   : (GfxSettings) -> Unit,
    onUser      : (com.gfxtool.roblox.data.model.UserPreset) -> Unit,
    onDelete    : (com.gfxtool.roblox.data.model.UserPreset) -> Unit,
) {
    Column {
        Text(
            "Preset nhanh",
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceMuted,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { PresetChip("Performance", "⚡", onClick = { onBuiltin(GfxSettings.PRESET_PERFORMANCE) }) }
            item { PresetChip("Balanced",    "⚖️",  onClick = { onBuiltin(GfxSettings.PRESET_BALANCED)    }) }
            item { PresetChip("Ultra",       "💎", onClick = { onBuiltin(GfxSettings.PRESET_ULTRA)       }) }
            items(userPresets) { preset ->
                UserPresetChip(preset, onClick = { onUser(preset) }, onDelete = { onDelete(preset) })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UserPresetChip(
    preset   : com.gfxtool.roblox.data.model.UserPreset,
    onClick  : () -> Unit,
    onDelete : () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Surface(
        shape    = RoundedCornerShape(10.dp),
        color    = SurfaceVariant,
        border   = BorderStroke(1.dp, AccentPrimary.copy(alpha = 0.4f)),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .combinedClickable(
                onClick      = onClick,
                onLongClick  = { showMenu = true },
            ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("📌", fontSize = 18.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                preset.name,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurface,
            )
        }
        DropdownMenu(
            expanded         = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            DropdownMenuItem(
                text    = { Text("Xóa preset", color = Color(0xFFFF5370)) },
                onClick = { showMenu = false; onDelete() },
                leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFFF5370)) },
            )
        }
    }
}

// ── Apply mode chip ───────────────────────────────────────────────

@Composable
private fun ApplyModeChip(
    label    : String,
    selected : Boolean,
    onClick  : () -> Unit,
    enabled  : Boolean = true,
) {
    Surface(
        shape    = RoundedCornerShape(8.dp),
        color    = if (selected) AccentPrimary else SurfaceVariant,
        border   = if (!selected) BorderStroke(1.dp, Outline) else null,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Text(
            label,
            style    = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            ),
            color    = when {
                !enabled -> OnSurfaceMuted
                selected -> Color.White
                else     -> OnSurface
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}