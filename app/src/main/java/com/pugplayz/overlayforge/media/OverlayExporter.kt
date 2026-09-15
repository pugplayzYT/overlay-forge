package com.pugplayz.overlayforge.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.OverlayEffect
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

fun buildTransformedOverlay(
    frameWidth: Int,
    frameHeight: Int,
    webBitmap: Bitmap,
    offsetXNormalized: Float,
    offsetYNormalized: Float,
    scale: Float
): Bitmap {
    val output = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    canvas.save()
    canvas.translate(offsetXNormalized * frameWidth, offsetYNormalized * frameHeight)
    canvas.scale(scale, scale, 0f, 0f)
    canvas.drawBitmap(
        webBitmap,
        null,
        RectF(0f, 0f, frameWidth.toFloat(), frameHeight.toFloat()),
        paint
    )
    canvas.restore()
    return output
}

fun composePhoto(base: Bitmap, transformedOverlay: Bitmap): Bitmap {
    val out = Bitmap.createBitmap(base.width, base.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    canvas.drawBitmap(base, 0f, 0f, paint)
    canvas.drawBitmap(transformedOverlay, 0f, 0f, paint)
    return out
}

@OptIn(UnstableApi::class)
class VideoExporter(private val context: Context) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var transformer: Transformer? = null
    private var progressRunnable: Runnable? = null
    private var tempFile: File? = null

    fun export(
        input: Uri,
        overlayBitmap: Bitmap,
        onProgress: (Int) -> Unit,
        onComplete: (Uri) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        cancel()
        val file = File(context.cacheDir, "overlayforge_${System.currentTimeMillis()}.mp4")
        tempFile = file

        val bitmapOverlay = BitmapOverlay.createStaticBitmapOverlay(overlayBitmap)
        val overlayEffect = OverlayEffect(listOf(bitmapOverlay))
        val effects = Effects(emptyList(), listOf(overlayEffect))
        val editedMediaItem = EditedMediaItem.Builder(MediaItem.fromUri(input))
            .setEffects(effects)
            .build()

        val listener = object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                stopProgressPolling()
                transformer = null
                scope.launch {
                    try {
                        val published = publishMp4ToGallery(context, file.absolutePath)
                        file.delete()
                        withContext(Dispatchers.Main) { onComplete(published) }
                    } catch (t: Throwable) {
                        withContext(Dispatchers.Main) { onError(t) }
                    } finally {
                        if (!overlayBitmap.isRecycled) overlayBitmap.recycle()
                    }
                }
            }

            override fun onError(
                composition: Composition,
                exportResult: ExportResult,
                exportException: ExportException
            ) {
                stopProgressPolling()
                transformer = null
                file.delete()
                if (!overlayBitmap.isRecycled) overlayBitmap.recycle()
                onError(exportException)
            }
        }

        transformer = Transformer.Builder(context)
            .addListener(listener)
            .build()
            .also { it.start(editedMediaItem, file.absolutePath) }

        startProgressPolling(onProgress)
    }

    fun cancel() {
        stopProgressPolling()
        transformer?.cancel()
        transformer = null
        tempFile?.delete()
        tempFile = null
    }

    private fun startProgressPolling(onProgress: (Int) -> Unit) {
        val holder = ProgressHolder()
        val runnable = object : Runnable {
            override fun run() {
                val current = transformer ?: return
                val state = current.getProgress(holder)
                if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                    onProgress(holder.progress)
                }
                if (state != Transformer.PROGRESS_STATE_NOT_STARTED) {
                    mainHandler.postDelayed(this, 250)
                }
            }
        }
        progressRunnable = runnable
        mainHandler.post(runnable)
    }

    private fun stopProgressPolling() {
        progressRunnable?.let(mainHandler::removeCallbacks)
        progressRunnable = null
    }
}
