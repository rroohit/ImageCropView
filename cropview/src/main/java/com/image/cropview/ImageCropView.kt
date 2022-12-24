package com.image.cropview

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 *  ImageCrop which is responsible to draw all rect and edges on bitmap
 *  @param bitmapImage Bitmap which has to cropped
 *
 */
class ImageCrop(
    var bitmapImage: Bitmap
) : OnCrop {

    /**
     *  Initializing crop util to handle all dragging and cropping events
     */
    private val cropU = CropUtil(bitmapImage)

    /**
     *  To track the rect edge which has to drag
     */
//    var selectedEdge: SelectedDraggablePoints = SelectedDraggablePoints.NULL
//        private set

    /**
     *  Touch area for rect edge
     */
    val edgeTouchArea: Float
        get() = 120f


    @Composable
    fun ImageCropView(
        modifier: Modifier = Modifier,
        guideLineColor: Color = Color.LightGray,
        guideLineWidth: Dp = 2.dp,
        edgeCircleSize: Dp = 8.dp,
    ) {
        var selectedEdge by remember { mutableStateOf(SelectedDraggablePoints.NULL) }
        val cropUtil by remember { mutableStateOf(cropU) }


        Canvas(
            modifier = modifier
                .onSizeChanged {
                    cropUtil.updateBitmapSizeChange(it.width, it.height)
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { onTouchPointOffset ->
                            if (onTouchPointOffset.x - cropUtil.circleOne.x < edgeTouchArea &&
                                onTouchPointOffset.x - cropUtil.circleOne.x > -edgeTouchArea &&
                                onTouchPointOffset.y - cropUtil.circleOne.y < edgeTouchArea &&
                                onTouchPointOffset.y - cropUtil.circleOne.y > -edgeTouchArea
                            ) {
                                selectedEdge = SelectedDraggablePoints.ONE

                            } else if (onTouchPointOffset.x - cropUtil.circleTwo.x < edgeTouchArea &&
                                onTouchPointOffset.x - cropUtil.circleTwo.x > -edgeTouchArea &&
                                onTouchPointOffset.y - cropUtil.circleTwo.y < edgeTouchArea &&
                                onTouchPointOffset.y - cropUtil.circleTwo.y > -edgeTouchArea
                            ) {
                                selectedEdge = SelectedDraggablePoints.TWO

                            } else if (onTouchPointOffset.x - cropUtil.circleThree.x < edgeTouchArea &&
                                onTouchPointOffset.x - cropUtil.circleThree.x > -edgeTouchArea &&
                                onTouchPointOffset.y - cropUtil.circleThree.y < edgeTouchArea &&
                                onTouchPointOffset.y - cropUtil.circleThree.y > -edgeTouchArea
                            ) {

                                selectedEdge = SelectedDraggablePoints.THREE

                            } else if (onTouchPointOffset.x - cropUtil.circleFour.x < edgeTouchArea &&
                                onTouchPointOffset.x - cropUtil.circleFour.x > -edgeTouchArea &&
                                onTouchPointOffset.y - cropUtil.circleFour.y < edgeTouchArea &&
                                onTouchPointOffset.y - cropUtil.circleFour.y > -edgeTouchArea
                            ) {
                                selectedEdge = SelectedDraggablePoints.FOUR

                            } else {
                                selectedEdge = SelectedDraggablePoints.NULL
                            }

                        },
                        onDrag = { pointerInputChange, _ ->
                            pointerInputChange.consume()
                            if (pointerInputChange.position.x < size.width &&
                                pointerInputChange.position.y < size.height &&
                                pointerInputChange.position.x > 0f &&
                                pointerInputChange.position.y > 0f
                            ) {

                                when (selectedEdge) {
                                    SelectedDraggablePoints.NULL -> Unit
                                    SelectedDraggablePoints.ONE -> {
                                        val newOffset = Offset(
                                            pointerInputChange.position.x,
                                            pointerInputChange.position.y
                                        )
                                        cropUtil.updateCircleOne(newOffset)
                                    }
                                    SelectedDraggablePoints.TWO -> {
                                        val newOffset = Offset(
                                            pointerInputChange.position.x,
                                            pointerInputChange.position.y
                                        )
                                        cropUtil.updateCircleTwo(newOffset)
                                    }
                                    SelectedDraggablePoints.THREE -> {
                                        val newOffset = Offset(
                                            pointerInputChange.position.x,
                                            pointerInputChange.position.y
                                        )
                                        cropUtil.updateCircleThree(newOffset)
                                    }
                                    SelectedDraggablePoints.FOUR -> {
                                        val newOffset = Offset(
                                            pointerInputChange.position.x,
                                            pointerInputChange.position.y
                                        )
                                        cropUtil.updateCircleFour(newOffset)
                                    }
                                    SelectedDraggablePoints.LINEONE -> {
                                        val moveOffset = Offset(
                                            pointerInputChange.position.x,
                                            pointerInputChange.position.y
                                        )
                                        cropUtil.moveLineOne(moveOffset)
                                    }
                                    SelectedDraggablePoints.LINETWO -> {
                                        val moveOffset = Offset(
                                            pointerInputChange.position.x,
                                            pointerInputChange.position.y
                                        )
                                        cropUtil.moveLineTwo(moveOffset)
                                    }
                                    SelectedDraggablePoints.LINETHREE -> {
                                        val moveOffset = Offset(
                                            pointerInputChange.position.x,
                                            pointerInputChange.position.y
                                        )
                                        cropUtil.moveLineThree(moveOffset)
                                    }
                                    SelectedDraggablePoints.LINEFOUR -> {
                                        val moveOffset = Offset(
                                            pointerInputChange.position.x,
                                            pointerInputChange.position.y
                                        )
                                        cropUtil.moveLineFour(moveOffset)
                                    }
                                    SelectedDraggablePoints.DRAGRECT -> {
                                        //todo drag an rect when drag starts from rect center
                                    }
                                }


                            }

                        },
                        onDragEnd = {
                            selectedEdge = SelectedDraggablePoints.NULL
                        }
                    )


                },
            onDraw = {
                // Draw or Show image on rect
                val bm =
                    Bitmap.createScaledBitmap(
                        bitmapImage,
                        cropUtil.canvasWidth,
                        cropUtil.canvasHeight,
                        false
                    )
                drawImage(image = bm.asImageBitmap())

                // Rect edges
                // edge 1
                drawCircle(
                    color = guideLineColor,
                    center = cropUtil.circleOne,
                    radius = edgeCircleSize.toPx()
                )

                // edge 2
                drawCircle(
                    color = guideLineColor,
                    center = cropUtil.circleTwo,
                    radius = edgeCircleSize.toPx()
                )

                // edge 3
                drawCircle(
                    color = guideLineColor,
                    center = cropUtil.circleThree,
                    radius = edgeCircleSize.toPx()
                )

                // edge 4
                drawCircle(
                    color = guideLineColor,
                    center = cropUtil.circleFour,
                    radius = edgeCircleSize.toPx()
                )

                // Rect borders
                drawRoundRect(
                    color = guideLineColor,
                    topLeft = cropUtil.circleOne,
                    style = Stroke(width = guideLineWidth.toPx()),
                    size = Size(cropUtil.getRectFromPoints().width, cropUtil.getRectFromPoints().height ),
                )


                // Rect center guideLines
                val path = Path().apply {

                    // line 1
                    drawLine(
                        color = guideLineColor,
                        start = cropUtil.guideLineOne.start,
                        end = cropUtil.guideLineOne.end,
                        strokeWidth = guideLineWidth.toPx()
                    )

                    // line 2
                    drawLine(
                        color = guideLineColor,
                        start = cropUtil.guideLineTwo.start,
                        end = cropUtil.guideLineTwo.end,
                        strokeWidth = guideLineWidth.toPx()
                    )

                    // line 3
                    drawLine(
                        color = guideLineColor,
                        start = cropUtil.guideLineThree.start,
                        end = cropUtil.guideLineThree.end,
                        strokeWidth = guideLineWidth.toPx()
                    )

                    // line 4
                    drawLine(
                        color = guideLineColor,
                        start = cropUtil.guideLineFour.start,
                        end = cropUtil.guideLineFour.end,
                        strokeWidth = guideLineWidth.toPx()
                    )
                }
                drawPath(path = path, color = guideLineColor)

            }
        )

    }

    override fun onCrop(): Bitmap {
        return cropU.cropImage()
    }

    override fun resetView() {
        cropU.resetCropRect()
    }

}

interface OnCrop {
    fun onCrop(): Bitmap
    fun resetView()

}