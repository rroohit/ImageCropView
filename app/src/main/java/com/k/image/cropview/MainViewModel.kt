package com.k.image.cropview

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class MainViewModel : ViewModel() {

    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps: StateFlow<List<Bitmap>> = _bitmaps


    companion object {
        const val IMAGE_URL =
            "https://images.unsplash.com/photo-1619203596659-6029850f0c73?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80"
    }


    fun onEvent(events: CropEvents) {
        when (events) {
            is CropEvents.ResetCrop -> {

            }
            is CropEvents.SaveCroppedImage -> {
                viewModelScope.launch {
                    saveMediaToStorage(events.bitmap, events.activity)
                }


            }
            else -> Unit
        }
    }

    private fun saveMediaToStorage(bitmap: Bitmap, activity: Activity) {
        _bitmaps.value = bitmaps.value + bitmap
        //Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        //Output stream
        var fos: OutputStream? = null

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            activity.contentResolver?.also { resolver ->

                //Content resolver will process the content values
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an output stream with the Uri that we got
                fos = imageUri?.let {
                    resolver.openOutputStream(it)
                }

            }
        } else {
            //These for devices running on android < Q
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)

        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)

//            Toast.makeText(activity, "Image Saved", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getBitmap(context: Context): Bitmap? {
        val loading = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(IMAGE_URL)
            .build()

        val result = (loading.execute(request) as SuccessResult).drawable

        return (result as BitmapDrawable).bitmap
    }


}