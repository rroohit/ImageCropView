package com.image.cropview

import java.io.Serializable

/**
 * Immutable snapshot of [ImageCrop] interactive state, serializable for persistence in Bundle.
 *
 * All spatial values are stored as normalised ratios (0..1) relative to the canvas dimensions
 * at the time of capture. This makes the snapshot canvas-size-independent: it scales correctly
 * when restored onto a canvas with different dimensions (e.g. after an orientation change).
 *
 * @param topLeftXRatio    cropRect.topLeft.x  / canvasWidth
 * @param topLeftYRatio    cropRect.topLeft.y  / canvasHeight
 * @param widthRatio       cropRect.width      / canvasWidth
 * @param heightRatio      cropRect.height     / canvasHeight
 * @param zoomScale        zoom scale factor (dimensionless — no normalisation needed)
 * @param zoomOffsetXRatio zoomOffset.x        / canvasWidth
 * @param zoomOffsetYRatio zoomOffset.y        / canvasHeight
 * @param bitmapWidth      source bitmap pixel width  — used to verify the snapshot belongs to the same image
 * @param bitmapHeight     source bitmap pixel height — used to verify the snapshot belongs to the same image
 */
public data class CropStateSnapshot(
    val topLeftXRatio: Float,
    val topLeftYRatio: Float,
    val widthRatio: Float,
    val heightRatio: Float,
    val zoomScale: Float,
    val zoomOffsetXRatio: Float,
    val zoomOffsetYRatio: Float,
    val bitmapWidth: Int,
    val bitmapHeight: Int
) : Serializable