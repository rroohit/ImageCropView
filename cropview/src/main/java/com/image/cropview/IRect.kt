package com.image.cropview

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 *  - Data class representing an internal rectangle a crop view.
 *
 *  @property topLeft The top-left offset of the rectangle.
 *  @property size The size of the rectangle.
 */
public data class IRect(
    val topLeft: Offset = Offset(0.0f, 0.0f),
    var size: Size = Size(0.0f, 0.0f)
)

public fun IRect.verticalGuidelineDiff(noOfGuideLines: Int): Float {
    return size.height / (noOfGuideLines + 1)
}

public fun IRect.horizontalGuidelineDiff(noOfGuideLines: Int): Float {
    return size.width / (noOfGuideLines + 1)
}