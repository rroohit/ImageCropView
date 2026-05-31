package com.k.image.cropview.examples.exampleone

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
 * ViewModel for [ExampleOneScreen]. Owns the screen's data state — current image, the list of
 * saved crops, and the active crop type — and delegates image loading/saving to the data layer.
 */
class ExampleOneViewModel(application: Application) : AndroidViewModel(application) {

    private val imageRepository = ImageRepository()
    private val imageStorage = ImageStorage()

    private val _uiState = MutableStateFlow(ExampleOneUiState())
    val uiState: StateFlow<ExampleOneUiState> = _uiState.asStateFlow()

    fun onEvent(event: ExampleOneEvent) {
        when (event) {
            ExampleOneEvent.LoadInitialImage ->
                if (_uiState.value.currentImage == null) loadImage()

            ExampleOneEvent.ChangeImage -> loadImage()

            is ExampleOneEvent.ChangeCropType ->
                _uiState.update { it.copy(cropType = event.cropType) }

            is ExampleOneEvent.SaveCroppedImage -> saveCroppedImage(event.bitmap)
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