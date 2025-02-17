package com.k.image.cropview.ui.main

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import com.image.cropview.CropType
import com.image.cropview.EdgeType
import com.image.cropview.ImageCrop
import com.k.image.cropview.R
import com.k.image.cropview.ui.components.ImageItem
import com.k.image.cropview.ui.components.ItemIcon
import com.k.image.cropview.ui.theme.ImageCropViewTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var imageCrop: ImageCrop

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel by viewModels()
            ImageCropViewTheme {
                val context = LocalContext.current as Activity
                val showProgressBarState = remember { mutableStateOf(true) }
                val showImageDialog = remember { mutableStateOf(false) }
                val croppedImage = remember { mutableStateOf<Bitmap?>(null) }
                val selectedImage = remember { mutableStateOf<Bitmap?>(null) }
                viewModel.getBitmapFromUrl(context)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val listImages = remember { mutableStateOf<List<Bitmap>>(emptyList()) }

                    LaunchedEffect(true) {
                        lifecycleScope.launch {
                            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                viewModel.bitmaps.collectLatest { list ->
                                    listImages.value = list
                                }
                            }
                        }
                    }

                    val bitmap = remember {
                        mutableStateOf<Bitmap?>(null)
                    }

                    LaunchedEffect(true) {
                        lifecycleScope.launch {
                            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                viewModel.currImage.collectLatest { curImage ->
                                    bitmap.value = curImage
                                }
                            }
                        }
                    }


                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .fillMaxHeight(0.6f)
                                .padding(
                                    start = 25.dp,
                                    top = 25.dp,
                                    end = 25.dp,
                                    bottom = 0.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (showProgressBarState.value) {
                                CircularProgressIndicator()
                            }

                            bitmap.value?.let { bm ->
                                imageCrop = ImageCrop(bitmapImage = bm)
                                imageCrop.ImageCropView(
                                    modifier = Modifier.fillMaxSize(),
                                    guideLineColor = Color.LightGray,
                                    guideLineWidth = 2.dp,
                                    edgeCircleSize = 5.dp,
                                    showGuideLines = viewModel.cropType.collectAsState().value != CropType.PROFILE_CIRCLE,
                                    // showGuideLines = true,
                                    cropType = viewModel.cropType.collectAsState().value,
                                    edgeType = EdgeType.CIRCULAR
                                )

                                showProgressBarState.value = false
                            }
                        }

                        //>=> >=>
                        Row(
                            modifier = Modifier
                                .height(80.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            ItemIcon(
                                painterResource(id = R.drawable.ratio_free_style),
                                description = "Free Style",
                                cropType = CropType.FREE_STYLE,
                                currentCropType = viewModel.cropType.collectAsState().value
                            ) {
                                viewModel.onEvent(CropEvents.ChangeCropType(CropType.FREE_STYLE))
                            }

                            Spacer(modifier = Modifier.width(24.dp))

                            ItemIcon(
                                painter = painterResource(id = R.drawable.ratio_square),
                                description = "Square",
                                cropType = CropType.SQUARE,
                                currentCropType = viewModel.cropType.collectAsState().value
                            ) {
                                viewModel.onEvent(CropEvents.ChangeCropType(CropType.SQUARE))
                            }

                            Spacer(modifier = Modifier.width(24.dp))

                            ItemIcon(
                                painter = painterResource(id = R.drawable.ratio_profile_crop),
                                description = "ProfileCircle",
                                cropType = CropType.PROFILE_CIRCLE,
                                currentCropType = viewModel.cropType.collectAsState().value
                            ) {
                                viewModel.onEvent(CropEvents.ChangeCropType(CropType.PROFILE_CIRCLE))
                            }

                        }

                        //>=> >=>
                        // Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                modifier = Modifier
                                    .height(40.dp)
                                    .weight(2F)
                                    .padding(start = 2.dp, end = 2.dp),
                                onClick = {
                                    showProgressBarState.value = true
                                    imageCrop.resetView()
                                    viewModel.getBitmapFromUrl(context)
                                }
                            ) {
                                Text(
                                    text = "Change Image",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                modifier = Modifier
                                    .height(40.dp)
                                    .weight(2F)
                                    .padding(start = 2.dp, end = 2.dp),
                                onClick = {
                                    imageCrop.resetView()
                                }
                            ) {
                                Text(
                                    text = "Reset",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                modifier = Modifier
                                    .height(40.dp)
                                    .weight(2F)
                                    .padding(start = 2.dp, end = 2.dp),
                                onClick = {
                                    val b = imageCrop.onCrop()
                                    croppedImage.value = b
                                    croppedImage.value?.let { bm ->
                                        viewModel.onEvent(
                                            CropEvents.SaveCroppedImage(
                                                bm,
                                                context
                                            )
                                        )
                                    } ?: run {
                                        Toast.makeText(context, "Null Image", Toast.LENGTH_SHORT)
                                            .show()

                                    }
                                }
                            ) {
                                Text(
                                    text = "CropImage",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        //>=> >=>
                        // Cropped images
                        LazyRow(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 8.dp, end = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            listImages.value.forEachIndexed { index, image ->
                                item {
                                    ImageItem(modifier = Modifier
                                        .fillMaxSize(0.6F)
                                        .clip(RoundedCornerShape(4.dp)),
                                        bitmap = image,
                                        index = index,
                                        onClick = {
                                            selectedImage.value = it
                                            showImageDialog.value = true
                                        }
                                    )
                                }
                            }

                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // ==>
                    if (showImageDialog.value) {
                        Dialog(
                            onDismissRequest = {
                                showImageDialog.value = false
                                selectedImage.value = null
                            }
                        ) {
                            Surface(
                                color = Color.Transparent,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            showImageDialog.value = false
                                            selectedImage.value = null
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.TopEnd)
                                            .padding(top = 30.dp),
                                        horizontalArrangement = Arrangement.End,

                                        ) {
                                        Icon(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clickable {
                                                    showImageDialog.value = false
                                                    selectedImage.value = null
                                                },
                                            imageVector = Icons.Rounded.Close,
                                            tint = Color.White,
                                            contentDescription = "close dialog"
                                        )
                                    }

                                    AsyncImage(
                                        modifier = Modifier
                                            .fillMaxSize(1f)
                                            .padding(bottom = 100.dp),
                                        model = selectedImage.value,
                                        contentDescription = "cropped image"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

