# Jetpack-Compose ImageCropView

Image crop view for jetpack compose.



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
