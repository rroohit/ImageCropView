package com.k.image.cropview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.k.image.cropview.navigation.SampleNavHost
import com.k.image.cropview.ui.theme.ImageCropViewTheme

/**
 * Single host Activity for the sample app. All UI is driven by Compose navigation — see
 * [SampleNavHost] for the example registry.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageCropViewTheme {
                SampleNavHost()
            }
        }
    }
}