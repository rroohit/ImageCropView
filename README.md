# Jetpack-Compose ImageCropView

## Including in your project 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.rroohit/ImageCropView/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.rroohit/ImageCropView)

Add this dependency to your **module-level** `build.gradle` in `dependencies` section :

```groovy
dependencies {
    //...other repositories

    implementation 'io.github.rroohit:ImageCropView:1.0.0'
    
}
```

## How to use

First We have to create an instance of `ImageCrop(bitmap)` by passing an bitmap. 

Using `.ImageCropView(...)` you can customize your crop view, `.ImageCropView(...)` is jetpack compose `@Composable` function.

To get cropped bitmap call `.onCrop()`.

```kotlin

private lateinit var imageCrop: ImageCrop
imageCrop = ImageCrop(bitmap)

// You can customize ImageCropView with following attributes.
imageCrop.ImageCropView(
    modifier = Modifier,
    guideLineColor = Color.LightGray,
    guideLineWidth = 2.dp,
    edgeCircleSize = 5.dp
)

imageCrop.onCrop() //will return the cropped bitmap.

```