package com.pugplayz.overlayforge.media

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class MediaInfo(
    val uri: Uri,
    val mimeType: String,
    val width: Int,
    val height: Int,
    val isVideo: Boolean
) {
    val aspectRatio: Float
        get() = if (height > 0) width.toFloat() / height.toFloat() else 16f / 9f
}

suspend fun readMediaInfo(context: Context, uri: Uri): MediaInfo = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    val mime = resolver.getType(uri).orEmpty()
    val isVideo = mime.startsWith("video/")

    if (isVideo) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            var width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 1920
            var height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 1080
            val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
            if (rotation == 90 || rotation == 270) {
                val tmp = width
                width = height
                height = tmp
            }
            MediaInfo(uri, mime.ifBlank { "video/*" }, width, height, true)
        } finally {
            retriever.release()
        }
    } else {
        val (width, height) = readImageBounds(context, uri)
        MediaInfo(uri, mime.ifBlank { "image/*" }, width, height, false)
    }
}

internal fun readImageBounds(context: Context, uri: Uri): Pair<Int, Int> {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    context.contentResolver.openInputStream(uri).use { input ->
        requireNotNull(input) { "Unable to open selected image" }
        BitmapFactory.decodeStream(input, null, options)
    }

    val width = options.outWidth
    val height = options.outHeight
    require(width > 0 && height > 0) {
        "Selected file is not a supported image"
    }
    return width to height
}

internal fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
    require(width > 0 && height > 0)
    require(maxDimension > 0)

    var sample = 1
    var sampledWidth = width
    var sampledHeight = height
    while (maxOf(sampledWidth, sampledHeight) > maxDimension * 2) {
        sample *= 2
        sampledWidth = width / sample
        sampledHeight = height / sample
    }
    return sample.coerceAtLeast(1)
}

suspend fun decodeImagePreview(
    context: Context,
    uri: Uri,
    maxDimension: Int = 2048
): Bitmap = withContext(Dispatchers.IO) {
    val (width, height) = readImageBounds(context, uri)
    val options = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(width, height, maxDimension)
        inPreferredConfig = Bitmap.Config.ARGB_8888
        inScaled = false
        inMutable = false
    }

    val bitmap = context.contentResolver.openInputStream(uri).use { input ->
        requireNotNull(input) { "Unable to reopen selected image" }
        BitmapFactory.decodeStream(input, null, options)
    }
    requireNotNull(bitmap) { "Android could not decode the selected image" }
}

suspend fun decodeImage(context: Context, uri: Uri): Bitmap = withContext(Dispatchers.IO) {
    val options = BitmapFactory.Options().apply {
        inPreferredConfig = Bitmap.Config.ARGB_8888
        inScaled = false
        inMutable = false
    }
    val bitmap = context.contentResolver.openInputStream(uri).use { input ->
        requireNotNull(input) { "Unable to open selected image" }
        BitmapFactory.decodeStream(input, null, options)
    }
    requireNotNull(bitmap) { "Android could not decode the selected image" }
}

suspend fun savePngToGallery(context: Context, bitmap: Bitmap): Uri = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    val name = "OverlayForge_${System.currentTimeMillis()}.png"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/OverlayForge")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
    try {
        resolver.openOutputStream(uri).use { out ->
            requireNotNull(out)
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out))
        }
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        uri
    } catch (t: Throwable) {
        resolver.delete(uri, null, null)
        throw t
    }
}

suspend fun publishMp4ToGallery(context: Context, sourcePath: String): Uri = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    val values = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, "OverlayForge_${System.currentTimeMillis()}.mp4")
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/OverlayForge")
        put(MediaStore.Video.Media.IS_PENDING, 1)
    }
    val uri = requireNotNull(resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values))
    try {
        resolver.openOutputStream(uri).use { output ->
            requireNotNull(output)
            java.io.File(sourcePath).inputStream().use { input -> input.copyTo(output) }
        }
        values.clear()
        values.put(MediaStore.Video.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        uri
    } catch (t: Throwable) {
        resolver.delete(uri, null, null)
        throw t
    }
}
