@file:Suppress("RedundantConstructorKeyword")

package com.image.cropview

import android.graphics.Bitmap
import android.graphics.PointF
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import androidx.core.graphics.scale

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
     * The current zoom scale factor. 1.0 = no zoom.
     */
    public var zoomScale: Float by mutableFloatStateOf(1.0f)
        private set

    /**
     * The current pan offset of the zoomed image, in canvas coordinates.
     */
    public var zoomOffset: Offset by mutableStateOf(Offset.Zero)
        private set

    private val minZoom: Float = 1.0f
    private val maxZoom: Float = 5.0f

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
        resetZoom()
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
     * The last touch point used for single-finger image pan tracking.
     */
    private var lastPanPoint: Offset? = null

    /**
     * Handles a pinch-zoom gesture update. Keeps the pinch centroid stationary on screen
     * while scaling the image, and applies simultaneous two-finger pan.
     *
     * Correct centroid-anchored formula:
     *   new_offset = (centroid - pivot) * (1 - scaleFactor) + old_offset * scaleFactor + panChange
     * where pivot = canvas center.
     *
     * @param centroid The centroid of the pointers in canvas coordinates.
     * @param scaleChange The multiplicative change in scale for this frame.
     * @param panChange The translational change from centroid movement this frame.
     */
    public fun onZoomChange(centroid: Offset, scaleChange: Float, panChange: Offset) {
        val newScale = (zoomScale * scaleChange).coerceIn(minZoom, maxZoom)
        val scaleFactor = newScale / zoomScale
        val pivot = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
        val newOffset = Offset(
            x = (centroid.x - pivot.x) * (1f - scaleFactor) + zoomOffset.x * scaleFactor + panChange.x,
            y = (centroid.y - pivot.y) * (1f - scaleFactor) + zoomOffset.y * scaleFactor + panChange.y
        )
        zoomScale = newScale
        zoomOffset = constrainOffset(newOffset, newScale)
    }

    /**
     * Constrains the pan offset so the zoomed image always fully covers the canvas,
     * preventing empty (background) areas from becoming visible at the edges.
     *
     * At scale S, the image extends by canvasSize * S / 2 from the canvas center.
     * The maximum offset in each direction is (canvasSize / 2) * (S - 1).
     */
    private fun constrainOffset(offset: Offset, scale: Float): Offset {
        val maxOffsetX = (canvasSize.width / 2f) * (scale - 1f)
        val maxOffsetY = (canvasSize.height / 2f) * (scale - 1f)
        return Offset(
            x = offset.x.coerceIn(-maxOffsetX, maxOffsetX),
            y = offset.y.coerceIn(-maxOffsetY, maxOffsetY)
        )
    }

    /**
     * Resets zoom state to default (no zoom, no pan).
     */
    public fun resetZoom() {
        zoomScale = 1.0f
        zoomOffset = Offset.Zero
        lastPanPoint = null
    }

    /**
     * Toggles zoom on a double-tap. Zooms to 2x centred on [tapPoint] if currently
     * at minimum zoom, or resets to 1x if already zoomed in.
     *
     * @param tapPoint The tap position in canvas coordinates.
     */
    public fun onDoubleTapZoom(tapPoint: Offset) {
        if (zoomScale > minZoom) {
            zoomScale = minZoom
            zoomOffset = Offset.Zero
        } else {
            val targetScale = 2.0f
            val pivot = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            val newOffset = Offset(
                x = (tapPoint.x - pivot.x) * (1f - targetScale),
                y = (tapPoint.y - pivot.y) * (1f - targetScale)
            )
            zoomScale = targetScale
            zoomOffset = constrainOffset(newOffset, targetScale)
        }
    }

    /**
     * Starts a single-finger image pan gesture at [touchPoint].
     * Call this when the user touches outside the crop rectangle while zoomed in.
     */
    public fun onImagePanStart(touchPoint: Offset) {
        lastPanPoint = touchPoint
    }

    /**
     * Updates the image pan position during a single-finger drag.
     * Applies the movement delta to [zoomOffset] while keeping the image within canvas bounds.
     *
     * @param dragPoint The current finger position in canvas coordinates.
     */
    public fun onImagePanDrag(dragPoint: Offset) {
        lastPanPoint?.let { last ->
            if (last != dragPoint) {
                val newOffset = Offset(
                    x = zoomOffset.x + (dragPoint.x - last.x),
                    y = zoomOffset.y + (dragPoint.y - last.y)
                )
                zoomOffset = constrainOffset(newOffset, zoomScale)
            }
        }
        lastPanPoint = dragPoint
    }

    /**
     * Ends a single-finger image pan gesture.
     */
    public fun onImagePanEnd() {
        lastPanPoint = null
    }

    /**
     * Returns true if [touchPoint] is within the interactive area of the crop rectangle,
     * including the extended corner hit zones used for resizing.
     *
     * Points outside this area (while zoomed) trigger image panning instead.
     */
    public fun isTouchOnCropRect(touchPoint: Offset): Boolean {
        val cornerPad = paddingForTouchRect * 3f // same as minLimit for corner detection
        val left = iRect.topLeft.x - cornerPad
        val top = iRect.topLeft.y - cornerPad
        val right = iRect.topLeft.x + iRect.size.width + cornerPad
        val bottom = iRect.topLeft.y + iRect.size.height + cornerPad
        return touchPoint.x in left..right && touchPoint.y in top..bottom
    }

    /**
     * Maps a point from canvas coordinates to the un-zoomed image coordinate space.
     *
     * Forward transform: screen = pivot + scale * (imagePoint - pivot) + offset
     * Inverse: imagePoint = pivot + (screen - offset - pivot) / scale
     */
    private fun canvasPointToImagePoint(canvasPoint: Offset): Offset {
        val cx = canvasSize.width / 2f
        val cy = canvasSize.height / 2f
        return Offset(
            x = cx + (canvasPoint.x - zoomOffset.x - cx) / zoomScale,
            y = cy + (canvasPoint.y - zoomOffset.y - cy) / zoomScale
        )
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
     *      The bottom-right corner remains fixed while the top-left corner is dragged.
     *
     *  @param dragPoint The current coordinates during the drag as an [Offset].
     */
    private fun topLeftCornerDrag(dragPoint: Offset) {
        dragDiffCalculation(dragPoint)?.let { dragDiff ->
            // Anchor: bottom-right corner must stay fixed
            val fixedRight = irectTopleft.x + iRect.size.width
            val fixedBottom = irectTopleft.y + iRect.size.height

            // Calculate new top-left position
            var newX = irectTopleft.x + dragDiff.x
            var newY = irectTopleft.y + dragDiff.y

            // Constrain within canvas bounds
            newX = newX.coerceAtLeast(0f)
            newY = newY.coerceAtLeast(0f)

            // Ensure minimum size by limiting how far top-left can move toward bottom-right
            newX = newX.coerceAtMost(fixedRight - minLimit)
            newY = newY.coerceAtMost(fixedBottom - minLimit)

            // Compute new size from fixed bottom-right and new top-left
            val newWidth = fixedRight - newX
            val newHeight = fixedBottom - newY

            val sizeOfIRect = when (cropType) {
                CropType.PROFILE_CIRCLE, CropType.SQUARE -> {
                    // For square, use the smaller dimension change
                    val sqSide = min(newWidth, newHeight).coerceAtLeast(minLimit)
                    // Adjust top-left to maintain square from fixed bottom-right
                    newX = fixedRight - sqSide
                    newY = fixedBottom - sqSide
                    // Ensure we don't go outside canvas
                    if (newX < 0f) {
                        newX = 0f
                    }
                    if (newY < 0f) {
                        newY = 0f
                    }
                    val finalSide = min(fixedRight - newX, fixedBottom - newY)
                    newX = fixedRight - finalSide
                    newY = fixedBottom - finalSide
                    Size(width = finalSide, height = finalSide)
                }

                else -> { // Free style
                    Size(width = newWidth, height = newHeight)
                }
            }

            irectTopleft = Offset(newX, newY)
            iRect = iRect.copy(topLeft = irectTopleft, size = sizeOfIRect)
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
     *      The bottom-left corner remains fixed while the top-right corner is dragged.
     *
     *  @param dragPoint The current coordinates during the drag as an [Offset].
     */
    private fun topRightCornerDrag(dragPoint: Offset) {
        dragDiffCalculation(dragPoint)?.let { dragDiff ->
            val (canvasWidth, _) = canvasSize

            // Anchor: bottom-left corner must stay fixed
            val fixedLeft = irectTopleft.x
            val fixedBottom = irectTopleft.y + iRect.size.height

            // Calculate new top-right position
            var newRight = irectTopleft.x + iRect.size.width + dragDiff.x
            var newTop = irectTopleft.y + dragDiff.y

            // Constrain within canvas bounds
            newRight = newRight.coerceAtMost(canvasWidth)
            newTop = newTop.coerceAtLeast(0f)

            // Ensure minimum size
            newRight = newRight.coerceAtLeast(fixedLeft + minLimit)
            newTop = newTop.coerceAtMost(fixedBottom - minLimit)

            // Compute new size from fixed bottom-left
            val newWidth = newRight - fixedLeft
            val newHeight = fixedBottom - newTop

            val sizeOfIRect = when (cropType) {
                CropType.PROFILE_CIRCLE, CropType.SQUARE -> {
                    // For square, use the smaller dimension change
                    val sqSide = min(newWidth, newHeight).coerceAtLeast(minLimit)
                    // Adjust to maintain square from fixed bottom-left
                    newTop = fixedBottom - sqSide
                    // Ensure we don't go outside canvas
                    if (newTop < 0f) {
                        newTop = 0f
                    }
                    val finalSide = min(fixedLeft + canvasWidth - fixedLeft, fixedBottom - newTop)
                        .coerceAtMost(sqSide)
                    newTop = fixedBottom - finalSide
                    Size(width = finalSide, height = finalSide)
                }

                else -> { // Free style
                    Size(width = newWidth, height = newHeight)
                }
            }

            irectTopleft = Offset(fixedLeft, newTop)
            iRect = iRect.copy(topLeft = irectTopleft, size = sizeOfIRect)
            updateTouchRect()
        }
    }

    /**
     *  - Handles the drag operation for resizing the rectangle when the bottom-left corner is touched.
     *      The top-right corner remains fixed while the bottom-left corner is dragged.
     *
     *  @param dragPoint The current coordinates during the drag as an [Offset].
     */
    private fun bottomLeftCornerDrag(dragPoint: Offset) {
        dragDiffCalculation(dragPoint)?.let { dragDiff ->
            val (_, canvasHeight) = canvasSize

            // Anchor: top-right corner must stay fixed
            val fixedTop = irectTopleft.y
            val fixedRight = irectTopleft.x + iRect.size.width

            // Calculate new bottom-left position
            var newLeft = irectTopleft.x + dragDiff.x
            var newBottom = irectTopleft.y + iRect.size.height + dragDiff.y

            // Constrain within canvas bounds
            newLeft = newLeft.coerceAtLeast(0f)
            newBottom = newBottom.coerceAtMost(canvasHeight)

            // Ensure minimum size
            newLeft = newLeft.coerceAtMost(fixedRight - minLimit)
            newBottom = newBottom.coerceAtLeast(fixedTop + minLimit)

            // Compute new size from fixed top-right
            val newWidth = fixedRight - newLeft
            val newHeight = newBottom - fixedTop

            val sizeOfIRect = when (cropType) {
                CropType.PROFILE_CIRCLE, CropType.SQUARE -> {
                    // For square, use the smaller dimension change
                    val sqSide = min(newWidth, newHeight).coerceAtLeast(minLimit)
                    // Adjust to maintain square from fixed top-right
                    newLeft = fixedRight - sqSide
                    // Ensure we don't go outside canvas
                    if (newLeft < 0f) {
                        newLeft = 0f
                    }
                    val finalSide = min(fixedRight - newLeft, canvasHeight - fixedTop)
                        .coerceAtMost(sqSide)
                    newLeft = fixedRight - finalSide
                    Size(width = finalSide, height = finalSide)
                }

                else -> { // Free style
                    Size(width = newWidth, height = newHeight)
                }
            }

            irectTopleft = Offset(newLeft, fixedTop)
            iRect = iRect.copy(topLeft = irectTopleft, size = sizeOfIRect)
            updateTouchRect()
        }
    }

    /**
     *  - Handles the drag operation for resizing the rectangle when the bottom-right corner is touched.
     *      The top-left corner remains fixed while the bottom-right corner is dragged.
     *
     *  @param dragPoint The current coordinates during the drag as an [Offset].
     */
    private fun bottomRightCornerDrag(dragPoint: Offset) {
        dragDiffCalculation(dragPoint)?.let { dragDiff ->
            val (canvasWidth, canvasHeight) = canvasSize

            // Anchor: top-left corner must stay fixed
            val fixedLeft = irectTopleft.x
            val fixedTop = irectTopleft.y

            // Calculate new bottom-right position
            var newRight = irectTopleft.x + iRect.size.width + dragDiff.x
            var newBottom = irectTopleft.y + iRect.size.height + dragDiff.y

            // Constrain within canvas bounds
            newRight = newRight.coerceAtMost(canvasWidth)
            newBottom = newBottom.coerceAtMost(canvasHeight)

            // Ensure minimum size
            newRight = newRight.coerceAtLeast(fixedLeft + minLimit)
            newBottom = newBottom.coerceAtLeast(fixedTop + minLimit)

            // Compute new size from fixed top-left
            val newWidth = newRight - fixedLeft
            val newHeight = newBottom - fixedTop

            val sizeOfIRect = when (cropType) {
                CropType.PROFILE_CIRCLE, CropType.SQUARE -> {
                    // For square, use the smaller dimension
                    val sqSide = min(newWidth, newHeight).coerceAtLeast(minLimit)
                    Size(width = sqSide, height = sqSide)
                }

                else -> { // Free style
                    Size(width = newWidth, height = newHeight)
                }
            }

            // top-left stays fixed, only size changes
            iRect = iRect.copy(topLeft = irectTopleft, size = sizeOfIRect)
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

        val cropRect = getRectFromPoints()
        val sourceBitmap = bitmapImage ?: mBitmapImage

        if (canvasWidth <= 0 || canvasHeight <= 0) return sourceBitmap

        val scaledBitmap = sourceBitmap.scale(canvasWidth, canvasHeight)

        // Map crop rect corners through inverse zoom transform
        val topLeftImage = canvasPointToImagePoint(Offset(cropRect.left, cropRect.top))
        val bottomRightImage = canvasPointToImagePoint(Offset(cropRect.right, cropRect.bottom))

        val cropLeft = topLeftImage.x.toInt().coerceAtLeast(0)
        val cropTop = topLeftImage.y.toInt().coerceAtLeast(0)
        var cropWidth = (bottomRightImage.x - topLeftImage.x).toInt().coerceIn(1, canvasWidth)
        var cropHeight = (bottomRightImage.y - topLeftImage.y).toInt().coerceIn(1, canvasHeight)

        if (cropLeft + cropWidth > canvasWidth) {
            cropWidth = canvasWidth - cropLeft
        }
        if (cropTop + cropHeight > canvasHeight) {
            cropHeight = canvasHeight - cropTop
        }

        cropWidth = cropWidth.coerceAtLeast(1)
        cropHeight = cropHeight.coerceAtLeast(1)

        val cropBitmap = Bitmap.createBitmap(
            scaledBitmap,
            cropLeft,
            cropTop,
            cropWidth,
            cropHeight
        )

        if (cropType == CropType.SQUARE || cropType == CropType.PROFILE_CIRCLE) {
            return cropBitmap.scale(maxSquareLimit.toInt(), maxSquareLimit.toInt())
        }

        return cropBitmap.scale(canvasWidth, canvasHeight)
    }

    /**
     *  - Crops the bitmap based on the current rectangle bounds and returns the cropped bitmap.
     *      The function creates a scaled bitmap from the original image, using the region defined by the user
     *      scaled to the original bitmap size. This ensures the returned bitmap is a crop of the original image, not
     *      the scaled version rendered in the canvas.
     *
     *  @return The cropped [Bitmap] based on the current rectangle bounds.
     */
    public fun cropSourceImage(): Bitmap {
        val sourceBitmap = bitmapImage ?: mBitmapImage

        val canvasCropRect = getRectFromPoints()

        if (canvasSize.width <= 0f || canvasSize.height <= 0f) {
            return sourceBitmap
        }

        // Map crop rect corners through inverse zoom transform
        val topLeftImage = canvasPointToImagePoint(Offset(canvasCropRect.left, canvasCropRect.top))
        val bottomRightImage = canvasPointToImagePoint(Offset(canvasCropRect.right, canvasCropRect.bottom))

        // Scale from canvas coords to source bitmap coords
        val scaleX = sourceBitmap.width.toFloat() / canvasSize.width
        val scaleY = sourceBitmap.height.toFloat() / canvasSize.height

        val sourceCropLeft = (topLeftImage.x * scaleX).toInt().coerceAtLeast(0)
        val sourceCropTop = (topLeftImage.y * scaleY).toInt().coerceAtLeast(0)
        var sourceCropWidth = ((bottomRightImage.x - topLeftImage.x) * scaleX).toInt()
        var sourceCropHeight = ((bottomRightImage.y - topLeftImage.y) * scaleY).toInt()

        sourceCropWidth = sourceCropWidth.coerceAtMost(sourceBitmap.width - sourceCropLeft)
        sourceCropHeight = sourceCropHeight.coerceAtMost(sourceBitmap.height - sourceCropTop)

        if (sourceCropWidth <= 0 || sourceCropHeight <= 0) {
            return sourceBitmap
        }

        return Bitmap.createBitmap(
            sourceBitmap,
            sourceCropLeft,
            sourceCropTop,
            sourceCropWidth,
            sourceCropHeight
        )
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
