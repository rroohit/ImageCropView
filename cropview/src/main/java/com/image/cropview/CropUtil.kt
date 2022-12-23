package com.image.cropview

import android.graphics.Bitmap
import android.util.Log
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
class CropUtil(var bitmapImage: Bitmap) {

    //
    //  # Setter :
    //      Private set - is used to avoid set the values from outside this class
    //

    /**
     *  Canvas size of crop view has to set to crop image later by mapping the crop view rect.
     *
     */
    var canvasWidth: Int = bitmapImage.width
        private set
    var canvasHeight: Int = bitmapImage.height
        private set

    /**
     *  To handle minimum rect to be cropped from image
     */
    private var widthDiffInEdges: Float = bitmapImage.width / 7F
    private var heightDiffInEdges: Float = bitmapImage.height / 7F

    /**
     *  Edge of cropping rect point at top-left
     */
    var circleOne: Offset = Offset(0.0F, 0.0F)
        private set(value) {
            field = value //To set current variable circleOne value
            updateGuidLinesOnX()
            updateGuidLinesOnY()
        }

    /**
     *  Edge of cropping rect point at top-right
     */
    var circleTwo: Offset = Offset(canvasWidth.toFloat(), 0.0F)
        private set(value) {
            field = value //To set current variable circleTwo value
            updateGuidLinesOnX()
            updateGuidLinesOnY()
        }

    /**
     *  Edge of cropping rect point at bottom-left
     */
    var circleThree: Offset = Offset(0.0F, canvasHeight.toFloat())
        private set(value) {
            field = value //To set current variable circleThree value
            updateGuidLinesOnX()
            updateGuidLinesOnY()
        }

    /**
     *  Edge of cropping rect point at bottom-right
     */
    var circleFour: Offset = Offset(canvasWidth.toFloat(), canvasHeight.toFloat())
        private set(value) {
            field = value //To set current variable circleFour value
            updateGuidLinesOnX()
            updateGuidLinesOnY()
        }

    /**
     *  Grid lines in rect currently only four grid lines
     */
    var guideLineOne: GuideLinePoints = GuideLinePoints()
        private set
    var guideLineTwo: GuideLinePoints = GuideLinePoints()
        private set
    var guideLineThree: GuideLinePoints = GuideLinePoints()
        private set
    var guideLineFour: GuideLinePoints = GuideLinePoints()
        private set

    /**
     *  Update crop size
     */
    fun updateBitmapSizeChange(width: Int, height: Int) {
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
    fun cropImage(): Bitmap {
        val cropRect = getRectFromPoints()

        val bitmap = Bitmap.createScaledBitmap(bitmapImage, canvasWidth, canvasHeight, true)

        //Top rect point must be >= 0
        var imageLeft = if (cropRect.left.toInt() < 0) 0 else cropRect.left.toInt()
        var imageTop = if (cropRect.top.toInt() < 0) 0 else cropRect.top.toInt()

        //Check whether cropping bounds are within bitmap size
        val imageWidth =
            if (cropRect.width.toInt() > canvasWidth) canvasWidth else cropRect.width.toInt()
        val imageHeight =
            if (cropRect.height.toInt() > canvasHeight) canvasHeight else cropRect.height.toInt()

        //
        if (imageLeft + imageWidth > canvasWidth) {
            imageLeft = 0
        }
        //
        if (imageTop + imageHeight > canvasHeight) {
            imageTop = abs(canvasHeight - imageHeight)
        }

        val bitmapNew = if (imageWidth <= 0 || imageHeight <= 0) {
            bitmapImage
        } else {
            Bitmap.createBitmap(
                bitmap,
                imageLeft,
                imageTop,
                imageWidth,
                imageHeight
            )
        }

        val croppedImage =
            Bitmap.createScaledBitmap(bitmapNew, canvasWidth, canvasHeight, true)

        // Crop the bitmap in size of initial crop bounds width and height...
        return croppedImage ?: bitmapImage

    }

    /**
     *  Update the edge rect at top-left while dragging
     *  @param offset changed offset of a top-left edge of rect
     */
    fun updateCircleOne(offset: Offset) {
        val diffX = abs(circleTwo.x - offset.x)
        val diffY = abs(circleThree.y - offset.y)

        if (diffX >= widthDiffInEdges && diffY > heightDiffInEdges && offset.y < circleThree.y && offset.x < circleTwo.x) {
            Log.d("CROP_UTIL", "updateCircleOne: offset => $offset")

            this.circleOne = offset
            this.circleTwo = Offset(circleTwo.x, offset.y)
            this.circleThree = Offset(offset.x, circleThree.y)

        }

    }

    /**
     *  Update the edge rect at top-right while dragging
     *  @param offset changed offset of a top-right edge of rect
     */
    fun updateCircleTwo(offset: Offset) {
        val diffX = abs(offset.x - circleOne.x)
        val diffY = abs(circleFour.y - offset.y)

        if (diffX >= widthDiffInEdges && diffY > heightDiffInEdges && offset.y < circleFour.y && offset.x > circleOne.x) {
            this.circleTwo = offset
            this.circleOne = Offset(circleOne.x, offset.y)
            this.circleFour = Offset(offset.x, circleFour.y)

        }

    }

    /**
     *  Update the edge rect at bottom-left while dragging
     *  @param offset changed offset of a bottom-left edge of rect
     */
    fun updateCircleThree(offset: Offset) {
        val diffX = abs(offset.x - circleFour.x)
        val diffY = abs(circleOne.y - offset.y)

        if (diffX >= widthDiffInEdges && diffY > heightDiffInEdges && offset.y > circleOne.y && offset.x < circleFour.x) {
            this.circleThree = offset
            this.circleOne = Offset(offset.x, circleOne.y)
            this.circleFour = Offset(circleFour.x, offset.y)

        }

    }

    /**
     *  Update the edge rect at bottom-right while dragging
     *  @param offset changed offset of a bottom-right edge of rect
     */
    fun updateCircleFour(offset: Offset) {
        val diffX = abs(offset.x - circleThree.x)
        val diffY = abs(circleTwo.y - offset.y)

        if (diffX >= widthDiffInEdges && diffY > heightDiffInEdges && offset.y > circleTwo.y && offset.x > circleThree.x) {
            this.circleFour = offset
            this.circleTwo = Offset(offset.x, circleTwo.y)
            this.circleThree = Offset(circleThree.x, offset.y)

        }

    }

    /**
     *  Reset the rectangle to start from initial
     */
    fun resetCropRect() {
        circleOne = Offset(0.0F, 0.0F)
        circleTwo = Offset(canvasWidth.toFloat(), 0.0F)
        circleThree = Offset(0.0F, canvasHeight.toFloat())
        circleFour = Offset(canvasWidth.toFloat(), canvasHeight.toFloat())

    }

    /**
     *  Updating the grid lines in rect as edges of rect changes on Y-axis lines
     */
    private fun updateGuidLinesOnY() {
        val diffInYDirectionForGuideLine =
            sqrt((circleOne.x - circleThree.x).pow(2) + (circleOne.y - circleThree.y).pow(2)) / 3

        //Update Guide Line One on Y-axis
        guideLineOne = GuideLinePoints(
            start = Offset(circleOne.x, circleThree.y - (diffInYDirectionForGuideLine * 2)),
            end = Offset(circleTwo.x, circleTwo.y + diffInYDirectionForGuideLine)
        )

        //Update Guide Line Two on Y-axis
        guideLineTwo = GuideLinePoints(
            start = Offset(circleOne.x, circleOne.y + (diffInYDirectionForGuideLine * 2)),
            end = Offset(circleTwo.x, circleTwo.y + (diffInYDirectionForGuideLine * 2))
        )
    }

    /**
     *  Move line one - top line of rect
     */
    fun moveLineOne(offset: Offset) {
        val diffX = abs(offset.x - circleThree.x)
        val diffY = abs(circleFour.y - offset.y)

        if (diffX >= widthDiffInEdges && diffY > heightDiffInEdges && offset.y <= circleThree.y) {
            this.circleOne = Offset(circleOne.x, offset.y)
            this.circleTwo = Offset(circleTwo.x, offset.y)

        }

    }

    /**
     *  Move line two - left line of rect
     */
    fun moveLineTwo(offset: Offset) {
        val diffX = abs(offset.x - circleTwo.x)

        if (diffX >= widthDiffInEdges && offset.x <= circleTwo.x) {
            this.circleOne = Offset(offset.x, circleOne.y)
            this.circleThree = Offset(offset.x, circleThree.y)
        }

    }

    /**
     *  Move line three - right line of rect
     */
    fun moveLineThree(offset: Offset) {
        val diffX = abs(offset.x - circleOne.x)

        if (diffX >= widthDiffInEdges && offset.x > circleOne.x) {
            this.circleTwo = Offset(offset.x, circleTwo.y)
            this.circleFour = Offset(offset.x, circleFour.y)
        }

    }

    /**
     *  Move line four - bottom line of rect
     */
    fun moveLineFour(offset: Offset) {
        val diffY = abs(offset.y - circleOne.y)

        if (diffY > heightDiffInEdges && offset.y > circleOne.y) {
            this.circleFour = Offset(circleFour.x, offset.y)
            this.circleThree = Offset(circleThree.x, offset.y)
        }

    }

    /**
     *  Updating the grid lines in rect as edges of rect changes on X-axis lines
     */
    private fun updateGuidLinesOnX() {
        val diffInXDirectionForGuideLine =
            sqrt((circleOne.x - circleTwo.x).pow(2) + (circleOne.y - circleTwo.y).pow(2)) / 3

        //Update Guide Line One on X-axis
        guideLineThree = GuideLinePoints(
            start = Offset(circleOne.x + diffInXDirectionForGuideLine, circleOne.y),
            end = Offset(circleThree.x + diffInXDirectionForGuideLine, circleThree.y)
        )

        //Update Guide Line Two on X-axis
        guideLineFour = GuideLinePoints(
            start = Offset(circleOne.x + (diffInXDirectionForGuideLine * 2), circleOne.y),
            end = Offset(circleThree.x + (diffInXDirectionForGuideLine * 2), circleThree.y)
        )
    }

    //           x
    //left/top  #----------------#
    //       y  |                |
    //          |                |
    //          |                |
    //          |                |  height
    //          #----------------#  right/bottom
    //                      width
    //
    private fun getRectFromPoints(): Rect = Rect(
        circleOne.x,    //left
        circleOne.y,    //top
        circleFour.x,   //right
        circleFour.y,   //bottom
    )

}