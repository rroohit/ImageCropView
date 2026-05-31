package com.k.image.cropview.examples.exampletwo

import android.graphics.Bitmap
import com.image.cropview.CropType

/** UI state rendered by [ExampleTwoScreen]. */
data class ExampleTwoUiState(
    val currentImage: Bitmap? = null,
    val croppedImages: List<Bitmap> = emptyList(),
    val cropType: CropType = CropType.FREE_STYLE,
    val isLoading: Boolean = false,
)

/** User intents emitted from [ExampleTwoScreen]. */
sealed interface ExampleTwoEvent {
    data object LoadInitialImage : ExampleTwoEvent
    data object ChangeImage : ExampleTwoEvent
    data class ChangeCropType(val cropType: CropType) : ExampleTwoEvent
    data class SaveCroppedImage(val bitmap: Bitmap) : ExampleTwoEvent
}