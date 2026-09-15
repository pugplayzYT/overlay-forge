package com.pugplayz.overlayforge.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PhotoImportTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun importedPng_isReadAndDecodedWithoutCrashing() {
        runBlocking {
            val file = makePhotoFile("photo-import-test.png", Bitmap.CompressFormat.PNG, 1600, 900)
            val uri = Uri.fromFile(file)

            val info = readMediaInfo(context, uri)
            val preview = decodeImagePreview(context, uri, maxDimension = 512)

            assertFalse(info.isVideo)
            assertEquals(1600, info.width)
            assertEquals(900, info.height)
            assertTrue(preview.width > 0)
            assertTrue(preview.height > 0)
            assertTrue(maxOf(preview.width, preview.height) <= 800)

            preview.recycle()
            file.delete()
        }
    }

    @Test
    fun importedJpeg_isReadAndDecodedWithoutCrashing() {
        runBlocking {
            val file = makePhotoFile("photo-import-test.jpg", Bitmap.CompressFormat.JPEG, 4032, 3024)
            val uri = Uri.fromFile(file)

            val info = readMediaInfo(context, uri)
            val preview = decodeImagePreview(context, uri, maxDimension = 1024)

            assertFalse(info.isVideo)
            assertEquals(4032, info.width)
            assertEquals(3024, info.height)
            assertTrue(maxOf(preview.width, preview.height) <= 2016)

            preview.recycle()
            file.delete()
        }
    }

    @Test
    fun invalidPhoto_failsCleanlyInsteadOfReturningBogusDimensions() {
        val file = File(context.cacheDir, "not-an-image.jpg")
        file.writeText("this is not image data")
        val uri = Uri.fromFile(file)

        val result = runCatching { readImageBounds(context, uri) }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        file.delete()
    }

    @Test
    fun samplingKeepsLargePhotosOutOfFullResolutionPreview() {
        assertEquals(4, calculateInSampleSize(12000, 9000, 2048))
        assertEquals(1, calculateInSampleSize(1920, 1080, 2048))
    }

    private fun makePhotoFile(
        name: String,
        format: Bitmap.CompressFormat,
        width: Int,
        height: Int
    ): File {
        val file = File(context.cacheDir, name)
        val source = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(32, 180, 220))
        }
        file.outputStream().use { output ->
            assertTrue(source.compress(format, 92, output))
        }
        source.recycle()
        return file
    }
}
