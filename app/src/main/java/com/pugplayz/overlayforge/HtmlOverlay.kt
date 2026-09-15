package com.pugplayz.overlayforge

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun HtmlOverlay(
    html: String,
    css: String,
    offsetX: Float,
    offsetY: Float,
    scale: Float,
    onTransform: (Float, Float, Float) -> Unit,
    onWebViewReady: (WebView) -> Unit
) {
    val widthPx = remember { mutableIntStateOf(1) }
    val heightPx = remember { mutableIntStateOf(1) }
    val document = remember(html, css) { buildHtmlDocument(html, css) }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(Color.TRANSPARENT)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
                isClickable = false
                isFocusable = false
                settings.javaScriptEnabled = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.textZoom = 100
                settings.setSupportZoom(false)
                webViewClient = WebViewClient()

                // Load the first document here instead of waiting for a Compose effect.
                // The old effect could run while the WebView reference was still null,
                // leaving a completely transparent/blank overlay until the editor changed.
                tag = document
                loadOverlayDocument(this, document)
                onWebViewReady(this)
            }
        },
        update = { webView ->
            // AndroidView's update block is guaranteed to run with the actual view.
            // Only reload when HTML/CSS changes so dragging and pinching do not refresh the page.
            if (webView.tag != document) {
                webView.tag = document
                loadOverlayDocument(webView, document)
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
            .onSizeChanged {
                widthPx.intValue = it.width.coerceAtLeast(1)
                heightPx.intValue = it.height.coerceAtLeast(1)
            }
            .graphicsLayer {
                translationX = offsetX * widthPx.intValue
                translationY = offsetY * heightPx.intValue
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
    )

    Box(
        Modifier
            .fillMaxSize()
            .zIndex(2f)
            .pointerInput(widthPx.intValue, heightPx.intValue) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onTransform(
                        pan.x / widthPx.intValue.toFloat(),
                        pan.y / heightPx.intValue.toFloat(),
                        zoom
                    )
                }
            }
    )
}

private fun loadOverlayDocument(webView: WebView, document: String) {
    webView.loadDataWithBaseURL(
        "https://overlayforge.local/",
        document,
        "text/html",
        "utf-8",
        null
    )
    webView.postInvalidateOnAnimation()
}

internal fun buildHtmlDocument(html: String, css: String): String = """
<!doctype html>
<html>
<head>
<meta name="viewport" content="width=device-width, height=device-height, initial-scale=1, maximum-scale=1, user-scalable=no">
<style>
:root {
  --of-vw: 1vw;
  --of-vh: 1vh;
  --of-vmin: 1vmin;
  --of-vmax: 1vmax;
}
html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: transparent !important;
  -webkit-text-size-adjust: 100%;
  text-size-adjust: 100%;
}
body {
  position: relative;
}
*, *::before, *::after {
  box-sizing: border-box;
}
img, svg, video, canvas {
  max-width: 100%;
  max-height: 100%;
}
$css
</style>
</head>
<body>$html</body>
</html>
""".trimIndent()

internal suspend fun captureWebView(webView: WebView): Bitmap = withContext(Dispatchers.Main.immediate) {
    val width = webView.width.coerceAtLeast(1)
    val height = webView.height.coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.TRANSPARENT)
    webView.draw(canvas)
    bitmap
}
