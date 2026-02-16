package com.image.cropview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt


/**
 *  - A composable utility class for displaying and manipulating crop views of a provided [Bitmap] image.
 *      The class allows users to interactively drag, resize, and crop the image within a specified rectangle.
 *
 *  @param bitmapImage The bitmap image to be cropped.
 */
public class ImageCrop(
    private var bitmapImage: Bitmap
) : OnCrop {

    /**
     * The internal [CropUtil] instance responsible for handling dragging and cropping events.
     */
    private lateinit var cropU: CropUtil

    /**
     * Timestamp of the most recent confirmed single tap, used for double-tap detection.
     */
    private var lastTapTime: Long = 0L

    /**
     * Canvas position of the most recent confirmed single tap, used for double-tap detection.
     */
    private var lastTapPosition: Offset = Offset.Zero

    /**
     *  - Composable function to display the crop view and handle user interactions.
     *
     *  @param modifier The modifier for configuring the layout and appearance of the crop view.
     *  @param guideLineColor The color of guidelines and rectangles in the crop view.
     *  @param guideLineWidth The width of guidelines in the crop view.
     *  @param edgeCircleSize The size of circular markers the crop rectangle.
     *  @param showGuideLines Handle the visibility of the guidelines.
     *  @param cropType [CropType]
     *  @param edgeType [EdgeType]
     *  @param enableZoom When true, enables pinch-to-zoom and double-tap-to-zoom on the image.
     *      - Two-finger pinch/pan zooms and pans the image underneath the crop frame.
     *      - Double-tap toggles between 1x and 2x zoom centred on the tap point.
     *      - Single-finger drag inside the crop rectangle continues to move or resize it.
     *      - Single-finger drag outside the crop rectangle pans the image when zoomed in.
     *      Defaults to false for backward compatibility.
     */
    @SuppressLint("UnusedBoxWithConstraintsScope")
    @Composable
    public fun ImageCropView(
        modifier: Modifier = Modifier,
        guideLineColor: Color = Color(0xFFD1CBE2),
        guideLineWidth: Dp = 2.dp,
        edgeCircleSize: Dp = 8.dp,
        showGuideLines: Boolean = true,
        cropType: CropType = CropType.FREE_STYLE,
        edgeType: EdgeType = EdgeType.CIRCULAR,
        enableZoom: Boolean = false
    ) {

        val cropUtil by remember { mutableStateOf(CropUtil(bitmapImage)) }
        cropU = cropUtil

        if (cropU.cropType == null || cropType != cropU.cropType) {
            cropU.updateCropType(cropType)
        }

        BoxWithConstraints(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            val imageAspectRatio = bitmapImage.width.toFloat() / bitmapImage.height.toFloat()
            val containerAspectRatio = maxWidth.value / maxHeight.value

            val (targetWidth, targetHeight) = if (imageAspectRatio > containerAspectRatio) {
                // Image is wider than container - fit by width
                maxWidth to (maxWidth / imageAspectRatio)
            } else {
                // Image is taller than container - fit by height
                (maxHeight * imageAspectRatio) to maxHeight
            }

            Canvas(
                modifier = Modifier
                    .size(width = targetWidth, height = targetHeight)
                    .onSizeChanged { intSize ->
                        cropUtil.onCanvasSizeChanged(intSize = intSize)
                    }
                    .pointerInput(enableZoom) {
                        if (!enableZoom) {
                            // Original single-gesture behavior, unchanged
                            detectDragGestures(
                                onDragStart = { touchPoint ->
                                    cropUtil.onDragStart(touchPoint)
                                },
                                onDrag = { pointerInputChange, _ ->
                                    pointerInputChange.consume()
                                    val dragPoint = pointerInputChange.position
                                    cropUtil.onDrag(dragPoint)
                                },
                                onDragEnd = {
                                    cropUtil.onDragEnd()
                                }
                            )
                        } else {
                            // Unified zoom-aware gesture handler.
                            //
                            // Gesture modes:
                            //  IDLE       – initial state; finger just placed
                            //  CROP_DRAG  – single finger on/inside the crop rectangle
                            //  IMAGE_PAN  – single finger outside crop rect while zoomed
                            //  ZOOM       – two or more fingers (pinch + pan)
                            //
                            // Double-tap (two quick taps < 300 ms apart, < 80 px) toggles zoom.
                            val doubleTapThresholdMs = 300L
                            val doubleTapRadiusPx = 80f

                            awaitEachGesture {
                                val firstDown = awaitFirstDown(requireUnconsumed = false)
                                firstDown.consume()

                                val downTime = System.currentTimeMillis()
                                val downPosition = firstDown.position

                                // Determine the initial mode from the touch position
                                val touchOnCropRect = cropUtil.isTouchOnCropRect(downPosition)
                                val zoomed = cropUtil.zoomScale > 1.0f

                                // 0 = IDLE, 1 = CROP_DRAG, 2 = IMAGE_PAN, 3 = ZOOM
                                var gestureMode: Int
                                when {
                                    touchOnCropRect -> {
                                        cropUtil.onDragStart(downPosition)
                                        gestureMode = 1 // CROP_DRAG
                                    }
                                    zoomed -> {
                                        cropUtil.onImagePanStart(downPosition)
                                        gestureMode = 2 // IMAGE_PAN
                                    }
                                    else -> {
                                        gestureMode = 0 // IDLE – touch outside crop rect at 1x zoom
                                    }
                                }

                                var hasMoved = false

                                // Track pinch state
                                var zoomInitialized = false
                                var previousCentroid = Offset.Zero
                                var previousCentroidDistance = 0f

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val changes = event.changes

                                    // All fingers lifted
                                    if (changes.all { !it.pressed }) {
                                        when (gestureMode) {
                                            1 -> cropUtil.onDragEnd()        // CROP_DRAG
                                            2 -> cropUtil.onImagePanEnd()    // IMAGE_PAN
                                        }

                                        // Double-tap detection: only fires for quick taps without movement
                                        if (!hasMoved && gestureMode != 3) {
                                            val upTime = System.currentTimeMillis()
                                            if ((upTime - downTime) < doubleTapThresholdMs) {
                                                val timeSinceLast = upTime - lastTapTime
                                                val distFromLast = run {
                                                    val d = downPosition - lastTapPosition
                                                    sqrt(d.x * d.x + d.y * d.y)
                                                }
                                                if (timeSinceLast < doubleTapThresholdMs &&
                                                    distFromLast < doubleTapRadiusPx
                                                ) {
                                                    // Confirmed double-tap
                                                    cropUtil.onDoubleTapZoom(downPosition)
                                                    lastTapTime = 0L
                                                } else {
                                                    // First tap of a potential double-tap
                                                    lastTapTime = upTime
                                                    lastTapPosition = downPosition
                                                }
                                            }
                                        }
                                        break
                                    }

                                    val activeChanges = changes.filter { it.pressed }
                                    val fingerCount = activeChanges.size

                                    if (fingerCount >= 2) {
                                        // Transition into zoom mode
                                        if (gestureMode == 1) cropUtil.onDragEnd()
                                        if (gestureMode == 2) cropUtil.onImagePanEnd()
                                        gestureMode = 3 // ZOOM

                                        val currentPositions = activeChanges.map { it.position }
                                        val currentCentroid = calculateCentroid(currentPositions)
                                        val currentDistance = calculateAverageDistance(
                                            currentPositions, currentCentroid
                                        )

                                        if (!zoomInitialized) {
                                            // Capture baseline on the first frame with 2 fingers
                                            // (also resets baseline each time a new 2-finger
                                            // contact is established after dropping to 1 finger)
                                            zoomInitialized = true
                                            previousCentroid = currentCentroid
                                            previousCentroidDistance = currentDistance
                                        } else {
                                            val scaleChange = if (previousCentroidDistance > 0f) {
                                                currentDistance / previousCentroidDistance
                                            } else {
                                                1f
                                            }
                                            val panChange = currentCentroid - previousCentroid
                                            cropUtil.onZoomChange(currentCentroid, scaleChange, panChange)

                                            previousCentroid = currentCentroid
                                            previousCentroidDistance = currentDistance
                                        }
                                        activeChanges.forEach { it.consume() }

                                    } else if (fingerCount == 1) {
                                        // One finger remaining
                                        val change = activeChanges.first()

                                        if (gestureMode == 3) {
                                            // Dropping from pinch back to one finger:
                                            // reset zoom baseline so next 2-finger contact
                                            // starts fresh, and ignore this single finger
                                            // to prevent a sudden position jump.
                                            zoomInitialized = false
                                            change.consume()
                                            continue
                                        }

                                        if (change.positionChanged()) {
                                            hasMoved = true
                                            when (gestureMode) {
                                                1 -> { // CROP_DRAG
                                                    cropUtil.onDrag(change.position)
                                                    change.consume()
                                                }
                                                2 -> { // IMAGE_PAN
                                                    cropUtil.onImagePanDrag(change.position)
                                                    change.consume()
                                                }
                                                0 -> { // IDLE - finger was outside crop rect at 1x zoom
                                                    // Nothing to do; user cannot interact until
                                                    // they touch inside the crop rect or zoom in.
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },

                onDraw = {
                    // Clip the bitmap draw to the canvas bounds so a zoomed image
                    // never bleeds outside the view's allocated space.
                    // clipRect defaults to (0, 0, size.width, size.height).
                    clipRect {
                        // Draw bitmap image with zoom transform applied.
                        // The transform order is: scale around canvas center, then translate by offset.
                        // A bitmap point b maps to: pivot + scale*(b - pivot) + offset
                        withTransform({
                            translate(left = cropUtil.zoomOffset.x, top = cropUtil.zoomOffset.y)
                            scale(
                                scaleX = cropUtil.zoomScale,
                                scaleY = cropUtil.zoomScale,
                                pivot = Offset(size.width / 2f, size.height / 2f)
                            )
                        }) {
                            drawBitmap(
                                bitmap = bitmapImage,
                                canvasSize = cropUtil.canvasSize
                            )
                        }
                    }

                    // Overlay elements are drawn at canvas scale (no zoom transform) so
                    // the crop rectangle, guidelines and edge markers remain stable and
                    // precise regardless of the current zoom level.

                    // Circle mask for PROFILE_CIRCLE crop type
                    if (cropType == CropType.PROFILE_CIRCLE) {
                        val circleRadius: Float = (cropU.iRect.size.width / 2)
                        val circlePath = Path().apply {
                            addOval(
                                Rect(
                                    center = Offset(
                                        cropU.iRect.topLeft.x + (circleRadius),
                                        cropU.iRect.topLeft.y + (circleRadius)
                                    ),
                                    radius = circleRadius - guideLineWidth.toPx()
                                )
                            )
                        }

                        clipPath(circlePath, clipOp = ClipOp.Difference) {
                            drawRect(SolidColor(Color.Black.copy(alpha = 0.5f)))
                        }
                    }

                    // Crop rectangle border
                    drawCropRectangleView(
                        guideLineColor = guideLineColor,
                        guideLineWidth = guideLineWidth,
                        iRect = cropU.iRect
                    )

                    // Rule-of-thirds grid
                    if (showGuideLines) {
                        drawGuideLines(
                            noOfGuideLines = 2,
                            guideLineColor = guideLineColor,
                            guideLineWidth = guideLineWidth,
                            iRect = cropU.iRect
                        )
                    }

                    // Corner edge markers
                    if (edgeType == EdgeType.CIRCULAR) {
                        drawCircularEdges(
                            edgeCircleSize = edgeCircleSize,
                            guideLineColor = guideLineColor,
                            iRect = cropU.iRect
                        )
                    } else {
                        drawSquareBrackets(
                            guideLineColor = guideLineColor,
                            guideLineWidthGiven = guideLineWidth,
                            iRect = cropU.iRect
                        )
                    }
                }
            )
        }
    }

    /**
     * Implements the [OnCrop] interface method to perform cropping and return the resulting [Bitmap].
     * @param cropSourceImage [Boolean] If true, crop the source image, if false, crop the scaled image in the canvas
     *
     * @return The cropped [Bitmap] based on the current crop view configuration.
     */
    override fun onCrop(cropSourceImage: Boolean): Bitmap {
        this.cropU.updateBitmapImage(bitmapImage)
        return if (cropSourceImage) {
            this.cropU.cropSourceImage()
        } else {
            this.cropU.cropImage()
        }
    }

    /**
     * Implements the [OnCrop] interface method to reset the crop view to its initial state.
     * Also resets zoom and pan to 1x.
     */
    override fun resetView() {
        this.cropU.resetZoom()
        this.cropU.resetCropIRect()
    }

    /**
     * Calculates the centroid (average position) of a list of pointer positions.
     */
    private fun calculateCentroid(positions: List<Offset>): Offset {
        if (positions.isEmpty()) return Offset.Zero
        var sumX = 0f
        var sumY = 0f
        for (pos in positions) {
            sumX += pos.x
            sumY += pos.y
        }
        return Offset(sumX / positions.size, sumY / positions.size)
    }

    /**
     * Calculates the average distance of pointer positions from their centroid.
     * Used to detect pinch scale changes frame-by-frame.
     */
    private fun calculateAverageDistance(positions: List<Offset>, centroid: Offset): Float {
        if (positions.size < 2) return 0f
        var totalDistance = 0f
        for (pos in positions) {
            val dx = pos.x - centroid.x
            val dy = pos.y - centroid.y
            totalDistance += sqrt(dx * dx + dy * dy)
        }
        return totalDistance / positions.size
    }
}

/**
 * Interface defining methods for cropping and resetting the crop view.
 */
public interface OnCrop {
    /**
     * Performs cropping and returns the resulting [Bitmap].
     * @param cropSourceImage [Boolean] If true, crop the source image, if false, crop the scaled image in the canvas
     *
     * @return The cropped [Bitmap] based on the current crop view configuration.
     */
    public fun onCrop(cropSourceImage: Boolean = false): Bitmap

    /**
     * Resets the crop view to its initial state.
     */
    public fun resetView()
}
