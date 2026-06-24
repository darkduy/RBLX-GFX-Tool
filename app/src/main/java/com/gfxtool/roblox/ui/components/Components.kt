package com.gfxtool.roblox.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gfxtool.roblox.ui.theme.*

// ── Section Card ──────────────────────────────────────────────────

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        color     = SurfaceDark,
        tonalElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 14.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint    = AccentPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color      = OnSurface,
                        letterSpacing = 0.5.sp,
                    ),
                )
            }
            content()
        }
    }
}

// ── Labeled Slider ────────────────────────────────────────────────

@Composable
fun LabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    displayValue: String = "%.0f%%".format(value * 100),
    enabled: Boolean = true,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) OnSurface else OnSurfaceMuted,
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = SurfaceVariant,
            ) {
                Text(
                    displayValue,
                    style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color    = AccentSecondary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        Slider(
            value         = value,
            onValueChange = onValueChange,
            valueRange    = valueRange,
            steps         = steps,
            enabled       = enabled,
            colors        = SliderDefaults.colors(
                thumbColor           = AccentPrimary,
                activeTrackColor     = AccentPrimary,
                inactiveTrackColor   = Outline,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ── Toggle Row ────────────────────────────────────────────────────

@Composable
fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) OnSurface else OnSurfaceMuted,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceMuted,
                )
            }
        }
        Switch(
            checked         = checked,
            onCheckedChange = onCheckedChange,
            enabled         = enabled,
            colors          = SwitchDefaults.colors(
                checkedThumbColor  = Color.White,
                checkedTrackColor  = AccentPrimary,
                uncheckedTrackColor = Outline,
            ),
        )
    }
}

// ── Segment Picker ────────────────────────────────────────────────

@Composable
fun SegmentPicker(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    enabled: Boolean = true,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) OnSurface else OnSurfaceMuted,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceVariant),
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            if (isSelected) AccentPrimary
                            else Color.Transparent
                        )
                        .clickable(enabled = enabled) { onSelect(index) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        option,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = if (isSelected) Color.White else OnSurfaceMuted,
                    )
                }
            }
        }
    }
}

// ── Preset Chip ───────────────────────────────────────────────────

@Composable
fun PresetChip(
    label: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape    = RoundedCornerShape(10.dp),
        color    = SurfaceVariant,
        border   = BorderStroke(1.dp, Outline),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurface,
            )
        }
    }
}

// ── Apply Button ──────────────────────────────────────────────────

@Composable
fun ApplyButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick  = { if (!isLoading) onClick() },
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = AccentPrimary,
            contentColor   = Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color    = Color.White,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Áp dụng Config",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}
