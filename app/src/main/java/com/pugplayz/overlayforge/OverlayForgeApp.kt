package com.pugplayz.overlayforge

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pugplayz.overlayforge.media.MediaInfo
import com.pugplayz.overlayforge.media.VideoExporter
import com.pugplayz.overlayforge.media.buildTransformedOverlay
import com.pugplayz.overlayforge.media.composePhoto
import com.pugplayz.overlayforge.media.decodeImage
import com.pugplayz.overlayforge.media.readMediaInfo
import com.pugplayz.overlayforge.media.savePngToGallery
import com.pugplayz.overlayforge.ui.theme.Ink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun OverlayForgeApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val exporter = remember { VideoExporter(context.applicationContext) }

    var media by remember { mutableStateOf<MediaInfo?>(null) }
    var html by remember { mutableStateOf(SAMPLE_HTML) }
    var css by remember { mutableStateOf(SAMPLE_CSS) }
    var selectedTab by remember { mutableStateOf(EditorTab.HTML) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var overlayScale by remember { mutableFloatStateOf(1f) }
    var exportProgress by remember { mutableIntStateOf(0) }
    var exporting by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                // Some providers grant only a transient read permission; that is enough for this session.
            }
            scope.launch {
                runCatching { readMediaInfo(context, uri) }
                    .onSuccess {
                        media = it
                        offsetX = 0f
                        offsetY = 0f
                        overlayScale = 1f
                    }
                    .onFailure { snackbar.showSnackbar("Could not open that media: ${it.message}") }
            }
        }
    }

    fun beginExport() {
        val info = media
        val view = webView
        if (info == null || view == null) {
            scope.launch { snackbar.showSnackbar("Pick a photo or video first.") }
            return
        }
        if (exporting) return

        exporting = true
        exportProgress = 0
        scope.launch {
            try {
                val webBitmap = captureWebView(view)
                val transformed = withContext(Dispatchers.Default) {
                    buildTransformedOverlay(
                        info.width,
                        info.height,
                        webBitmap,
                        offsetX,
                        offsetY,
                        overlayScale
                    )
                }
                webBitmap.recycle()

                if (info.isVideo) {
                    exporter.export(
                        input = info.uri,
                        overlayBitmap = transformed,
                        onProgress = { exportProgress = it },
                        onComplete = {
                            exporting = false
                            exportProgress = 100
                            scope.launch { snackbar.showSnackbar("Video exported to Movies/OverlayForge") }
                        },
                        onError = {
                            exporting = false
                            scope.launch { snackbar.showSnackbar("Video export failed: ${it.message}") }
                        }
                    )
                } else {
                    val base = decodeImage(context, info.uri)
                    val output = withContext(Dispatchers.Default) { composePhoto(base, transformed) }
                    savePngToGallery(context, output)
                    base.recycle()
                    transformed.recycle()
                    output.recycle()
                    exportProgress = 100
                    exporting = false
                    snackbar.showSnackbar("Photo exported to Pictures/OverlayForge")
                }
            } catch (t: Throwable) {
                exporting = false
                snackbar.showSnackbar("Export failed: ${t.message}")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exporter.cancel()
            webView?.destroy()
        }
    }

    Scaffold(
        containerColor = Ink,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            AppHeader(
                exporting = exporting,
                progress = exportProgress,
                onImport = { picker.launch(arrayOf("image/*", "video/*")) },
                onExport = ::beginExport
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val wide = maxWidth >= 900.dp
            if (wide) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PreviewPanel(
                        media = media,
                        html = html,
                        css = css,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        overlayScale = overlayScale,
                        onTransform = { dx, dy, zoom ->
                            offsetX = (offsetX + dx).coerceIn(-1.5f, 1.5f)
                            offsetY = (offsetY + dy).coerceIn(-1.5f, 1.5f)
                            overlayScale = (overlayScale * zoom).coerceIn(.2f, 4f)
                        },
                        onResetTransform = {
                            offsetX = 0f
                            offsetY = 0f
                            overlayScale = 1f
                        },
                        onWebViewReady = { webView = it },
                        modifier = Modifier.weight(1.15f).fillMaxHeight()
                    )
                    EditorPanel(
                        selectedTab = selectedTab,
                        onTab = { selectedTab = it },
                        html = html,
                        css = css,
                        onHtml = { html = it },
                        onCss = { css = it },
                        onResetSample = {
                            html = SAMPLE_HTML
                            css = SAMPLE_CSS
                        },
                        modifier = Modifier.weight(.85f).fillMaxHeight()
                    )
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PreviewPanel(
                        media = media,
                        html = html,
                        css = css,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        overlayScale = overlayScale,
                        onTransform = { dx, dy, zoom ->
                            offsetX = (offsetX + dx).coerceIn(-1.5f, 1.5f)
                            offsetY = (offsetY + dy).coerceIn(-1.5f, 1.5f)
                            overlayScale = (overlayScale * zoom).coerceIn(.2f, 4f)
                        },
                        onResetTransform = {
                            offsetX = 0f
                            offsetY = 0f
                            overlayScale = 1f
                        },
                        onWebViewReady = { webView = it },
                        modifier = Modifier.fillMaxWidth().weight(1.05f)
                    )
                    EditorPanel(
                        selectedTab = selectedTab,
                        onTab = { selectedTab = it },
                        html = html,
                        css = css,
                        onHtml = { html = it },
                        onCss = { css = it },
                        onResetSample = {
                            html = SAMPLE_HTML
                            css = SAMPLE_CSS
                        },
                        modifier = Modifier.fillMaxWidth().weight(.95f)
                    )
                }
            }
        }
    }
}
