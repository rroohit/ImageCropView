# Jetpack Compose ImageCropView


ImageCropView is a Jetpack Compose library that provides a simple and customizable image cropping view for Android applications.

It supports various cropping styles such as free-form, square, circular, and fixed aspect-ratio cropping (3:2, 4:3, 16:9, 9:16), pinch-to-zoom, and crop state that survives device rotation — making it easy to integrate image cropping into your Compose UI.



https://github.com/user-attachments/assets/0feb8c47-e96c-455a-877a-ffa82378630a


## Features

- 🎯 **Multiple crop types** — free-style, square, circular profile, and fixed aspect ratios (3:2, 4:3, 16:9, 9:16).
- 🔍 **Zoom & pan** — optional pinch-to-zoom and double-tap zoom on the source image.
- 🔄 **Rotation-safe state** — the crop rectangle, zoom, and pan survive device rotation and other configuration changes.
- 🎨 **Customizable overlay** — guide-line color/width, rule-of-thirds grid, and circular or square corner handles.
- 🖼️ **Flexible output** — crop the canvas-scaled image or the original full-resolution bitmap.


## Setup


[![Maven Central](https://img.shields.io/maven-central/v/io.github.rroohit/ImageCropView?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.rroohit/ImageCropView)

Add this dependency to your **module-level** `build.gradle` in the `dependencies` section:

```groovy
dependencies {
    implementation 'io.github.rroohit:ImageCropView:4.1.0'
}
```

**Requirements:** `minSdk 24`, Jetpack Compose.


## Quick Start

1. Obtain an `ImageCrop` state holder from one of the factory composables.
2. Render the `ImageCropView` composable, passing the state holder in.
3. Call `onCrop()` on the state holder to get the cropped `Bitmap`.

```kotlin
@Composable
fun CropScreen(bitmap: Bitmap) {
    // Rotation-safe state holder — survives configuration changes.
    val imageCrop = rememberSaveableImageCrop(bitmap)

    ImageCropView(
        imageCrop = imageCrop,
        modifier = Modifier.fillMaxSize(),
        guideLineColor = Color.LightGray,
        guideLineWidth = 2.dp,
        edgeCircleSize = 5.dp,
        showGuideLines = true,
        cropType = CropType.SQUARE,
        edgeType = EdgeType.CIRCULAR,
        enableZoom = true
    )

    // When you want the result (e.g. on a button click):
    val croppedBitmap: Bitmap = imageCrop.onCrop()
}
```

> **Note:** In `4.1.0` the entry point changed from a method (`imageCrop.ImageCropView(...)`) to a
> top-level composable `ImageCropView(imageCrop = ..., ...)`, and `ImageCrop` is now created through
> the `rememberImageCrop` / `rememberSaveableImageCrop` factory composables rather than constructed directly.


## State Holders

`ImageCrop` holds all interactive crop state. Create it with one of these composables — don't construct it directly:

| Factory | Behavior |
|---------|----------|
| `rememberSaveableImageCrop(bitmap)` | Crop rectangle, zoom level, and pan offset **survive device rotation** and other Activity recreation. Recommended for most cases. |
| `rememberImageCrop(bitmap)` | In-process only; state resets when the Activity is recreated. |

Both recreate the state holder when the `bitmap` changes (e.g. the user loads a different image).

```kotlin
// Rotation-safe (recommended):
val imageCrop = rememberSaveableImageCrop(bitmap)

// In-process only:
val imageCrop = rememberImageCrop(bitmap)
```

If you need to call `onCrop()` / `resetView()` from outside the composition (e.g. a toolbar button
handler in the Activity), keep a reference to the returned `ImageCrop`:

```kotlin
private var imageCrop: ImageCrop? = null
// ...inside setContent:
val cropState = rememberSaveableImageCrop(bitmap)
imageCrop = cropState
ImageCropView(imageCrop = cropState, /* ... */)
// ...in a click handler:
val cropped = imageCrop?.onCrop()
```


## `ImageCropView` Parameters

| Parameter        | Type        | Default               | Description |
|------------------|-------------|-----------------------|-------------|
| `imageCrop`      | `ImageCrop` | —                     | State holder from `rememberSaveableImageCrop` / `rememberImageCrop`. |
| `modifier`       | `Modifier`  | `Modifier`            | Layout modifier applied to the crop view container. |
| `guideLineColor` | `Color`     | `Color(0xFFD1CBE2)`   | Color of the crop border and rule-of-thirds grid. |
| `guideLineWidth` | `Dp`        | `2.dp`                | Stroke width of the guide lines. |
| `edgeCircleSize` | `Dp`        | `8.dp`                | Radius of the corner drag handles (only for `EdgeType.CIRCULAR`). |
| `showGuideLines` | `Boolean`   | `true`                | Whether to draw the rule-of-thirds grid inside the crop rect. |
| `cropType`       | `CropType`  | `CropType.FREE_STYLE` | Aspect-ratio constraint applied to the crop rectangle. |
| `edgeType`       | `EdgeType`  | `EdgeType.CIRCULAR`   | Corner handle style — circular handles or square brackets. |
| `enableZoom`     | `Boolean`   | `false`               | Enables pinch-to-zoom and double-tap zoom on the image. |


## Getting the Cropped Image

`ImageCrop` exposes the crop operations through the `OnCrop` interface:

```kotlin
// Crop the canvas-scaled image (default):
val cropped: Bitmap = imageCrop.onCrop()

// Crop the original bitmap at full resolution:
val croppedFullRes: Bitmap = imageCrop.onCrop(cropSourceImage = true)

// Reset the crop rectangle and zoom to their initial state:
imageCrop.resetView()
```

| Method | Description |
|--------|-------------|
| `onCrop(cropSourceImage: Boolean = false)` | Returns the cropped `Bitmap`. When `cropSourceImage = true`, crops the original bitmap at full resolution; otherwise crops the canvas-scaled version. |
| `resetView()` | Resets the crop rectangle and zoom back to their default initial state. |


## Zoom

Set `enableZoom = true` to let users zoom into the source image while cropping:

- **Pinch** with two fingers to zoom and pan the image.
- **Double-tap** to toggle zoom in/out.

The crop rectangle, guide lines, and edge handles stay fixed on screen while the image zooms beneath them.

```kotlin
ImageCropView(
    imageCrop = imageCrop,
    modifier = Modifier.fillMaxSize(),
    enableZoom = true
)
```


## Crop Types

| Crop Type          | Description                              |
|--------------------|------------------------------------------|
| **FREE_STYLE**     | Free-form cropping (no ratio constraint) |
| **SQUARE**         | Square cropping (1:1)                    |
| **PROFILE_CIRCLE** | Circular view for profile pictures (1:1) |
| **RATIO_3_2**      | 3:2 landscape aspect ratio               |
| **RATIO_4_3**      | 4:3 standard aspect ratio                |
| **RATIO_16_9**     | 16:9 widescreen aspect ratio             |
| **RATIO_9_16**     | 9:16 portrait / stories aspect ratio     |

### Aspect Ratio Cropping

Use the `RATIO_*` crop types to constrain the crop rectangle to a fixed aspect ratio. The crop rectangle maintains the selected ratio during both initial placement and corner-drag resizing. The cropped output bitmap also preserves the ratio.

```kotlin
ImageCropView(
    imageCrop = imageCrop,
    modifier = Modifier.fillMaxSize(),
    cropType = CropType.RATIO_16_9,   // Widescreen crop
    edgeType = EdgeType.CIRCULAR
)

val croppedBitmap = imageCrop.onCrop()  // Output maintains the 16:9 ratio
```

Each `CropType` exposes an `aspectRatio()` method that returns the width-to-height ratio as a `Float?` (returns `null` for `FREE_STYLE`, `SQUARE`, and `PROFILE_CIRCLE`):

```kotlin
CropType.RATIO_16_9.aspectRatio()  // 1.7778 (16f / 9f)
CropType.RATIO_9_16.aspectRatio()  // 0.5625 (9f / 16f)
CropType.FREE_STYLE.aspectRatio()  // null
```


## Edge Types

`EdgeType` controls how the crop rectangle's corner handles are drawn:

| Edge Type  | Description                                                        |
|------------|--------------------------------------------------------------------|
| **CIRCULAR** | Circular drag handles at each corner (size set by `edgeCircleSize`). |
| **SQUARE**   | L-shaped square brackets at each corner.                           |


### License

```
Copyright 2022 rohit

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

```
