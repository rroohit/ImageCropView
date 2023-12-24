package com.k.image.cropview.ui

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun ImageItem(
    modifier: Modifier = Modifier,
    bitmap: Bitmap,
    index: Int,
    onClick: (Bitmap) -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    onClick(bitmap)
                },
            model = bitmap,
            contentDescription = "cropped image"
        )

        Row(
            modifier = Modifier.fillMaxSize().padding(start = 60.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                modifier = Modifier
                    .size(14.dp)
                    .background(Color.Gray, RoundedCornerShape(50)),
                text = "${index + 1}",
                style = TextStyle(
                    fontSize = 12.sp,
                ),
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
        }
    }
}