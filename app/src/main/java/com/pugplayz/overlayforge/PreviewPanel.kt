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
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.pugplayz.overlayforge.media.MediaInfo
import com.pugplayz.overlayforge.media.decodeImagePreview
import com.pugplayz.overlayforge.ui.theme.Border
import com.pugplayz.overlayforge.ui.theme.ForgeCyan
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
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(start = 14.dp, end = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Preview", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("Drag • pinch", color = TextMuted, fontSize = 11.sp)
                Spacer(Modifier.width(3.dp))
                IconButton(onClick = onResetTransform, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Rounded.Refresh, "Reset overlay position", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(color = Border)

            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                val aspect = media?.aspectRatio ?: (16f / 9f)
                val availableAspect = if (maxHeight.value > 0f) maxWidth.value / maxHeight.value else aspect
                val stageWidth = if (aspect >= availableAspect) maxWidth else (maxHeight.value * aspect).dp
                val stageHeight = if (aspect >= availableAspect) (maxWidth.value / aspect).dp else maxHeight

                Box(
                    Modifier
                        .size(stageWidth, stageHeight)
                        .clip(RoundedCornerShape(12.dp))
                        .background(androidx.compose.ui.graphics.Color.Black)
                        .border(1.dp, Border, RoundedCornerShape(12.dp))
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
                .size(48.dp)
                .clip(CircleShape)
                .background(ForgeCyan.copy(alpha = .10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.AddPhotoAlternate, null, tint = ForgeCyan, modifier = Modifier.size(25.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text("Choose media", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("Photo or video", color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun PhotoPreview(uri: Uri) {
    val context = LocalContext.current
    var failed by remember(uri) { mutableStateOf(false) }
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        failed = false
        value = runCatching { decodeImagePreview(context, uri) }
            .onFailure { failed = true }
            .getOrNull()
    }

    DisposableEffect(bitmap) {
        onDispose {
            bitmap?.let { if (!it.isRecycled) it.recycle() }
        }
    }

    when {
        bitmap != null -> Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        failed -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Could not preview this image", color = TextMuted, fontSize = 12.sp)
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
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
