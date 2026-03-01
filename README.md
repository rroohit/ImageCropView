# Jetpack Compose ImageCropView


ImageCropView is a Jetpack Compose library that provides a simple and customizable image cropping view for Android applications.

It supports various cropping styles such as free-form, square, circular, and fixed aspect-ratio cropping (3:2, 4:3, 16:9, 9:16), making it easy to integrate image cropping functionality into your Compose UI.


https://github.com/rroohit/ImageCropView/assets/36406595/92906b61-277a-4bd6-a814-e3573ae36ee3


## Setup


[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.rroohit/ImageCropView/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.rroohit/ImageCropView)

Add this dependency to your **module-level** `build.gradle` in `dependencies` section :

```groovy
dependencies {
    implementation 'io.github.rroohit:ImageCropView:4.0.0'
}
```

## Usage

Create an instance of `ImageCrop(bitmap)` by passing an bitmap.

Using `.ImageCropView(...)` you can customize your crop view.
`.ImageCropView(...)` is jetpack compose `@Composable` function.

To get cropped bitmap invoke `.onCrop()`.

```kotlin

private lateinit var imageCrop: ImageCrop
imageCrop = ImageCrop(bitmap)

// Configure ImageCropView. 
imageCrop.ImageCropView(
    modifier = Modifier,            
    guideLineColor = Color.LightGray,
    guideLineWidth = 2.dp,
    edgeCircleSize = 5.dp,
    showGuideLines = true,
    cropType = CropType.SQUARE,
    edgeType = EdgeType.CIRCULAR
)

val croppedBitmap = imageCrop.onCrop() // To get the cropped image in bitmap format.

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
imageCrop.ImageCropView(
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
