package com.image.cropview

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlin.math.abs

// Scale given bitmap to given canvas size and draw the scaled bitmap on canvas draw scope
/**
 * Extension function to draw a Bitmap onto a DrawScope, scaling it to fit the canvas.
 *
 * This improved version handles cases where the bitmap doesn't need scaling, avoids unnecessary allocations,
 * uses a more robust scaling method, and adds logging for potential issues.
 *
 * @param bitmap The Bitmap to draw.
 * @param canvasSize The target size (width and height) of the canvas.
 *
 */
public fun DrawScope.drawBitmap(bitmap: Bitmap, canvasSize: CanvasSize) {
    val targetWidth = canvasSize.width.toInt()
    val targetHeight = canvasSize.height.toInt()

    // Check if scaling is necessary.
    if (bitmap.width == targetWidth && bitmap.height == targetHeight) {
        // No scaling needed, draw directly.
        drawImage(bitmap.asImageBitmap())
        return
    }

    // Check if canvas size is valid.
    if (canvasSize.width <= 0 || canvasSize.height <= 0) {
        return
    }

    // Calculate scaling factors
    val scaleX = targetWidth.toFloat() / bitmap.width
    val scaleY = targetHeight.toFloat() / bitmap.height

    // Create a matrix for scaling
    val matrix = Matrix().apply {
        postScale(scaleX, scaleY)
    }

    // Create a new bitmap with the target dimensions and apply the scaling
    val scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    try{
        // Draw the scaled bitmap onto the canvas.
        drawImage(scaledBitmap.asImageBitmap())
    } catch (e: IllegalArgumentException){
        //Log.e("drawScaledBitmap", "Error scaling and drawing bitmap: ${e.message}")
    } finally {
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
            //Log.d("drawScaledBitmap", "Scaled bitmap recycled.")
        }
    }
}

/*public fun DrawScope.drawBitmap(bitmap: Bitmap, canvasSize: CanvasSize) {
    val mBitmap = bitmap.scale(
        canvasSize.width.toInt(),
        canvasSize.height.toInt(),
        false
    )
    drawImage(mBitmap.asImageBitmap())
}*/

// Draw the bordered rectangle view
public fun DrawScope.drawCropRectangleView(
    guideLineColor: Color,
    guideLineWidth: Dp = 2.dp,
    iRect: IRect
) {
    drawRect(
        color = guideLineColor,
        topLeft = iRect.topLeft,
        size = iRect.size,
        style = Stroke(guideLineWidth.toPx())
    )
}

// Draw Vertical and Horizontal lines
public fun DrawScope.drawGuideLines(
    noOfGuideLines: Int = 2,
    guideLineColor: Color,
    guideLineWidth: Dp = 2.dp,
    iRect: IRect
) {
    // Vertical lines
    val verticalGuidelineDiff = iRect.verticalGuidelineDiff(noOfGuideLines)
    val horizontalGuidelineDiff = iRect.horizontalGuidelineDiff(noOfGuideLines)
    val topLeft = iRect.topLeft

    for (i in 1..noOfGuideLines) {
        // Vertical Line
        drawLine(
            color = guideLineColor,
            start = Offset(topLeft.x, topLeft.y + (verticalGuidelineDiff * i)),
            end = Offset(topLeft.x + iRect.size.width, topLeft.y + (verticalGuidelineDiff * i)),
            strokeWidth = guideLineWidth.toPx(),
        )

        // Horizontal Line
        drawLine(
            color = guideLineColor,
            start = Offset(topLeft.x + (horizontalGuidelineDiff * i), iRect.topLeft.y),
            end = Offset(topLeft.x + (horizontalGuidelineDiff * i), topLeft.y + iRect.size.height),
            strokeWidth = guideLineWidth.toPx(),
        )
    }
}

// Draw circular edge on crop view rect
public fun DrawScope.drawCircularEdges(
    edgeCircleSize: Dp = 8.dp,
    guideLineColor: Color,
    iRect: IRect
) {
    val topLeft = iRect.topLeft
    // edge 1 - Topleft
    drawCircle(
        color = guideLineColor,
        center = topLeft,
        radius = edgeCircleSize.toPx()
    )

    // edge 2 - TopRight
    drawCircle(
        color = guideLineColor,
        center = Offset((topLeft.x + iRect.size.width), topLeft.y),
        radius = edgeCircleSize.toPx()
    )

    // edge 3 - BottomLeft
    drawCircle(
        color = guideLineColor,
        center = Offset(topLeft.x, (topLeft.y + iRect.size.height)),
        radius = edgeCircleSize.toPx()
    )

    // edge 4 - BottomRight
    drawCircle(
        color = guideLineColor,
        center = Offset((topLeft.x + iRect.size.width), (topLeft.y + iRect.size.height)),
        radius = edgeCircleSize.toPx()
    )
}

public fun DrawScope.drawSquareBrackets(
    guideLineColor: Color,
    guideLineWidthGiven: Dp = 2.dp,
    iRect: IRect
) {
    val guideLineWidth = min(guideLineWidthGiven, 2.dp)
    val topLeft = iRect.topLeft
    val iRectWidth = (topLeft.x + iRect.size.width)
    val iRectHeight = (topLeft.y + iRect.size.height)
    val lineLength = 45F
    val strokeWidth = (guideLineWidth.toPx() * 1.3F)
    val halfStrokeWidth = (strokeWidth / 2F)
    val guideLineWidthInPx = guideLineWidth.toPx()

    val yTopLine = (topLeft.y - guideLineWidthInPx)
    val xLeftLine = (topLeft.x - guideLineWidthInPx)
    val xRightLine = (iRectWidth + guideLineWidthInPx)
    val yBottomLine = (iRectHeight + guideLineWidthInPx)
    // TopLeft Corner
    drawLine(
        start = Offset((xLeftLine - (halfStrokeWidth)), yTopLine),
        end = Offset((xLeftLine + lineLength + halfStrokeWidth), yTopLine),
        color = guideLineColor,
        strokeWidth = strokeWidth,
    )
    drawLine(
        start = Offset(xLeftLine, yTopLine - (halfStrokeWidth)),
        end = Offset(xLeftLine, yTopLine + lineLength + halfStrokeWidth),
        color = guideLineColor,
        strokeWidth = strokeWidth,
    )

    // topRight Corner
    val tpXStart = iRectWidth - abs(lineLength - strokeWidth) // top x start line point
    val tpXEnd = iRectWidth + (halfStrokeWidth + guideLineWidthInPx) // top x end line point
    drawLine(
        start = Offset(tpXStart, yTopLine),
        end = Offset(tpXEnd, yTopLine),
        color = guideLineColor,
        strokeWidth = strokeWidth,
    )
    drawLine(
        start = Offset(xRightLine, yTopLine - (halfStrokeWidth)),
        end = Offset(xRightLine, yTopLine + lineLength + halfStrokeWidth),
        color = guideLineColor,
        strokeWidth = strokeWidth
    )

    // BottomLeft Corner
    drawLine(
        start = Offset((xLeftLine - (halfStrokeWidth)), yBottomLine),
        end = Offset((xLeftLine + lineLength + halfStrokeWidth), yBottomLine),
        color = guideLineColor,
        strokeWidth = strokeWidth
    )
    drawLine(
        start = Offset(xLeftLine, iRectHeight - abs(lineLength - strokeWidth)),
        end = Offset(xLeftLine, (iRectHeight + guideLineWidthInPx + halfStrokeWidth)),
        color = guideLineColor,
        strokeWidth = strokeWidth
    )

    // BottomRight Corner
    drawLine(
        start = Offset(iRectWidth - abs(lineLength - strokeWidth), yBottomLine),
        end = Offset(iRectWidth + strokeWidth, yBottomLine),
        color = guideLineColor,
        strokeWidth = strokeWidth
    )
    drawLine(
        start = Offset(xRightLine, iRectHeight - abs(lineLength - strokeWidth)),
        end = Offset(xRightLine, iRectHeight + halfStrokeWidth + guideLineWidthInPx),
        color = guideLineColor,
        strokeWidth = strokeWidth
    )

    // Centers
    val xHorizontalCenter = (topLeft.x + (iRect.horizontalGuidelineDiff(1)))
    val halfLineLength = (lineLength / 2F) + strokeWidth
    drawLine(
        start = Offset(xHorizontalCenter - halfLineLength, yTopLine),
        end = Offset(xHorizontalCenter + halfLineLength, yTopLine),
        color = guideLineColor,
        strokeWidth = strokeWidth,
    )
    drawLine(
        start = Offset(xHorizontalCenter - halfLineLength, yBottomLine),
        end = Offset(xHorizontalCenter + halfLineLength, yBottomLine),
        color = guideLineColor,
        strokeWidth = strokeWidth,
    )

    val xVerticalCenter = (topLeft.y + (iRect.verticalGuidelineDiff(1)))
    drawLine(
        start = Offset(xLeftLine, xVerticalCenter - halfLineLength),
        end = Offset(xLeftLine, xVerticalCenter + halfLineLength),
        color = guideLineColor,
        strokeWidth = strokeWidth,
    )
    drawLine(
        start = Offset(xRightLine, xVerticalCenter - halfLineLength),
        end = Offset(xRightLine, xVerticalCenter + halfLineLength),
        color = guideLineColor,
        strokeWidth = strokeWidth,
    )


}
