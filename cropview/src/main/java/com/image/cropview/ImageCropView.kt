package com.image.cropview

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


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
     *  - Composable function to display the crop view and handle user interactions.
     *
     *  @param modifier The modifier for configuring the layout and appearance of the crop view.
     *  @param guideLineColor The color of guide lines and rectangles in the crop view.
     *  @param guideLineWidth The width of guide lines in the crop view.
     *  @param edgeCircleSize The size of circular markers the crop rectangle.
     *  @param showGuideLines Handle the visibility of the guidelines.
     *  @param cropType [CropType]
     *  @param edgeType [EdgeType]
     */
    @Composable
    public fun ImageCropView(
        modifier: Modifier = Modifier,
        guideLineColor: Color = Color(0xFFD1CBE2),
        guideLineWidth: Dp = 2.dp,
        edgeCircleSize: Dp = 8.dp,
        showGuideLines: Boolean = true,
        cropType: CropType = CropType.FREE_STYLE,
        edgeType: EdgeType = EdgeType.CIRCULAR
    ) {

        val cropUtil by remember { mutableStateOf(CropUtil(bitmapImage)) }
        cropU = cropUtil

        if (cropU.cropType == null || cropType != cropU.cropType) {
            cropU.updateCropType(cropType)
        }

        Canvas(
            modifier = modifier
                .fillMaxSize()
                .onSizeChanged { intSize ->
                    cropUtil.onCanvasSizeChanged(intSize = intSize)
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { touchPoint ->
                            cropUtil.onDragStart(touchPoint)
                        },
                        onDrag = { pointerInputChange, _ ->
                            // consume the drag points and update the rect
                            pointerInputChange.consume()

                            val dragPoint = pointerInputChange.position
                            cropUtil.onDrag(dragPoint)
                        },
                        onDragEnd = {
                            cropU.onDragEnd()
                        }
                    )
                },

            onDraw = {
                // Draw bitmap image on rect
                drawBitmap(
                    bitmap = bitmapImage,
                    canvasSize = cropUtil.canvasSize
                )

                // Circle
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

                // Actual crop view rect
                drawCropRectangleView(
                    guideLineColor = guideLineColor,
                    guideLineWidth = guideLineWidth,
                    iRect = cropU.iRect
                )

                if (showGuideLines) {
                    drawGuideLines(
                        noOfGuideLines = 2,
                        guideLineColor = guideLineColor,
                        guideLineWidth = guideLineWidth,
                        iRect = cropU.iRect
                    )
                }

                // Circular edges of crop rect corner
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
     */
    override fun resetView() {
        this.cropU.resetCropIRect()
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
