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
    fun importedPng_isReadAndDecodedWithoutCrashing() = runBlocking {
        val file = File(context.cacheDir, "photo-import-test.png")
        val source = Bitmap.createBitmap(1600, 900, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(32, 180, 220))
        }
        file.outputStream().use { output ->
            assertTrue(source.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
        source.recycle()

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
}
