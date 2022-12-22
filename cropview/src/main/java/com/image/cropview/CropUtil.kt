package com.image.cropview

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.pow
import kotlin.math.sqrt

/**
 *  Crop util for handling events and changes from image crop view.
 *  @param bitmapImage to crop the image no need to set it will updated from ImageCropView.
 *
 */

class CropUtil(bitmapImage: Bitmap) {

    //
    //  # Setter :
    //      Private set - is used to avoid set the values from outside this class
    //

    /**
     *  Canvas size of crop view has to set to crop image later by mapping the crop view rect.
     *
     *
     */
    var canvasWidth: Int = bitmapImage.width
        private set
    var canvasHeight: Int = bitmapImage.height
        private set

    /**
     *  To handle minimum rect to be cropped from image
     */
    var widthDiffInEdges: Float = 0.0F
        private set
    var heightDiffInEdges: Float = 0.0F
        private set

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