package com.image.cropview

import androidx.compose.ui.geometry.Offset

/**
 *  Points data of lines which has to be draw on rectangle and has to be update whenever dragged/changed points
 *
 */
public data class GuideLinePoints(
    val start: Offset = Offset(0.0f, 0.0f),
    val end: Offset = Offset(0.0f, 0.0f)
)
