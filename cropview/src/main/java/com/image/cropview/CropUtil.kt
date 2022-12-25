package com.image.cropview

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 *  Crop util for handling events and changes from image crop view.
 *  @param bitmapImage is bitmap to crop the image no need to set it will updated from ImageCropView.
 *
 */
public class CropUtil constructor(public var bitmapImage: Bitmap) {


    //
    //  # Setter :
    //      Private set - is used to avoid set the values from outside this class
    //

    /**
     *  Canvas size of crop view has to set to crop image later by mapping the crop view rect.
     *
     */
    public var canvasWidth: Int by mutableStateOf(0)
    public var canvasHeight: Int by mutableStateOf(0)

    /**
     *  To handle minimum rect to be cropped from image
     */
    public var widthDiffInEdges: Float by mutableStateOf(0.0f)
        private set
    public var heightDiffInEdges: Float by mutableStateOf(0.0f)
        private set

    /**
     *  Edge of cropping rect point at top-left
     */
    public var circleOne: Offset by mutableStateOf(Offset(0.0f, 0.0f))
        private set

    /**
     *  Edge of cropping rect point at top-right
     */
    public var circleTwo: Offset by mutableStateOf(Offset(0.0f, 0.0f))
        private set

    /**
     *  Edge of cropping rect point at bottom-left
     */
    public var circleThree: Offset by mutableStateOf(Offset(0.0f, 0.0f))
        private set

    /**
     *  Edge of cropping rect point at bottom-right
     */
    public var circleFour: Offset by mutableStateOf(Offset(0.0f, 0.0f))
        private set

    /**
     *  Grid lines in rect currently only four grid lines
     */
    public var guideLineOne: GuideLinePoints by mutableStateOf(GuideLinePoints())
        private set
    public var guideLineTwo: GuideLinePoints by mutableStateOf(GuideLinePoints())
        private set
    public var guideLineThree: GuideLinePoints by mutableStateOf(GuideLinePoints())
        private set
    public var guideLineFour: GuideLinePoints by mutableStateOf(GuideLinePoints())
        private set


    init {
        canvasWidth = bitmapImage.width
        canvasHeight = bitmapImage.height

        resetCropRect()

    }

    /**
     *  Update crop size
     */
    public fun updateCanvasSizeChange(width: Int, height: Int) {
        this.canvasWidth = width
        this.canvasHeight = height
        this.widthDiffInEdges = width / 7f
        this.heightDiffInEdges = height / 7f
        resetCropRect()

    }

    /**
     *  Function to crop the bitmap respected to new rect bounds to crop
     *
     */
    public fun cropImage(): Bitmap {

        val rect = getRectFromPoints()

        val bitmap: Bitmap = Bitmap.createScaledBitmap(bitmapImage, canvasWidth, canvasHeight, true)

        var imgLef = if (rect.left.toInt() < 0) 0 else rect.left.toInt()
        var imgTop = if (rect.top.toInt() < 0) 0 else rect.top.toInt()

        val imgWidth = if (rect.width.toInt() > canvasWidth) canvasWidth else rect.width.toInt()
        val imgHeight =
            if (rect.height.toInt() > canvasHeight) canvasHeight else rect.height.toInt()

        if (imgLef + imgWidth > canvasWidth) {
            imgLef = 0
        }
        if (imgTop + imgHeight > canvasHeight) {
            imgTop = abs(canvasHeight - imgHeight)
        }

        val cropBitmap = if (imgWidth <= 0 || imgHeight <= 0) {
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                canvasWidth,
                canvasHeight
            )
        } else {
            Bitmap.createBitmap(
                bitmap,
                imgLef,
                imgTop,
                imgWidth,
                imgHeight
            )
        }


        return Bitmap.createScaledBitmap(cropBitmap, canvasWidth, canvasHeight, true)

    }

    /**
     *  Update the edge rect at top-left while dragging
     *  @param offset changed offset of a top-left edge of rect
     */
    public fun updateCircleOne(offset: Offset) {

        val diffX = abs(circleTwo.x - offset.x)
        val diffY = abs(circleThree.y - offset.y)

        if (diffX >= widthDiffInEdges && diffY > heightDiffInEdges && offset.y < circleThree.y && offset.x < circleTwo.x) {


            this.circleOne = offset
            this.circleTwo = Offset(circleTwo.x, offset.y)
            this.circleThree = Offset(offset.x, circleThree.y)

            updateGuideLineOne()
            updateGuideLineTwo()
            updateGuideLineThree()
            updateGuideLineFour()
        }
    }

    /**
     *  Update the edge rect at top-right while dragging
     *  @param offset changed offset of a top-right edge of rect
     */
    public fun updateCircleTwo(offset: Offset) {
        val diffX = abs(offset.x - circleOne.x)
        val diffY = abs(circleFour.y - offset.y)

        if (diffX >= widthDiffInEdges && diffY > heightDiffInEdges && offset.y < circleFour.y && offset.x > circleOne.x) {

            this.circleTwo = offset
            this.circleOne = Offset(circleOne.x, offset.y)
            this.circleFour = Offset(offset.x, circleFour.y)
            updateGuideLineOne()
            updateGuideLineTwo()
            updateGuideLineThree()
            updateGuideLineFour()
        }
    }

    /**
     *  Update the edge rect at bottom-left while dragging
     *  @param offset changed offset of a bottom-left edge of rect
     */
    public fun updateCircleThree(offset: Offset) {
        val diffX = abs(offset.x - circleFour.x)
        val diffY = abs(circleOne.y - offset.y)

        if (diffX >= widthDiffInEdges && diffY > heightDiffInEdges && offset.y > circleOne.y && offset.x < circleFour.x) {

            this.circleThree = offset
            this.circleOne = Offset(offset.x, circleOne.y)
            this.circleFour = Offset(circleFour.x, offset.y)
            updateGuideLineOne()
            updateGuideLineTwo()
            updateGuideLineThree()
            updateGuideLineFour()
        }

    }

    /**
     *  Update the edge rect at bottom-right while dragging
     *  @param offset changed offset of a bottom-right edge of rect
     */
    public fun updateCircleFour(offset: Offset) {
        val diffX = abs(offset.x - circleThree.x)
        val diffY = abs(circleTwo.y - offset.y)

        if (diffX >= widthDiffInEdges && diffY > heightDiffInEdges && offset.y > circleTwo.y && offset.x > circleThree.x) {

            this.circleFour = offset
            this.circleTwo = Offset(offset.x, circleTwo.y)
            this.circleThree = Offset(circleThree.x, offset.y)
            updateGuideLineOne()
            updateGuideLineTwo()
            updateGuideLineThree()
            updateGuideLineFour()
        }
    }

    /**
     *  Move line one - top line of rect
     */
    public fun moveLineOne(offset: Offset) {
        val diffX = abs(offset.x - circleThree.x)
        val diffY = abs(circleFour.y - offset.y)

        if (diffX >= widthDiffInEdges && diffY > heightDiffInEdges && offset.y <= circleThree.y) {
            this.circleOne = Offset(circleOne.x, offset.y)
            this.circleTwo = Offset(circleTwo.x, offset.y)

            updateGuideLineOne()
            updateGuideLineTwo()
            updateGuideLineThree()
            updateGuideLineFour()
        }

    }

    /**
     *  Move line two - left line of rect
     */
    public fun moveLineTwo(offset: Offset) {
        val diffX = abs(offset.x - circleTwo.x)

        if (diffX >= widthDiffInEdges && offset.x <= circleTwo.x) {
            this.circleOne = Offset(offset.x, circleOne.y)
            this.circleThree = Offset(offset.x, circleThree.y)

            updateGuideLineOne()
            updateGuideLineTwo()
            updateGuideLineThree()
            updateGuideLineFour()
        }


    }

    /**
     *  Move line three - right line of rect
     */
    public fun moveLineThree(offset: Offset) {
        val diffX = abs(offset.x - circleOne.x)

        if (diffX >= widthDiffInEdges && offset.x > circleOne.x) {
            this.circleTwo = Offset(offset.x, circleTwo.y)
            this.circleFour = Offset(offset.x, circleFour.y)

            updateGuideLineOne()
            updateGuideLineTwo()
            updateGuideLineThree()
            updateGuideLineFour()
        }

    }

    /**
     *  Move line four - bottom line of rect
     */
    public fun moveLineFour(offset: Offset) {
        val diffY = abs(offset.y - circleOne.y)

        if (diffY > heightDiffInEdges && offset.y > circleOne.y) {
            this.circleFour = Offset(circleFour.x, offset.y)
            this.circleThree = Offset(circleThree.x, offset.y)

            updateGuideLineOne()
            updateGuideLineTwo()
            updateGuideLineThree()
            updateGuideLineFour()
        }

    }

    /**
     *  Updating the grid lines in rect as edges of rect changes on Y-axis lines
     */
    private fun updateGuideLineOne() {
        val diffY =
            sqrt((circleOne.x - circleThree.x).pow(2) + (circleOne.y - circleThree.y).pow(2)) / 3
        this.guideLineOne = GuideLinePoints(
            start = Offset(circleOne.x, circleThree.y - (diffY * 2)),
            end = Offset(circleTwo.x, circleTwo.y + diffY)
        )
    }

    /**
     *  Updating the grid lines in rect as edges of rect changes on Y-axis lines
     */
    private fun updateGuideLineTwo() {
        val diffY =
            sqrt((circleOne.x - circleThree.x).pow(2) + (circleOne.y - circleThree.y).pow(2)) / 3
        this.guideLineTwo = GuideLinePoints(
            start = Offset(circleOne.x, circleOne.y + (diffY * 2)),
            end = Offset(circleTwo.x, circleTwo.y + (diffY * 2))
        )
    }

    /**
     *  Updating the grid lines in rect as edges of rect changes on X-axis lines
     */
    private fun updateGuideLineThree() {
        val diffX =
            sqrt((circleOne.x - circleTwo.x).pow(2) + (circleOne.y - circleTwo.y).pow(2)) / 3
        this.guideLineThree = GuideLinePoints(
            start = Offset(circleOne.x + diffX, circleOne.y),
            end = Offset(circleThree.x + diffX, circleThree.y)
        )
    }

    /**
     *  Updating the grid lines in rect as edges of rect changes on X-axis lines
     */
    private fun updateGuideLineFour() {
        val diffX =
            sqrt((circleOne.x - circleTwo.x).pow(2) + (circleOne.y - circleTwo.y).pow(2)) / 3
        this.guideLineFour = GuideLinePoints(
            start = Offset(circleOne.x + (diffX + diffX), circleOne.y),
            end = Offset(circleThree.x + (diffX + diffX), circleThree.y)
        )
    }

    /**
     *  Reset the rectangle to start from initial
     */
    public fun resetCropRect() {
        updateCircleOne(Offset(0f, 0f))
        updateCircleTwo(Offset(canvasWidth.toFloat(), 0f))
        updateCircleThree(Offset(0f, canvasHeight.toFloat()))
        updateCircleFour(Offset(canvasWidth.toFloat(), canvasHeight.toFloat()))

    }

    //
    //           x
    // left/top  #----------------#
    //        y  |                |
    //           |                |
    //           |                |
    //           |                |  height
    //           #----------------#  right/bottom
    //                       width
    //
    public fun getRectFromPoints(): Rect = Rect(
        circleOne.x,    //left
        circleOne.y,    //top
        circleFour.x,   //right
        circleFour.y,   //bottom
    )

}