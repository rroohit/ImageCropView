package com.k.image.cropview.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.image.cropview.CropType

@Composable
fun ItemIcon(
    painter: Painter,
    description: String?,
    cropType: CropType,
    currentCropType: CropType,
    onClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .size(30.dp)
                .clickable { onClick() },
            painter = painter,
            contentDescription = description,
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground)
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (currentCropType == cropType) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFF10F019), Color(0x834CAF50))
                        ),
                        shape = CircleShape
                    )
            )

        } else {
            Box(modifier = Modifier.size(8.dp))
        }

    }

}