package com.k.image.cropview

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.image.cropview.ImageCrop
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
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val listImages = remember { mutableStateOf<List<String>>(emptyList()) }

                    lifecycleScope.launchWhenStarted {
                        viewModel.listOfImages.collectLatest { list ->
                            listImages.value = list
                        }
                    }

                    val context = LocalContext.current as Activity

                    val bitmap = remember {
                        mutableStateOf<Bitmap?>(null)
                    }

                    val showProgressBarState = remember { mutableStateOf(true) }

                    val croppedImage = remember { mutableStateOf<Bitmap?>(null) }


                    LaunchedEffect(true) {
                        launch {
                            bitmap.value = viewModel.getBitmap(context)

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
                                    guideLineWidth = 3.dp,
                                    edgeCircleSize = 10.dp
                                )

                                showProgressBarState.value = false

                            }

                        }

                        //>=> >=>
                        Spacer(modifier = Modifier.height(100.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            Arrangement.SpaceEvenly
                        ) {
                            Button(
                                modifier = Modifier
                                    .height(50.dp)
                                    .weight(2F)
                                    .padding(start = 4.dp, end = 4.dp),
                                onClick = {
                                }
                            ) {
                                Text(
                                    text = "Select Image",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis

                                )
                            }

                            Button(
                                modifier = Modifier
                                    .height(50.dp)
                                    .weight(2F)
                                    .padding(start = 4.dp, end = 4.dp),
                                onClick = {
                                    imageCrop.resetView()
                                }
                            ) {
                                Text(
                                    text = "Reset"
                                )
                            }

                            Button(
                                modifier = Modifier
                                    .height(50.dp)
                                    .weight(2F)
                                    .padding(start = 4.dp, end = 4.dp),
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
//                                        Toast.makeText(context, "Null Image", Toast.LENGTH_SHORT)
//                                            .show()

                                    }
                                }
                            ) {
                                Text(
                                    text = "Save"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(100.dp))


                        //>=> >=>
                        LazyRow(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),

                            ) {
//                            (1..100).forEach { intV ->
//                                item {
//                                    Text(text = intV.toString())
//
//                                }
//                            }
                            listImages.value.forEach { image ->
                                Log.d("IMAGE_FILE", "saveMediaToStorage: image  => $image")

                                item {

                                    AsyncImage(
                                        modifier = Modifier.size(20.dp, 20.dp),
                                        model = image,
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

