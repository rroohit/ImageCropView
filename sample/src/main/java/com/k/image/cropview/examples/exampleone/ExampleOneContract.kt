package com.k.image.cropview.examples.exampleone

import android.graphics.Bitmap
import com.image.cropview.CropType

/**
 * UI state rendered by [ExampleOneScreen].
 *
 * Note: the interactive [com.image.cropview.ImageCrop] state holder is NOT kept here — it lives in
 * the composition (via `rememberSaveableImageCrop`) because it is Compose UI state. This holds only
 * data the ViewModel owns.
 */
data class ExampleOneUiState(
    val currentImage: Bitmap? = null,
    val croppedImages: List<Bitmap> = emptyList(),
    val cropType: CropType = CropType.FREE_STYLE,
    val isLoading: Boolean = false,
)

/** User intents emitted from [ExampleOneScreen]. */
sealed interface ExampleOneEvent {
    /** Load the first image if one is not already loaded. */
    data object LoadInitialImage : ExampleOneEvent

    /** Load a fresh random image. */
    data object ChangeImage : ExampleOneEvent

    /** Change the active crop type. */
    data class ChangeCropType(val cropType: CropType) : ExampleOneEvent

    /** Persist a cropped bitmap and add it to the gallery row. */
    data class SaveCroppedImage(val bitmap: Bitmap) : ExampleOneEvent
}