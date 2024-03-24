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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
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
     *  @param edgeCircleSize The size of circular markers at the edges and corners of the crop rectangle.
     */
    @Composable
    public fun ImageCropView(
        modifier: Modifier = Modifier,
        guideLineColor: Color = Color.Green,
        guideLineWidth: Dp = 2.dp,
        edgeCircleSize: Dp = 8.dp,
        showGuideLines: Boolean = true,
        cropType: CropType = CropType.FREE_STYLE
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
                // Draw or Show image on rect
                val bm =
                    Bitmap.createScaledBitmap(
                        bitmapImage,
                        cropUtil.canvasSize.canvasWidth.toInt(),
                        cropUtil.canvasSize.canvasHeight.toInt(),
                        false
                    )
                drawImage(image = bm.asImageBitmap())

                // Actual rect
                drawRect(
                    color = guideLineColor,
                    size = cropU.iRect.size,
                    topLeft = cropU.iRect.topLeft,
                    style = Stroke(guideLineWidth.toPx()),
                )

                if (showGuideLines) {
                    // Vertical lines
                    val verticalDiff = cropU.iRect.size.height / 3
                    drawLine(
                        color = guideLineColor,
                        start = Offset(
                            cropU.iRect.topLeft.x,
                            (cropU.iRect.topLeft.y + verticalDiff)
                        ),
                        end = Offset(
                            (cropU.iRect.topLeft.x + cropU.iRect.size.width),
                            (cropU.iRect.topLeft.y + verticalDiff)
                        ),
                        strokeWidth = guideLineWidth.toPx(),
                    )
                    drawLine(
                        color = guideLineColor,
                        start = Offset(
                            cropU.iRect.topLeft.x,
                            (cropU.iRect.topLeft.y + (verticalDiff * 2))
                        ),
                        end = Offset(
                            (cropU.iRect.topLeft.x + cropU.iRect.size.width),
                            (cropU.iRect.topLeft.y + (verticalDiff * 2))
                        ),
                        strokeWidth = guideLineWidth.toPx(),
                    )

                    // Horizontal lines
                    val horizontalDiff = cropU.iRect.size.width / 3
                    drawLine(
                        color = guideLineColor,
                        start = Offset(
                            (cropU.iRect.topLeft.x + horizontalDiff),
                            cropU.iRect.topLeft.y
                        ),
                        end = Offset(
                            (cropU.iRect.topLeft.x + horizontalDiff),
                            (cropU.iRect.topLeft.y + cropU.iRect.size.height)
                        ),
                        strokeWidth = guideLineWidth.toPx(),
                    )

                    drawLine(
                        color = guideLineColor,
                        start = Offset(
                            (cropU.iRect.topLeft.x + (horizontalDiff * 2)),
                            cropU.iRect.topLeft.y
                        ),
                        end = Offset(
                            (cropU.iRect.topLeft.x + (horizontalDiff * 2)),
                            (cropU.iRect.topLeft.y + cropU.iRect.size.height)
                        ),
                        strokeWidth = guideLineWidth.toPx(),
                    )
                }

                // Rect edges
                // edge 1
                drawCircle(
                    color = guideLineColor,
                    center = cropU.iRect.topLeft,
                    radius = edgeCircleSize.toPx()
                )

                // edge 2
                drawCircle(
                    color = guideLineColor,
                    center = Offset(
                        (cropU.iRect.topLeft.x + cropU.iRect.size.width),
                        cropU.iRect.topLeft.y
                    ),
                    radius = edgeCircleSize.toPx()
                )


                // edge 3
                drawCircle(
                    color = guideLineColor,
                    center = Offset(
                        cropU.iRect.topLeft.x,
                        (cropU.iRect.topLeft.y + cropU.iRect.size.height)
                    ),
                    radius = edgeCircleSize.toPx()
                )

                // edge 4
                drawCircle(
                    color = guideLineColor,
                    center = Offset(
                        (cropU.iRect.topLeft.x + cropU.iRect.size.width),
                        (cropU.iRect.topLeft.y + cropU.iRect.size.height)
                    ),
                    radius = edgeCircleSize.toPx()
                )
            }
        )
    }


    /**
     * Implements the [OnCrop] interface method to perform cropping and return the resulting [Bitmap].
     *
     * @return The cropped [Bitmap] based on the current crop view configuration.
     */
    override fun onCrop(): Bitmap {
        return this.cropU.cropImage()
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
private interface OnCrop {
    /**
     * Performs cropping and returns the resulting [Bitmap].
     *
     * @return The cropped [Bitmap] based on the current crop view configuration.
     */
    fun onCrop(): Bitmap

    /**
     * Resets the crop view to its initial state.
     */
    fun resetView()
}