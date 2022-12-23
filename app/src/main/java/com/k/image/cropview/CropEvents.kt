package com.k.image.cropview

import android.app.Activity
import android.graphics.Bitmap

sealed class CropEvents {
    object Initial : CropEvents()
    data class ResetCrop(val isResetCrop: Boolean) : CropEvents()
    data class ShowSnackBar(val message: String) : CropEvents()
    data class SaveCroppedImage(val bitmap: Bitmap, val activity: Activity) : CropEvents()
}
