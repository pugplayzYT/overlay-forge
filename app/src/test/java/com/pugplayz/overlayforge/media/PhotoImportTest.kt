package com.pugplayz.overlayforge.media

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
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
    fun pickedJpegContentUri_isReadAndDecodedWithoutCrashing() {
        runBlocking {
            val file = makePhotoFile("picked-photo.jpg", Bitmap.CompressFormat.JPEG, 4032, 3024)
            val provider = TestPhotoProvider().apply { backingFile = file }
            provider.attachInfo(
                context,
                ProviderInfo().apply {
                    authority = TEST_AUTHORITY
                    exported = false
                    grantUriPermissions = true
                }
            )
            ShadowContentResolver.registerProviderInternal(TEST_AUTHORITY, provider)
            val uri = Uri.parse("content://$TEST_AUTHORITY/picked-photo.jpg")

            assertEquals("image/jpeg", context.contentResolver.getType(uri))
            val info = readMediaInfo(context, uri)
            val preview = decodeImagePreview(context, uri, maxDimension = 1024)

            assertFalse(info.isVideo)
            assertEquals(4032, info.width)
            assertEquals(3024, info.height)
            assertTrue(preview.width > 0)
            assertTrue(preview.height > 0)
            assertTrue(maxOf(preview.width, preview.height) <= 2016)

            preview.recycle()
            file.delete()
        }
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

    companion object {
        private const val TEST_AUTHORITY = "com.pugplayz.overlayforge.test.photos"
    }
}

private class TestPhotoProvider : ContentProvider() {
    lateinit var backingFile: File

    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String = "image/jpeg"

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor =
        ParcelFileDescriptor.open(backingFile, ParcelFileDescriptor.MODE_READ_ONLY)

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
