package com.k.image.cropview.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult

/**
 * Loads sample images from the network (via Coil) for the demo screens.
 *
 * Bitmaps are cached by URL so the same [Bitmap] instance is reused across configuration
 * changes — this keeps the crop state (which is keyed on the bitmap) stable across rotation.
 *
 * This is sample infrastructure shared by all examples; it is not part of the library.
 */
class ImageRepository {

    private val bitmapCache = mutableMapOf<String, Bitmap>()
    private var lastUrl = ""

    val imageUrls = listOf(
        "https://cdn.pixabay.com/photo/2025/03/06/09/08/woman-9450191_1280.jpg",
        "https://images.unsplash.com/photo-1572782252655-9c8771392601?q=80&w=2112&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
        "https://images.unsplash.com/photo-1698417386582-5e7a652254f5?q=80&w=2859&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
        "https://images.unsplash.com/photo-1641369330370-28260e386e0f?q=80&w=3387&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
    )

    /** Loads a random image (different from the previous one) and returns it as a [Bitmap]. */
    suspend fun loadRandomImage(context: Context): Bitmap? {
        lastUrl = randomUrl()
        return loadImage(context, lastUrl)
    }

    private suspend fun loadImage(context: Context, imageUrl: String): Bitmap? {
        bitmapCache[imageUrl]?.let { return it }

        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()

        val result = (loader.execute(request) as SuccessResult).drawable
        val bitmap = (result as BitmapDrawable).bitmap

        bitmapCache[imageUrl] = bitmap
        return bitmap
    }

    private fun randomUrl(): String {
        var newUrl = imageUrls.random()
        while (newUrl == lastUrl) {
            newUrl = imageUrls.random()
        }
        return newUrl
    }
}