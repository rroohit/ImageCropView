package com.k.image.cropview.examples.exampletwo

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.k.image.cropview.data.ImageRepository
import com.k.image.cropview.data.ImageStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for [ExampleTwoScreen].
 *
 * The ViewModel instance is retained across configuration changes (rotation), and the repository's
 * bitmap cache keeps the same [Bitmap] instance for a given URL — so the loaded image is identical
 * before and after a rotation. Combined with `rememberSaveableImageCrop` in the screen, this is what
 * keeps the crop rectangle, zoom, and pan stable when the device rotates.
 */
class ExampleTwoViewModel(application: Application) : AndroidViewModel(application) {

    private val imageRepository = ImageRepository()
    private val imageStorage = ImageStorage()

    private val _uiState = MutableStateFlow(ExampleTwoUiState())
    val uiState: StateFlow<ExampleTwoUiState> = _uiState.asStateFlow()

    fun onEvent(event: ExampleTwoEvent) {
        when (event) {
            ExampleTwoEvent.LoadInitialImage ->
                if (_uiState.value.currentImage == null) loadImage()

            ExampleTwoEvent.ChangeImage -> loadImage()

            is ExampleTwoEvent.ChangeCropType ->
                _uiState.update { it.copy(cropType = event.cropType) }

            is ExampleTwoEvent.SaveCroppedImage -> saveCroppedImage(event.bitmap)
        }
    }

    private fun loadImage() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val bitmap = imageRepository.loadRandomImage(getApplication())
            _uiState.update { it.copy(currentImage = bitmap, isLoading = false) }
        }
    }

    private fun saveCroppedImage(bitmap: Bitmap) {
        _uiState.update { it.copy(croppedImages = it.croppedImages + bitmap) }
        viewModelScope.launch {
            imageStorage.saveToPictures(getApplication(), bitmap)
        }
    }
}