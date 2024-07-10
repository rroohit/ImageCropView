# Jetpack Compose ImageCropView


ImageCropView is a Jetpack Compose library that provides a simple and customizable image cropping view for Android applications.

It supports various cropping styles such as free-form, square, and circular cropping, making it easy to integrate image cropping functionality into your Compose UI.


https://github.com/rroohit/ImageCropView/assets/36406595/d856d353-ab62-42c5-83db-ea99f8b4e8d8


## Setup


[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.rroohit/ImageCropView/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.rroohit/ImageCropView)

Add this dependency to your **module-level** `build.gradle` in `dependencies` section :

```groovy
dependencies {
    implementation 'io.github.rroohit:ImageCropView:3.0.0'
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

| Crop Type          | Description                        |
|--------------------|------------------------------------|
| **FREE_STYLE**     | Free-form cropping                 |
| **SQUARE**         | Square cropping                    |
| **PROFILE_CIRCLE** | Circular View for profile pictures |



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
