package com.k.image.cropview.examples.exampleone

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.image.cropview.CropType
import com.image.cropview.EdgeType
import com.image.cropview.ImageCrop
import com.image.cropview.ImageCropView
import com.image.cropview.rememberSaveableImageCrop
import com.k.image.cropview.R
import com.k.image.cropview.ui.components.ImageItem
import com.k.image.cropview.util.LockScreenOrientation

/**
 * Example One — the simplest, portrait-only usage of the library. Same building blocks as
 * Example Two (crop-type chips, action buttons, bottom-sheet results grid) in a plain vertical
 * layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExampleOneScreen(
    onBack: () -> Unit,
    viewModel: ExampleOneViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Example One demonstrates the simplest, portrait-only usage.
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    LaunchedEffect(Unit) { viewModel.onEvent(ExampleOneEvent.LoadInitialImage) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Example One") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_round_arrow_back),
                            contentDescription = "Back",
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        ExampleOneContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

@Composable
private fun ExampleOneContent(
    uiState: ExampleOneUiState,
    onEvent: (ExampleOneEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var previewImage by remember { mutableStateOf<Bitmap?>(null) }
    var showResults by remember { mutableStateOf(false) }

    // Single, stable saveable slot so crop state survives recreation.
    val imageCrop = uiState.currentImage?.let { rememberSaveableImageCrop(it) }

    Column(modifier = modifier) {
        CropArea(
            imageCrop = imageCrop,
            cropType = uiState.cropType,
            isLoading = uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            CropTypeChips(
                currentCropType = uiState.cropType,
                onCropTypeSelected = { onEvent(ExampleOneEvent.ChangeCropType(it)) },
            )
            Spacer(Modifier.height(12.dp))
            CropActions(
                onCrop = {
                    val cropped = imageCrop?.onCrop()
                    if (cropped != null) {
                        onEvent(ExampleOneEvent.SaveCroppedImage(cropped))
                    } else {
                        Toast.makeText(context, "No image to crop", Toast.LENGTH_SHORT).show()
                    }
                },
                onReset = { imageCrop?.resetView() },
                onChangeImage = {
                    imageCrop?.resetView()
                    onEvent(ExampleOneEvent.ChangeImage)
                },
            )
            Spacer(Modifier.height(12.dp))
            ResultsButton(
                count = uiState.croppedImages.size,
                onClick = { showResults = true },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showResults) {
        ResultsBottomSheet(
            images = uiState.croppedImages,
            onResultClick = { previewImage = it },
            onDismiss = { showResults = false },
        )
    }

    previewImage?.let { image ->
        ImagePreviewDialog(image = image, onDismiss = { previewImage = null })
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Building blocks
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CropArea(
    imageCrop: ImageCrop?,
    cropType: CropType,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (imageCrop != null) {
            ImageCropView(
                imageCrop = imageCrop,
                modifier = Modifier.fillMaxSize(),
                guideLineColor = Color.LightGray,
                guideLineWidth = 2.dp,
                edgeCircleSize = 5.dp,
                showGuideLines = cropType != CropType.PROFILE_CIRCLE,
                cropType = cropType,
                edgeType = EdgeType.CIRCULAR,
                enableZoom = true,
            )
        }
        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}

private data class CropTypeOption(val cropType: CropType, val label: String)

private val cropTypeOptions = listOf(
    CropTypeOption(CropType.FREE_STYLE, "Free"),
    CropTypeOption(CropType.SQUARE, "Square"),
    CropTypeOption(CropType.PROFILE_CIRCLE, "Circle"),
    CropTypeOption(CropType.RATIO_3_2, "3:2"),
    CropTypeOption(CropType.RATIO_4_3, "4:3"),
    CropTypeOption(CropType.RATIO_16_9, "16:9"),
    CropTypeOption(CropType.RATIO_9_16, "9:16"),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CropTypeChips(
    currentCropType: CropType,
    onCropTypeSelected: (CropType) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        cropTypeOptions.forEach { option ->
            FilterChip(
                selected = option.cropType == currentCropType,
                onClick = { onCropTypeSelected(option.cropType) },
                label = { Text(option.label) },
            )
        }
    }
}

@Composable
private fun CropActions(
    onCrop: () -> Unit,
    onReset: () -> Unit,
    onChangeImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(onClick = onChangeImage, modifier = Modifier.weight(1f)) {
            Text("Change", maxLines = 1)
        }
        OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) { Text("Reset") }
        Button(onClick = onCrop, modifier = Modifier.weight(1f)) { Text("Crop") }
    }
}

@Composable
private fun ResultsButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = count > 0,
        modifier = modifier,
    ) {
        Text(if (count > 0) "View results ($count)" else "No results yet")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultsBottomSheet(
    images: List<Bitmap>,
    onResultClick: (Bitmap) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Cropped images (${images.size})",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(12.dp))

            if (images.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No cropped images yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    itemsIndexed(images) { index, image ->
                        ImageItem(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(2.dp)),
                            bitmap = image,
                            index = index,
                            onClick = onResultClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImagePreviewDialog(
    image: Bitmap,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                model = image,
                contentDescription = "cropped image",
            )
        }
    }
}