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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
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


// ─────────────────────────────────────────────────────────────────────────────
// State holder
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Custom state holder for an image crop view (UI State Holder pattern).
 *
 * Encapsulates all interactive crop state and business logic. Obtain an instance via the
 * provided factory composables so the state is correctly scoped to the composition:
 *
 * ```kotlin
 * // Survives rotation and Activity recreation:
 * val imageCrop = rememberSaveableImageCrop(bitmap)
 *
 * // In-process only (resets on Activity recreation):
 * val imageCrop = rememberImageCrop(bitmap)
 *
 * // Render:
 * ImageCropView(imageCrop = imageCrop, cropType = CropType.SQUARE)
 *
 * // Retrieve result:
 * val croppedBitmap = imageCrop.onCrop()
 * ```
 *
 * @param bitmapImage The bitmap to crop. Internal so the companion [saver] and the
 *                    [ImageCropView] composable can access it within the module.
 */
public class ImageCrop(
    internal var bitmapImage: Bitmap
) : OnCrop {

    /** Internal state holder that owns all mutable crop geometry and zoom state. */
    internal val cropUtil: CropUtil = CropUtil(bitmapImage)

    override fun onCrop(cropSourceImage: Boolean): Bitmap {
        cropUtil.updateBitmapImage(bitmapImage)
        return if (cropSourceImage) cropUtil.cropSourceImage() else cropUtil.cropImage()
    }

    override fun resetView() {
        cropUtil.resetZoom()
        cropUtil.resetCropIRect()
    }

    public companion object {
        /**
         * Returns a [Saver] that persists this [ImageCrop]'s interactive state across
         * Activity recreation via Compose's [rememberSaveable] mechanism.
         *
         * **save** — called by the system during `onSaveInstanceState`; reads the live
         * [CropUtil] state and serialises it to a [CropStateSnapshot]. Returns `null`
         * when the canvas has not been laid out yet.
         *
         * **restore** — called during the new Activity's composition phase (before the canvas
         * is laid out); creates a fresh [ImageCrop] and pre-loads its [CropUtil] with the
         * snapshot. [CropUtil.onCanvasSizeChanged] then applies and rescales the snapshot the
         * first time it receives actual canvas dimensions.
         *
         * @param bitmap The bitmap the [ImageCrop] was created with, so the restored instance
         *               uses the same image.
         */
        public fun saver(bitmap: Bitmap): Saver<ImageCrop, Any> = Saver(
            save = { imageCrop -> imageCrop.cropUtil.toSnapshot() },
            restore = { snapshot ->
                ImageCrop(bitmap).also { imageCrop ->
                    if (snapshot is CropStateSnapshot && snapshot.bitmapWidth == bitmap.width && snapshot.bitmapHeight == bitmap.height) {
                        imageCrop.cropUtil.pendingSnapshot = snapshot
                    }
                }
            }
        )
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Factory composables
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Creates and [remember]s an [ImageCrop] instance for [bitmap].
 *
 * The instance is recreated whenever [bitmap] changes (e.g. the user loads a new image).
 * Crop state is **not** preserved across Activity recreation; use [rememberSaveableImageCrop]
 * for rotation-safe state.
 */
@Composable
public fun rememberImageCrop(bitmap: Bitmap): ImageCrop =
    remember(bitmap) { ImageCrop(bitmap) }

/**
 * Creates and [rememberSaveable]s an [ImageCrop] instance for [bitmap].
 *
 * The crop rectangle, zoom level, and pan offset survive device rotation and other Activity
 * recreation events via Compose's [rememberSaveable] + [ImageCrop.saver] mechanism.
 * The state persists across rotations; the saver validates bitmap dimensions and resets
 * state if a different image is loaded.
 */
@Composable
public fun rememberSaveableImageCrop(bitmap: Bitmap): ImageCrop =
    rememberSaveable(bitmap, saver = ImageCrop.saver(bitmap)) { ImageCrop(bitmap) }


// ─────────────────────────────────────────────────────────────────────────────
// Composable view
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Renders an interactive crop view for the image held by [imageCrop].
 *
 * The composable is stateless from the caller's perspective — all mutable state lives inside
 * [imageCrop]. Obtain [imageCrop] from [rememberSaveableImageCrop] (rotation-safe) or
 * [rememberImageCrop] (in-process only).
 *
 * @param imageCrop       State holder created by [rememberImageCrop] or [rememberSaveableImageCrop].
 * @param modifier        Layout modifier applied to the crop view container.
 * @param guideLineColor  Colour of the crop border and rule-of-thirds grid.
 * @param guideLineWidth  Stroke width of the guide lines.
 * @param edgeCircleSize  Radius of the corner drag handles (only for [EdgeType.CIRCULAR]).
 * @param showGuideLines  Whether to draw the rule-of-thirds grid inside the crop rect.
 * @param cropType        Aspect-ratio constraint applied to the crop rectangle; see [CropType].
 * @param edgeType        Corner handle style; see [EdgeType].
 * @param enableZoom      When `true`, enables pinch-to-zoom and double-tap zoom on the image.
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
public fun ImageCropView(
    imageCrop: ImageCrop,
    modifier: Modifier = Modifier,
    guideLineColor: Color = Color(0xFFD1CBE2),
    guideLineWidth: Dp = 2.dp,
    edgeCircleSize: Dp = 8.dp,
    showGuideLines: Boolean = true,
    cropType: CropType = CropType.FREE_STYLE,
    edgeType: EdgeType = EdgeType.CIRCULAR,
    enableZoom: Boolean = false
) {
    val cropUtil = imageCrop.cropUtil

    if (cropUtil.cropType == null || cropType != cropUtil.cropType) {
        cropUtil.updateCropType(cropType)
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val imageAspectRatio = imageCrop.bitmapImage.width.toFloat() / imageCrop.bitmapImage.height.toFloat()
        val containerAspectRatio = maxWidth.value / maxHeight.value

        val (targetWidth, targetHeight) = if (imageAspectRatio > containerAspectRatio) {
            // Image is wider than container — fit by width
            maxWidth to (maxWidth / imageAspectRatio)
        } else {
            // Image is taller than container — fit by height
            (maxHeight * imageAspectRatio) to maxHeight
        }

        Canvas(
            modifier = Modifier
                .size(width = targetWidth, height = targetHeight)
                .onSizeChanged { intSize ->
                    cropUtil.onCanvasSizeChanged(intSize = intSize)
                }
                .pointerInput(imageCrop, enableZoom) {
                    // lastTapTime / lastTapPosition live here — they persist across
                    // awaitEachGesture iterations but never trigger recomposition.
                    var lastTapTime = 0L
                    var lastTapPosition = Offset.Zero

                    if (!enableZoom) {
                        detectDragGestures(
                            onDragStart = { touchPoint ->
                                cropUtil.onDragStart(touchPoint)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                cropUtil.onDrag(change.position)
                            },
                            onDragEnd = {
                                cropUtil.onDragEnd()
                            }
                        )
                    } else {
                        // Unified zoom-aware gesture handler.
                        //
                        // Gesture modes:
                        //  0 = IDLE       – finger placed outside crop rect at 1× zoom
                        //  1 = CROP_DRAG  – single finger on/inside the crop rectangle
                        //  2 = IMAGE_PAN  – single finger outside crop rect while zoomed
                        //  3 = ZOOM       – two or more fingers (pinch + pan)
                        //
                        // Double-tap (two taps < 300 ms apart, < 80 px) toggles zoom.
                        val doubleTapThresholdMs = 300L
                        val doubleTapRadiusPx = 80f

                        awaitEachGesture {
                            val firstDown = awaitFirstDown(requireUnconsumed = false)
                            firstDown.consume()

                            val downTime = System.currentTimeMillis()
                            val downPosition = firstDown.position

                            val touchOnCropRect = cropUtil.isTouchOnCropRect(downPosition)
                            val zoomed = cropUtil.zoomScale > 1.0f

                            var gestureMode: Int
                            when {
                                touchOnCropRect -> {
                                    cropUtil.onDragStart(downPosition)
                                    gestureMode = 1
                                }
                                zoomed -> {
                                    cropUtil.onImagePanStart(downPosition)
                                    gestureMode = 2
                                }
                                else -> gestureMode = 0
                            }

                            var hasMoved = false
                            var zoomInitialized = false
                            var previousCentroid = Offset.Zero
                            var previousCentroidDistance = 0f

                            while (true) {
                                val event = awaitPointerEvent()
                                val changes = event.changes

                                if (changes.all { !it.pressed }) {
                                    when (gestureMode) {
                                        1 -> cropUtil.onDragEnd()
                                        2 -> cropUtil.onImagePanEnd()
                                    }

                                    if (!hasMoved && gestureMode != 3) {
                                        val upTime = System.currentTimeMillis()
                                        if ((upTime - downTime) < doubleTapThresholdMs) {
                                            val timeSinceLast = upTime - lastTapTime
                                            val d = downPosition - lastTapPosition
                                            val distFromLast = sqrt(d.x * d.x + d.y * d.y)
                                            if (timeSinceLast < doubleTapThresholdMs &&
                                                distFromLast < doubleTapRadiusPx
                                            ) {
                                                cropUtil.onDoubleTapZoom(downPosition)
                                                lastTapTime = 0L
                                            } else {
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
                                    if (gestureMode == 1) cropUtil.onDragEnd()
                                    if (gestureMode == 2) cropUtil.onImagePanEnd()
                                    gestureMode = 3

                                    val currentPositions = activeChanges.map { it.position }
                                    val currentCentroid = calculateCentroid(currentPositions)
                                    val currentDistance = calculateAverageDistance(
                                        currentPositions, currentCentroid
                                    )

                                    if (!zoomInitialized) {
                                        zoomInitialized = true
                                        previousCentroid = currentCentroid
                                        previousCentroidDistance = currentDistance
                                    } else {
                                        val scaleChange = if (previousCentroidDistance > 0f) {
                                            currentDistance / previousCentroidDistance
                                        } else 1f
                                        val panChange = currentCentroid - previousCentroid
                                        cropUtil.onZoomChange(currentCentroid, scaleChange, panChange)
                                        previousCentroid = currentCentroid
                                        previousCentroidDistance = currentDistance
                                    }
                                    activeChanges.forEach { it.consume() }

                                } else if (fingerCount == 1) {
                                    val change = activeChanges.first()

                                    if (gestureMode == 3) {
                                        // Dropping from pinch back to one finger: reset zoom
                                        // baseline and skip this frame to avoid a position jump.
                                        zoomInitialized = false
                                        change.consume()
                                        continue
                                    }

                                    if (change.positionChanged()) {
                                        hasMoved = true
                                        when (gestureMode) {
                                            1 -> { cropUtil.onDrag(change.position); change.consume() }
                                            2 -> { cropUtil.onImagePanDrag(change.position); change.consume() }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },

            onDraw = {
                // Clip the bitmap draw to canvas bounds so a zoomed image never
                // bleeds outside the view's allocated space.
                clipRect {
                    withTransform({
                        translate(left = cropUtil.zoomOffset.x, top = cropUtil.zoomOffset.y)
                        scale(
                            scaleX = cropUtil.zoomScale,
                            scaleY = cropUtil.zoomScale,
                            pivot = Offset(size.width / 2f, size.height / 2f)
                        )
                    }) {
                        drawBitmap(bitmap = imageCrop.bitmapImage, canvasSize = cropUtil.canvasSize)
                    }
                }

                // Overlay elements are drawn without the zoom transform so the crop
                // rectangle, guidelines and edge markers stay stable regardless of zoom.

                if (cropType == CropType.PROFILE_CIRCLE) {
                    val circleRadius = cropUtil.iRect.size.width / 2f
                    val circlePath = Path().apply {
                        addOval(
                            Rect(
                                center = Offset(
                                    cropUtil.iRect.topLeft.x + circleRadius,
                                    cropUtil.iRect.topLeft.y + circleRadius
                                ),
                                radius = circleRadius - guideLineWidth.toPx()
                            )
                        )
                    }
                    clipPath(circlePath, clipOp = ClipOp.Difference) {
                        drawRect(SolidColor(Color.Black.copy(alpha = 0.5f)))
                    }
                }

                drawCropRectangleView(
                    guideLineColor = guideLineColor,
                    guideLineWidth = guideLineWidth,
                    iRect = cropUtil.iRect
                )

                if (showGuideLines) {
                    drawGuideLines(
                        noOfGuideLines = 2,
                        guideLineColor = guideLineColor,
                        guideLineWidth = guideLineWidth,
                        iRect = cropUtil.iRect
                    )
                }

                if (edgeType == EdgeType.CIRCULAR) {
                    drawCircularEdges(
                        edgeCircleSize = edgeCircleSize,
                        guideLineColor = guideLineColor,
                        iRect = cropUtil.iRect
                    )
                } else {
                    drawSquareBrackets(
                        guideLineColor = guideLineColor,
                        guideLineWidthGiven = guideLineWidth,
                        iRect = cropUtil.iRect
                    )
                }
            }
        )
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Interface
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Provides imperative access to the crop operation and view reset.
 * Implemented by [ImageCrop].
 */
public interface OnCrop {
    /**
     * Performs the crop and returns the resulting [Bitmap].
     *
     * @param cropSourceImage When `true`, crops the original bitmap at full resolution.
     *                        When `false` (default), crops the canvas-scaled version.
     */
    public fun onCrop(cropSourceImage: Boolean = false): Bitmap

    /** Resets the crop rectangle and zoom to their default initial state. */
    public fun resetView()
}


// ─────────────────────────────────────────────────────────────────────────────
// Private geometry helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun calculateCentroid(positions: List<Offset>): Offset {
    if (positions.isEmpty()) return Offset.Zero
    var sumX = 0f
    var sumY = 0f
    for (pos in positions) { sumX += pos.x; sumY += pos.y }
    return Offset(sumX / positions.size, sumY / positions.size)
}

private fun calculateAverageDistance(positions: List<Offset>, centroid: Offset): Float {
    if (positions.size < 2) return 0f
    var total = 0f
    for (pos in positions) {
        val dx = pos.x - centroid.x
        val dy = pos.y - centroid.y
        total += sqrt(dx * dx + dy * dy)
    }
    return total / positions.size
}