package com.k.image.cropview.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Persists cropped bitmaps to the device's Pictures directory via [MediaStore].
 *
 * This is sample infrastructure shared by all examples; it is not part of the library.
 */
class ImageStorage {

    /** Saves [bitmap] as a JPEG into the public Pictures directory. */
    fun saveToPictures(context: Context, bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
                )
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            @Suppress("DEPRECATION")
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            fos = FileOutputStream(File(imagesDir, filename))
        }

        fos?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
    }
}