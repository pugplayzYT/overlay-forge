package com.pugplayz.overlayforge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pugplayz.overlayforge.ui.theme.Border
import com.pugplayz.overlayforge.ui.theme.ForgeCyan
import com.pugplayz.overlayforge.ui.theme.Panel
import com.pugplayz.overlayforge.ui.theme.PanelRaised
import com.pugplayz.overlayforge.ui.theme.TextMuted

internal enum class EditorTab { HTML, CSS }

@Composable
internal fun EditorPanel(
    selectedTab: EditorTab,
    onTab: (EditorTab) -> Unit,
    html: String,
    css: String,
    onHtml: (String) -> Unit,
    onCss: (String) -> Unit,
    onResetSample: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Panel,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CodeTab(
                    text = "HTML",
                    icon = Icons.Rounded.Code,
                    selected = selectedTab == EditorTab.HTML,
                    onClick = { onTab(EditorTab.HTML) }
                )
                Spacer(Modifier.width(6.dp))
                CodeTab(
                    text = "CSS",
                    icon = Icons.Rounded.Palette,
                    selected = selectedTab == EditorTab.CSS,
                    onClick = { onTab(EditorTab.CSS) }
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "Reset sample",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onResetSample)
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                )
            }
            HorizontalDivider(color = Border)
            CodeEditor(
                value = if (selectedTab == EditorTab.HTML) html else css,
                onValueChange = if (selectedTab == EditorTab.HTML) onHtml else onCss,
                placeholder = if (selectedTab == EditorTab.HTML) "<div>Your overlay</div>" else ".overlay { color: white; }",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun CodeTab(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) ForgeCyan.copy(alpha = .14f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (selected) ForgeCyan else TextMuted, modifier = Modifier.width(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = if (selected) ForgeCyan else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CodeEditor(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .background(PanelRaised)
            .padding(12.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxSize(),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 19.sp
            ),
            cursorBrush = SolidColor(ForgeCyan),
            decorationBox = { inner ->
                if (value.isBlank()) {
                    Text(placeholder, color = TextMuted, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                }
                inner()
            }
        )
    }
}
