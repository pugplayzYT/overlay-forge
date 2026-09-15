package com.pugplayz.overlayforge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.pugplayz.overlayforge.ui.theme.Panel
import com.pugplayz.overlayforge.ui.theme.PanelRaised
import com.pugplayz.overlayforge.ui.theme.TextMuted

@Composable
internal fun AppHeader(
    exporting: Boolean,
    progress: Int,
    onImport: () -> Unit,
    onExport: () -> Unit
) {
    Surface(color = Panel, shadowElevation = 8.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(66.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(ForgeCyan),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Terminal, null, tint = Ink, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("OverlayForge", fontWeight = FontWeight.Black, fontSize = 19.sp)
                Text("HTML + CSS media overlays", color = TextMuted, fontSize = 11.sp)
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onImport,
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PanelRaised)
            ) {
                Icon(Icons.Rounded.AddPhotoAlternate, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text("Media")
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onExport,
                enabled = !exporting,
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForgeCyan, contentColor = Ink)
            ) {
                if (exporting) {
                    CircularProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Ink,
                        trackColor = Ink.copy(alpha = .2f)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text("$progress%")
                } else {
                    Icon(Icons.Rounded.Download, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Export", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
