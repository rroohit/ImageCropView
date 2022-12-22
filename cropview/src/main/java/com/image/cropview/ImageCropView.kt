package com.image.cropview

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset

/**
 *  ImageCropView which is responsible to draw all rect and edges with bitmap of an image in it
 *  @param bitmapImage Bitmap which has to cropped
 *
 */

class ImageCropView(
    bitmapImage: Bitmap
) : OnCrop {



    override fun resetView() {

    }

    override fun updateCropPoints(
        circleOne: Offset,
        circleTwo: Offset,
        circleThree: Offset,
        circleFour: Offset
    ) {

    }


}

interface OnCrop {
    fun resetView()
    fun updateCropPoints(
        circleOne: Offset,
        circleTwo: Offset,
        circleThree: Offset,
        circleFour: Offset
    )
}