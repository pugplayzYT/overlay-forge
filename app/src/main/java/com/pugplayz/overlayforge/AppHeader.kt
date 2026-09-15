package com.pugplayz.overlayforge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pugplayz.overlayforge.ui.theme.ForgeCyan
import com.pugplayz.overlayforge.ui.theme.Ink
import com.pugplayz.overlayforge.ui.theme.PanelRaised
import com.pugplayz.overlayforge.ui.theme.TextMuted
import com.pugplayz.overlayforge.ui.theme.TextPrimary

@Composable
internal fun AppHeader(
    exporting: Boolean,
    progress: Int,
    canExport: Boolean,
    onImport: () -> Unit,
    onExport: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Ink
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ForgeCyan),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Terminal, null, tint = Ink, modifier = Modifier.size(23.dp))
            }

            Spacer(Modifier.width(11.dp))
            Text(
                text = "OverlayForge",
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                maxLines = 1
            )

            Spacer(Modifier.weight(1f))

            FilledIconButton(
                onClick = onImport,
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(13.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PanelRaised,
                    contentColor = TextPrimary
                )
            ) {
                Icon(Icons.Rounded.AddPhotoAlternate, "Choose media", modifier = Modifier.size(21.dp))
            }

            Spacer(Modifier.width(8.dp))

            FilledIconButton(
                onClick = onExport,
                enabled = canExport && !exporting,
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(13.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = ForgeCyan,
                    contentColor = Ink,
                    disabledContainerColor = PanelRaised,
                    disabledContentColor = TextMuted.copy(alpha = .55f)
                )
            ) {
                if (exporting) {
                    CircularProgressIndicator(
                        progress = { progress.coerceIn(0, 100) / 100f },
                        modifier = Modifier.size(21.dp),
                        strokeWidth = 2.5.dp,
                        color = Ink,
                        trackColor = Ink.copy(alpha = .18f)
                    )
                } else {
                    Icon(Icons.Rounded.Download, "Export", modifier = Modifier.size(21.dp))
                }
            }
        }
    }
}
