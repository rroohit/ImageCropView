package com.image.cropview

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset

/**
 *  ImageCrop which is responsible to draw all rect and edges on bitmap
 *  @param bitmapImage Bitmap which has to cropped
 *
 */
class ImageCrop(
    var bitmapImage: Bitmap
) : OnCrop {

    val cropUtil = CropUtil(bitmapImage)

    @Composable
    fun ImageCropView() {

    }

    override fun resetView() {
        cropUtil.resetCropRect()
    }

}

interface OnCrop {
    fun resetView()

}