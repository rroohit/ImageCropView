# Jetpack-Compose ImageCropView

Image crop view for jetpack compose.


https://user-images.githubusercontent.com/36406595/209850544-a7fd3c14-5cef-4aa3-8f66-fa7626c7cd06.mp4


## Including in your project 


[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.rroohit/ImageCropView/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.rroohit/ImageCropView)

Add this dependency to your **module-level** `build.gradle` in `dependencies` section :

```groovy
dependencies {
    //...other repositories

    implementation 'io.github.rroohit:ImageCropView:2.1.0'
    
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
    modifier = Modifier,            //must provide with size
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
