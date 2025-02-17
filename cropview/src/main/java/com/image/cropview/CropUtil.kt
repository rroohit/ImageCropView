@file:Suppress("RedundantConstructorKeyword")

package com.image.cropview

import android.graphics.Bitmap
import android.graphics.PointF
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 *  - Utility class for performing cropping operations on a provided bitmap image.
 *      Instances of this class enable the manipulation of an internal rectangle within
 *      the provided image, facilitating cropping and resizing functionalities.
 *
 *  @property bitmapImage The bitmap image to be cropped and manipulated.
 *
 *  @property canvasSize The size of the canvas representing the crop view.
 *
 *  @property iRect The internal rectangle (iRect) representing the region to be cropped.
 *
 *  @constructor Creates a CropUtil instance with the provided bitmap image.
 */
public class CropUtil constructor(private var mBitmapImage: Bitmap) {

    public var cropType: CropType? = null
    private var bitmapImage: Bitmap? = mBitmapImage

    /**
     * The canvas size of the crop view.
     */
    public var canvasSize: CanvasSize by mutableStateOf(CanvasSize())

    /**
     * The internal rectangle (iRect) representing the region to be cropped.
     */
    public var iRect: IRect by mutableStateOf(IRect())

    /**
     * The touch rectangle region used detecting outside iRect to be moved while dragged touched inside this rect.
     */
    private var touchRect: IRect by mutableStateOf(IRect())

    /**
     * The state indicating whether the touch input is inside the touch rectangle for moving the rectangle.
     */
    private var isTouchedInsideRectMove: Boolean by mutableStateOf(false)

    /**
     * The edge of the rectangle touched to drag and resize it.
     */
    private var rectEdgeTouched: RectEdge by mutableStateOf(RectEdge.NULL)

    /**
     * The top-left offset of the rectangle (iRect).
     */
    private var irectTopleft: Offset by mutableStateOf(Offset(0.0f, 0.0f))

    /**
     * The top-left offset of the touch rectangle.
     */
    private var touchAreaRectTopLeft: Offset by mutableStateOf(Offset(0.0f, 0.0f))

    /**
     * The padding inside the internal rectangle for the touch rectangle.
     */
    private val paddingForTouchRect = 70F

    /**
     * The minimum limit for various calculations based on the touch rectangle padding.
     */
    private val minLimit: Float = paddingForTouchRect * 3F

    private var maxSquareLimit: Float = 0F
        set(value) {
            minSquareLimit = value * 0.2F
            field = value
        }

    private var minSquareLimit: Float = maxSquareLimit * 0.3F

    /**
     * The last point updated during drag operations.
     */
    private var lastPointUpdated: Offset? = null

    /**
     * Initializes the crop view by resetting the iRect rectangle.
     */
    init {
        resetCropIRect()
    }

    /**
     *  - Handles the event when the canvas size is changed.
     *      Updates the internal canvasSize property with the new dimensions and
     *      resets the crop rectangle (iRect) accordingly.
     *
     *  @param intSize The new size of the canvas as an [IntSize] object.
     */
    public fun onCanvasSizeChanged(intSize: IntSize) {
        canvasSize = CanvasSize(intSize.width.toFloat(), intSize.height.toFloat())
        resetCropIRect()
    }

    /**
     *  - Resets the internal rectangle (iRect) used for cropping to the full canvas size.
     *      This function sets the iRect's top-left corner to (0,0) and its size to match the canvas size.
     */
    public fun resetCropIRect() {
        // Irect resetting
        val canWidth = canvasSize.width
        val canHeight = canvasSize.height

        if (getCurrCropType() == CropType.SQUARE ||
            getCurrCropType() == CropType.PROFILE_CIRCLE
        ) {
            // Square style Rect positioning
            val squareSize = getSquareSize(canWidth, canHeight)
            irectTopleft = getSquarePosition(canWidth, canHeight, squareSize.width)
            iRect = IRect(topLeft = irectTopleft, size = squareSize)

        } else { // For free style crop
            // Free style Rect positioning
            irectTopleft = Offset(x = 0.0F, y = 0.0F)
            iRect = IRect(topLeft = irectTopleft, size = Size(canWidth, canHeight))
        }

        updateTouchRect()
    }

    private fun getSquareSize(width: Float, height: Float): Size {
        val squareSize = minOf(width, height) - 100F
        maxSquareLimit = squareSize + 100F
        return Size(squareSize, squareSize)
    }

    private fun getSquarePosition(width: Float, height: Float, squareSize: Float): Offset {
        val x = (width - squareSize) / 2
        val y = (height - squareSize) / 2
        return Offset(x, y)
    }

    /**
     *  - Updates the touch rectangle (touchRect) based on the current iRect and given padding.
     *      The touch rectangle is adjusted to fit within the iRect with additional padding.
     *      This function is typically called after resetting the iRect,
     *      And also after any changes respectively with iRect changes.
     */
    private fun updateTouchRect() {
        // Touch rect resetting
        val size = iRect.size
        val insidePadding = (paddingForTouchRect * 2)
        val touchRectTopleft = Offset(
            x = (irectTopleft.x + paddingForTouchRect),
            y = (irectTopleft.y + paddingForTouchRect)
        )
        touchRect = IRect(
            topLeft = touchRectTopleft,
            size = Size(
                width = (size.width - (insidePadding)),
                height = (size.height - (insidePadding))
            )
        )
    }

    /**
     *  - Handles the initial touch event when starting a drag operation.
     *      Determines if the touch input is inside the touch rectangle, identifies the touched
     *      edge of the rectangle, and updates the last touch point for tracking movement.
     *
     *  @param touchPoint The coordinates of the touch event as an [Offset].
     */
    public fun onDragStart(touchPoint: Offset) { // First event of pointer input
        isTouchedInsideRectMove = isTouchInputInsideTheTouchRect(touchPoint)
        rectEdgeTouched = getRectEdge(touchPoint)
        lastPointUpdated = touchPoint
    }

    /**
     *  - Handles the ongoing drag event after the initial touch, providing logic for dragging
     *      the entire rectangle or resizing its corners based on the initial touch location.
     *
     *  @param dragPoint The coordinates of the ongoing drag event as an [Offset].
     */
    public fun onDrag(dragPoint: Offset) { // Second event of pointer input
        if (isTouchedInsideRectMove) {
            processIRectDrag(dragPoint = dragPoint)
        } else {
            when (rectEdgeTouched) {
                RectEdge.TOP_LEFT -> {
                    topLeftCornerDrag(dragPoint)
                }

                RectEdge.TOP_RIGHT -> {
                    topRightCornerDrag(dragPoint)
                }

                RectEdge.BOTTOM_LEFT -> {
                    bottomLeftCornerDrag(dragPoint)
                }

                RectEdge.BOTTOM_RIGHT -> {
                    bottomRightCornerDrag(dragPoint)
                }

                else -> Unit
            }
        }
    }


    /**
     *  - Handles the conclusion of a drag operation, resetting state variables related to the drag.
     *      This function is typically called after the user lifts their finger following a drag interaction.
     */
    public fun onDragEnd() { // Third event of pointer input
        isTouchedInsideRectMove = false
        lastPointUpdated = null
        rectEdgeTouched = RectEdge.NULL
    }

    /**
     *  - Handles the drag operation for moving the entire internal rectangle (iRect).
     *      Calculates the difference in drag position, checks if the updated top-left point
     *      of iRect stays inside the canvas, and updates the iRect accordingly.
     *
     *  @param dragPoint The current coordinates during the drag as an [Offset].
     */
    private fun processIRectDrag(dragPoint: Offset) {
        dragDiffCalculation(dragPoint)?.let { diffOffset ->
            val offsetCheck = Offset(
                x = (irectTopleft.x + diffOffset.x),
                y = (irectTopleft.y + diffOffset.y)
            )

            // before updating the top left point in rect need to check the irect stays inside the canvas
            val isIRectStaysInsideCanvas = isDragPointInsideTheCanvas(offsetCheck)

            // one point may reach any of corner but other way rect can still move
            if (offsetCheck.x >= 0F && offsetCheck.y >= 0F && isIRectStaysInsideCanvas) {
                updateIRectTopLeftPoint(offsetCheck)
            } else {
                // one point may reach any of corner but other way rect can still move
                val x = offsetCheck.x
                val y = offsetCheck.y
                var newOffset: Offset? = null

                if (y <= 0F && x > 0.0F && (x + iRect.size.width in 0F..canvasSize.width)) {
                    // top side touched to edge
                    newOffset = Offset(x, 0.0F)

                } else if (x <= 0F && y > 0F && (y + iRect.size.height in 0F..canvasSize.height)) {
                    // left side touched to edge
                    newOffset = Offset(0.0F, y)

                } else if ((x + iRect.size.width >= canvasSize.width) && y >= 0F
                    && (y + iRect.size.height in 0F..canvasSize.height)
                ) {
                    // right side touched to edge
                    newOffset = Offset((canvasSize.width - iRect.size.width), y)

                } else if ((y + iRect.size.height >= canvasSize.height) &&
                    x > 0F &&
                    (x + iRect.size.width in 0F..canvasSize.width)
                ) {
                    // bottom side touched to edge
                    newOffset = Offset(x, (canvasSize.height - iRect.size.height))
                }
                if (newOffset != null) {
                    updateIRectTopLeftPoint(newOffset)
                }
            }
        }
    }

    /**
     *  - Handles the drag operation for resizing the rectangle when the top-left corner is touched.
     *      Calculates the drag difference, updates the top-left point, and adjusts the width and height
     *      of the rectangle while ensuring it stays within specified limits.
     *
     *  @param dragPoint The current coordinates during the drag as an [Offset].
     */
    private fun topLeftCornerDrag(dragPoint: Offset) {
        dragDiffCalculation(dragPoint)?.let { dragDiff ->
            val (canvasWidth, canvasHeight) = canvasSize
            val size = iRect.size


            val x = (0f.coerceAtLeast(irectTopleft.x + dragDiff.x))
                .coerceAtMost(canvasWidth - minLimit)

            val y = (0f.coerceAtLeast(irectTopleft.y + dragDiff.y))
                .coerceAtMost(canvasHeight - minLimit)


            // Calculate new width and height based on drag direction
            val newWidth = calculateNewSize(size.width, dragDiff.x)
            val newHeight = calculateNewSize(size.height, dragDiff.y)

            irectTopleft = Offset(x, y)

            val sizeOfIRect = when (cropType) {
                CropType.PROFILE_CIRCLE, CropType.SQUARE -> {
                    val sqSide = min(newWidth, canvasWidth)
                    val totalHeight = (sqSide + irectTopleft.y)
                    val diff = canvasHeight - totalHeight
                    if (diff < 0) {
                        irectTopleft = irectTopleft.copy(
                            y = (irectTopleft.y + diff)
                        )
                    }

                    Size(width = sqSide, height = sqSide)
                }

                else -> { // Free style
                    Size(
                        width = min(newWidth, canvasWidth),
                        height = min(newHeight, canvasHeight)
                    )
                }
            }

            iRect = iRect.copy(
                topLeft = irectTopleft,
                size = sizeOfIRect
            )

            updateTouchRect()
        }
    }

    /**
     *  - Calculates the new size after a drag operation, considering the drag direction and limits.
     *
     *  @param currentSize The current size of the dimension (width/height).
     *  @param dragDiff The difference in drag position.
     *  @return The new size for the dimension.
     */
    private fun calculateNewSize(currentSize: Float, dragDiff: Float): Float {
        return if (dragDiff < 0F) {
            // Dimension will increase
            (currentSize + abs(dragDiff))
        } else {
            // Dimension will reduce
            max(currentSize - abs(dragDiff), minLimit)
        }
    }

    /**
     *  - Handles the drag operation for resizing the rectangle when the top-right corner is touched.
     *      Calculates the drag difference, adjusts the width and height of the rectangle while ensuring
     *      it stays within specified limits, and updates the rectangle accordingly.
     *
     *  @param dragPoint The current coordinates during the drag as an [Offset].
     */
    private fun topRightCornerDrag(dragPoint: Offset) {
        dragDiffCalculation(dragPoint)?.let { dragDiff ->
            // If irect y is already at 0 and dragDiff y is negative, no need to update
            if (iRect.topLeft.y <= 0F && dragDiff.y < 0F) return

            val size = iRect.size
            val (canvasWidth, canvasHeight) = canvasSize
            val irectX = iRect.topLeft.x
            val irectY = iRect.topLeft.y

            // Calculate new width based on drag direction
            val newWidth = if (dragDiff.x < 0F) {
                (size.width - abs(dragDiff.x))
            } else (size.width + abs(dragDiff.x))

            // Limit width based on canvas boundaries
            val width = if ((newWidth + irectX) > canvasWidth) {
                canvasWidth - irectX
            } else {
                if (newWidth <= minLimit) return
                newWidth
            }

            // Calculate new height based on drag direction
            var height = if (dragDiff.y <= 0F) {
                (size.height + abs(dragDiff.y))
            } else {
                (size.height - abs(dragDiff.y))
            }

            // Limit height based on canvas boundaries
            if (height > canvasHeight) height = canvasHeight

            // Calculate new y-point within canvas boundaries
            val yLimitPoint = canvasHeight - minLimit
            var yPoint = irectY + dragDiff.y
            yPoint = if (yPoint <= 0F) 0F else {
                if (yPoint >= yLimitPoint) yLimitPoint else yPoint
            }
            // Update top-left point and rectangle size
            irectTopleft = irectTopleft.copy(y = yPoint)

            val sizeOfIRect = when (cropType) {
                CropType.PROFILE_CIRCLE, CropType.SQUARE -> {
                    val sqSide = maxOf(minLimit, width)
                    val totalHeight = (sqSide + irectTopleft.y)
                    val diff = canvasHeight - totalHeight

                    if (diff < 0) {
                        irectTopleft = irectTopleft.copy(
                            y = (irectTopleft.y + diff)
                        )
                    }
                    Size(width = sqSide, height = sqSide)
                }

                else -> { // Free style
                    Size(width = maxOf(minLimit, width), height = maxOf(minLimit, height))
                }
            }


            iRect = iRect.copy(
                topLeft = irectTopleft,
                size = sizeOfIRect
            )

            updateTouchRect()
        }
    }

    /**
     *  - Handles the drag operation for resizing the rectangle when the bottom-left corner is touched.
     *      Calculates the drag difference, adjusts the width and height of the rectangle while ensuring
     *      it stays within specified limits, and updates the rectangle accordingly.
     *
     *  @param dragPoint The current coordinates during the drag as an [Offset].
     */
    private fun bottomLeftCornerDrag(dragPoint: Offset) {
        dragDiffCalculation(dragPoint)?.let { dragDiff ->
            val canvasHeight = canvasSize.height
            val size = iRect.size

            // For Y-Axis
            val h = (size.height + dragDiff.y)
            val height = if ((h + iRect.topLeft.y) > (canvasSize.height)) {
                (canvasSize.height - iRect.topLeft.y)
            } else h


            // For X-Axis
            val x = if ((iRect.topLeft.x + dragDiff.x) >= (canvasSize.width - minLimit)) {
                canvasSize.width - minLimit
            } else {
                val a = iRect.topLeft.x + dragDiff.x
                if (a < 0F) return
                a
            }

            // Update top-left point and rectangle size
            irectTopleft = Offset(x = if (x < 0F) 0F else x, y = iRect.topLeft.y)

            // For Irect Width
            var width = if (dragDiff.x < 0F) {
                (size.width + abs(dragDiff.x))
            } else (size.width - abs(dragDiff.x))

            if (width >= canvasSize.width) width = canvasSize.width

            val sizeOfIRect = when (cropType) {
                CropType.PROFILE_CIRCLE, CropType.SQUARE -> {
                    val sqSide = maxOf(minLimit, width)
                    val totalHeight = (sqSide + irectTopleft.y)
                    val diff = canvasHeight - totalHeight

                    if (diff < 0) {
                        irectTopleft = irectTopleft.copy(
                            y = (irectTopleft.y + diff)
                        )
                    }

                    Size(width = sqSide, height = sqSide)
                }

                else -> { // Free style
                    Size(width = maxOf(minLimit, width), height = maxOf(minLimit, height))
                }

            }

            iRect = iRect.copy(
                topLeft = irectTopleft,
                size = sizeOfIRect
            )

            updateTouchRect()
        }
    }

    /**
     *  - Handles the drag operation for resizing the rectangle when the bottom-right corner is touched.
     *      Calculates the drag difference, adjusts the width and height of the rectangle while ensuring
     *      it stays within specified limits, and updates the rectangle accordingly.
     *
     *  @param dragPoint The current coordinates during the drag as an [Offset].
     */
    private fun bottomRightCornerDrag(dragPoint: Offset) {
        dragDiffCalculation(dragPoint)?.let { dragDiff ->
            val canvasHeight = canvasSize.height
            val (sizeWidth, sizeHeight) = iRect.size

            val newWidth = (sizeWidth + dragDiff.x)
                .coerceAtMost(canvasSize.width - iRect.topLeft.x)
            val newHeight = (sizeHeight + dragDiff.y)
                .coerceAtMost(canvasSize.height - iRect.topLeft.y)

            val sizeOfIrect = when (cropType) {
                CropType.PROFILE_CIRCLE, CropType.SQUARE -> {
                    val sqSide = minLimit.coerceAtLeast(newWidth)
                    val totalHeight = (sqSide + irectTopleft.y)
                    val diff = canvasHeight - totalHeight

                    if (diff < 0) {
                        irectTopleft = irectTopleft.copy(
                            y = (irectTopleft.y + diff)
                        )
                    }
                    Size(width = sqSide, height = sqSide)
                }

                else -> { // Free style
                    Size(
                        width = newWidth.coerceAtLeast(minLimit),
                        height = newHeight.coerceAtLeast(minLimit)
                    )
                }
            }

            // Update rectangle size
            iRect = iRect.copy(topLeft = irectTopleft, size = sizeOfIrect)
            updateTouchRect()
        }
    }

    /**
     *  - Updates the top-left point of the internal rectangle (iRect) and its associated touch rectangle.
     *      The function takes an [Offset] representing the new top-left point and adjusts both rectangles
     *      accordingly, ensuring that the touch rectangle is updated based on padding.
     *
     *  @param offset The new top-left coordinates as an [Offset].
     */
    private fun updateIRectTopLeftPoint(offset: Offset) {
        irectTopleft = Offset(
            x = offset.x,
            y = offset.y
        )

        touchAreaRectTopLeft = Offset(
            x = (irectTopleft.x + paddingForTouchRect),
            y = (irectTopleft.y + paddingForTouchRect)
        )

        iRect = iRect.copy(
            topLeft = irectTopleft
        )
        touchRect = touchRect.copy(
            topLeft = touchAreaRectTopLeft
        )
    }


    /**
     *  - Checks if the given drag Point, considering the width and height of the internal rectangle (iRect),
     *      is within the boundaries of the canvas.
     *
     *  @param dragPoint The top-left coordinates as an [Offset].
     *  @return `true` if the calculated bottom-right point is inside the canvas boundaries, `false` otherwise.
     */
    private fun isDragPointInsideTheCanvas(dragPoint: Offset): Boolean {
        val x = (dragPoint.x + iRect.size.width)
        val y = (dragPoint.y + iRect.size.height)
        return (x in 0F..canvasSize.width && y in 0F..canvasSize.height)
    }

    /**
     *  - Calculates the difference in coordinates between two consecutive drag points.
     *      Returns an [Offset] representing the change in position from the last updated point to the current drag point.
     *      If there is no previous point, returns null.
     *
     *  @param dragPoint The current coordinates during the drag as an [Offset].
     *  @return The difference in coordinates as an [Offset], or null if no previous point exists.
     */
    private fun dragDiffCalculation(dragPoint: Offset): Offset? {
        if (lastPointUpdated != null && lastPointUpdated != dragPoint) {
            val difference = getDiffBetweenTwoOffset(lastPointUpdated!!, dragPoint)
            lastPointUpdated = dragPoint
            // Return the difference in coordinates
            return Offset(difference.x, difference.y)
        }
        lastPointUpdated = dragPoint
        return null
    }

    /**
     *  - Calculates the difference in coordinates between two offset points and returns a [PointF] representing
     *      the change in position from the first point to the second point.
     *
     *  @param pointOne The first offset point as an [Offset].
     *  @param pointTwo The second offset point as an [Offset].
     *  @return The difference in coordinates as a [PointF].
     */
    private fun getDiffBetweenTwoOffset(pointOne: Offset, pointTwo: Offset): PointF {
        val dx = pointTwo.x - pointOne.x // calculate the difference in the x coordinates
        val dy = pointTwo.y - pointOne.y // calculate the difference in the y coordinates
        // pointF holds the two x,y values
        return PointF(dx, dy)
    }

    /**
     *  - Determines which edge of the rectangle (iRect) needs to be dragged based on the provided touch point
     *      inside the canvas. The function checks the proximity of the touch point to the edges of the rectangle.
     *
     *  @param touchPoint The touch point coordinates as an [Offset] inside the canvas.
     *  @return The corresponding [RectEdge] indicating which edge of the rectangle to drag.
     */
    private fun getRectEdge(touchPoint: Offset): RectEdge {
        val iRectSize = iRect.size
        val topleftX = iRect.topLeft.x
        val topleftY = iRect.topLeft.y

        val rectWidth = (topleftX + iRectSize.width)
        val rectHeight = (iRect.topLeft.y + iRectSize.height)

        val padding = minLimit

        // For bottom right edge
        val width = (touchPoint.x in (rectWidth - padding..rectWidth + padding))
        val height = (touchPoint.y in (rectHeight - padding..rectHeight + padding))

        // For bottom left edge
        val widthLeft = (touchPoint.x in (topleftX - padding..topleftX + padding))

        // For top right edge
        val isOnY = (touchPoint.y in (topleftY - padding..topleftY + padding))

        // For top left edge
        val x = (touchPoint.x in (topleftX - padding..topleftX + padding))
        val y = (touchPoint.y in (topleftY - padding..topleftY + padding))


        if (width && height) {
            // BOTTOM_RIGHT edge
            return RectEdge.BOTTOM_RIGHT
        } else if (height && widthLeft) {
            // BOTTOM_LEFT edge
            return RectEdge.BOTTOM_LEFT
        } else if (width && isOnY) {
            // TOP_RIGHT edge
            return RectEdge.TOP_RIGHT
        } else if (x && y) {
            // TOP_LEFT
            return RectEdge.TOP_LEFT
        }

        return RectEdge.NULL
    }

    /**
     *  - Checks whether the touch input started from inside the bounds of the touch rectangle.
     *      The function verifies if the provided touch point is within the horizontal and vertical boundaries
     *      of the touch rectangle.
     *
     *  @param touchPoint The touch point coordinates as an [Offset].
     *  @return `true` if the touch input started inside the touch rectangle, `false` otherwise.
     */
    private fun isTouchInputInsideTheTouchRect(touchPoint: Offset): Boolean {
        val xStartPoint = touchRect.topLeft.x
        val xEndPoint = (touchRect.topLeft.x + touchRect.size.width)

        val yStartPoint = touchRect.topLeft.y
        val yEndPoint = (touchRect.topLeft.y + touchRect.size.height)

        return (touchPoint.x in xStartPoint..xEndPoint && touchPoint.y in yStartPoint..yEndPoint)
    }


    /**
     *  - Crops the bitmap based on the current rectangle bounds and returns the cropped bitmap.
     *      The function creates a scaled bitmap from the original image and extracts the region defined
     *      by the current rectangle bounds, ensuring the cropped image fits within the canvas size.
     *
     *  @return The cropped [Bitmap] based on the current rectangle bounds.
     */
    public fun cropImage(): Bitmap {
        val canvasWidth = canvasSize.width.toInt()
        val canvasHeight = canvasSize.height.toInt()

        // Get the rectangle bounds to crop
        val cropRect = getRectFromPoints()

        val sourceBitmap = bitmapImage ?: mBitmapImage

        if (canvasWidth <= 0 || canvasHeight <= 0) return sourceBitmap

        // Create a scaled bitmap from the original image
        val scaledBitmap = Bitmap.createScaledBitmap(sourceBitmap, canvasWidth, canvasHeight, true)

        var cropLeft = cropRect.left.toInt().coerceAtLeast(0)
        var cropTop = cropRect.top.toInt().coerceAtLeast(0)
        val cropWidth = cropRect.width.toInt().coerceAtMost(canvasWidth)
        val cropHeight = cropRect.height.toInt().coerceAtMost(canvasHeight)


        // Adjust bounds to ensure they fit within the canvas size.
        if (cropLeft + cropWidth > canvasWidth) {
            cropLeft = 0
        }
        if (cropTop + cropHeight > canvasHeight) {
            cropTop = abs(canvasHeight - cropHeight)
        }

        // Create the cropped bitmap.
        val cropBitmap = if (cropWidth <= 0 || cropHeight <= 0) {
            Bitmap.createBitmap(
                scaledBitmap,
                0,
                0,
                canvasWidth,
                canvasHeight
            )
        } else {
            Bitmap.createBitmap(
                scaledBitmap,
                cropLeft,
                cropTop,
                cropWidth,
                cropHeight
            )
        }

        if (cropType == CropType.SQUARE || cropType == CropType.PROFILE_CIRCLE) {
            // Will scale the bitmap in square shape.
            return Bitmap.createScaledBitmap(
                cropBitmap,
                maxSquareLimit.toInt(),
                maxSquareLimit.toInt(),
                true
            )
        }

        // Scale the cropped bitmap to match the canvas size.
        return Bitmap.createScaledBitmap(cropBitmap, canvasWidth, canvasHeight, true)
    }

    public fun updateCropType(type: CropType) {
        cropType = type
        resetCropIRect()
    }

    private fun getCurrCropType(): CropType {
        return cropType ?: CropType.FREE_STYLE
    }

    public fun updateBitmapImage(bitmap: Bitmap) {
        bitmapImage = bitmap
    }

    /**
     *  - Converts the internal rectangle (iRect) represented by the top-left point (irectTopleft) and its size
     *      into a [Rect] object, specifying the left, top, right, and bottom coordinates.
     *
     *  @return The [Rect] object representing the internal rectangle.
     */
    private fun getRectFromPoints(): Rect {
        val size = iRect.size
        val right = (size.width + irectTopleft.x)
        val bottom = (size.height + irectTopleft.y)
        return Rect(
            irectTopleft.x,    //left
            irectTopleft.y,    //top
            right,             //right
            bottom,            //bottom
        )
    }

}