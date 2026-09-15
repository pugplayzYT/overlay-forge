package com.pugplayz.overlayforge

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.pugplayz.overlayforge.media.MediaInfo
import com.pugplayz.overlayforge.media.decodeImage
import com.pugplayz.overlayforge.ui.theme.Border
import com.pugplayz.overlayforge.ui.theme.ForgeCyan
import com.pugplayz.overlayforge.ui.theme.ForgeMint
import com.pugplayz.overlayforge.ui.theme.Panel
import com.pugplayz.overlayforge.ui.theme.TextMuted

@Composable
internal fun PreviewPanel(
    media: MediaInfo?,
    html: String,
    css: String,
    offsetX: Float,
    offsetY: Float,
    overlayScale: Float,
    onTransform: (Float, Float, Float) -> Unit,
    onResetTransform: () -> Unit,
    onWebViewReady: (WebView) -> Unit,
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
                    .height(48.dp)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.DragIndicator, null, tint = ForgeMint, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("PREVIEW", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                Spacer(Modifier.weight(1f))
                Text("drag • pinch to scale", color = TextMuted, fontSize = 11.sp)
                IconButton(onClick = onResetTransform, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.Refresh, "Reset transform", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(color = Border)

            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                val aspect = media?.aspectRatio ?: (16f / 9f)
                val availableAspect = if (maxHeight.value > 0f) maxWidth.value / maxHeight.value else aspect
                val stageWidth = if (aspect >= availableAspect) maxWidth else (maxHeight.value * aspect).dp
                val stageHeight = if (aspect >= availableAspect) (maxWidth.value / aspect).dp else maxHeight

                Box(
                    Modifier
                        .size(stageWidth, stageHeight)
                        .clip(RoundedCornerShape(14.dp))
                        .background(androidx.compose.ui.graphics.Color.Black)
                        .border(1.dp, Border, RoundedCornerShape(14.dp))
                ) {
                    when {
                        media == null -> EmptyPreview()
                        media.isVideo -> VideoPreview(media.uri)
                        else -> PhotoPreview(media.uri)
                    }

                    HtmlOverlay(
                        html = html,
                        css = css,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        scale = overlayScale,
                        onTransform = onTransform,
                        onWebViewReady = onWebViewReady
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPreview() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(ForgeCyan.copy(alpha = .12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.AddPhotoAlternate, null, tint = ForgeCyan, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text("Choose a photo or video", fontWeight = FontWeight.Bold)
        Text("Your HTML overlay is already loaded", color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
private fun PhotoPreview(uri: Uri) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        value = runCatching { decodeImage(context, uri) }.getOrNull()
    }
    DisposableEffect(bitmap) {
        onDispose {
            bitmap?.let { if (!it.isRecycled) it.recycle() }
        }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun VideoPreview(uri: Uri) {
    val context = LocalContext.current
    val player = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setShutterBackgroundColor(Color.BLACK)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
