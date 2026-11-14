package com.image.cropview

/**
 *  - Defines the available crop types for [ImageCrop].
 *
 *  Each type determines the shape and aspect ratio constraint applied to the
 *  crop rectangle when the user interacts with the [ImageCrop.ImageCropView] composable.
 *
 *  - [FREE_STYLE] – No ratio constraint; the user can resize the crop rectangle to any proportion.
 *  - [SQUARE] – Locks the crop rectangle to a 1 : 1 aspect ratio.
 *  - [PROFILE_CIRCLE] – Same 1 : 1 ratio as [SQUARE], with a circular overlay mask.
 *  - [RATIO_3_2] – Locks the crop rectangle to a 3 : 2 (landscape) aspect ratio.
 *  - [RATIO_4_3] – Locks the crop rectangle to a 4 : 3 (standard) aspect ratio.
 *  - [RATIO_16_9] – Locks the crop rectangle to a 16 : 9 (widescreen) aspect ratio.
 *  - [RATIO_9_16] – Locks the crop rectangle to a 9 : 16 (portrait / stories) aspect ratio.
 *  - [CUSTOM_RATIO] – Locks the crop rectangle to a user-defined aspect ratio, specified by the `x` and `y` constructor parameters.
 */
public sealed class CropType(public val x: Float, public val y: Float) {
    public object FREE_STYLE : CropType(0f, 0f)
    public object SQUARE : CropType(1f, 1f)
    public object PROFILE_CIRCLE : CropType(1f, 1f)
    public object RATIO_3_2 : CropType(3f, 2f)
    public object RATIO_4_3 : CropType(4f, 3f)
    public object RATIO_16_9 : CropType(16f, 9f)
    public object RATIO_9_16 : CropType(9f, 16f)
    public class CUSTOM_RATIO(x: Float, y: Float) : CropType(x, y)

    /**
     *  - Returns the width-to-height aspect ratio for fixed-ratio crop types.
     *
     *  For [RATIO_3_2], [RATIO_4_3], [RATIO_16_9], and [RATIO_9_16] this returns the
     *  corresponding floating-point ratio (e.g. `16f / 9f` for [RATIO_16_9]).
     *
     *  For [CUSTOM_RATIO]  returns the ratio of the provided `x` and `y` values.
     *
     *  Returns `null` for [FREE_STYLE] (no constraint) and for [SQUARE] / [PROFILE_CIRCLE],
     *  which use their own dedicated 1 : 1 constraint logic in [CropUtil].
     *
     *  @return The width / height ratio as a [Float], or `null` if no aspect-ratio
     *          constraint applies through this method.
     */
    public fun aspectRatio(): Float? = when (this) {
        RATIO_3_2 ,
        RATIO_4_3 ,
        RATIO_16_9 ,
        RATIO_9_16 ,
        is CUSTOM_RATIO -> x / y
        else -> null
    }
}