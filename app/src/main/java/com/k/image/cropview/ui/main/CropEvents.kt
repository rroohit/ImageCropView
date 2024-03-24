package com.k.image.cropview.ui.main

import android.app.Activity
import android.graphics.Bitmap
import com.image.cropview.CropType

sealed class CropEvents {
    data object Initial : CropEvents()
    data class ResetCrop(val isResetCrop: Boolean) : CropEvents()
    data class ShowSnackBar(val message: String) : CropEvents()
    data class SaveCroppedImage(val bitmap: Bitmap, val activity: Activity) : CropEvents()
    data class ChangeCropType(val cropType: CropType) : CropEvents()
}
