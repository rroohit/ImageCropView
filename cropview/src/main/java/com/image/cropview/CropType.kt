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
 */
public enum class CropType {
    FREE_STYLE,
    SQUARE,
    PROFILE_CIRCLE,
    RATIO_3_2,
    RATIO_4_3,
    RATIO_16_9,
    RATIO_9_16;

    /**
     *  - Returns the width-to-height aspect ratio for fixed-ratio crop types.
     *
     *  For [RATIO_3_2], [RATIO_4_3], [RATIO_16_9], and [RATIO_9_16] this returns the
     *  corresponding floating-point ratio (e.g. `16f / 9f` for [RATIO_16_9]).
     *
     *  Returns `null` for [FREE_STYLE] (no constraint) and for [SQUARE] / [PROFILE_CIRCLE],
     *  which use their own dedicated 1 : 1 constraint logic in [CropUtil].
     *
     *  @return The width / height ratio as a [Float], or `null` if no aspect-ratio
     *          constraint applies through this method.
     */
    public fun aspectRatio(): Float? = when (this) {
        RATIO_3_2 -> 3f / 2f
        RATIO_4_3 -> 4f / 3f
        RATIO_16_9 -> 16f / 9f
        RATIO_9_16 -> 9f / 16f
        else -> null
    }
}