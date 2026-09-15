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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.viewinterop.AndroidView
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
    var view by remember { mutableStateOf<WebView?>(null) }
    var widthPx by remember { mutableIntStateOf(1) }
    var heightPx by remember { mutableIntStateOf(1) }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(Color.TRANSPARENT)
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
                isClickable = false
                isFocusable = false
                settings.javaScriptEnabled = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                webViewClient = WebViewClient()
                view = this
                onWebViewReady(this)
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                widthPx = it.width.coerceAtLeast(1)
                heightPx = it.height.coerceAtLeast(1)
            }
            .graphicsLayer {
                translationX = offsetX * widthPx
                translationY = offsetY * heightPx
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
    )

    LaunchedEffect(html, css, view) {
        view?.loadDataWithBaseURL(
            "https://overlayforge.local/",
            buildHtmlDocument(html, css),
            "text/html",
            "utf-8",
            null
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(widthPx, heightPx) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onTransform(
                        pan.x / widthPx.toFloat(),
                        pan.y / heightPx.toFloat(),
                        zoom
                    )
                }
            }
    )
}

private fun buildHtmlDocument(html: String, css: String): String = """
<!doctype html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
<style>
html, body {
  margin: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: transparent !important;
}
* { box-sizing: border-box; }
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
