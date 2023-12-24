package com.github.rroohit.imagecropview

object Configuration {
    const val compileSdk = 34
    const val targetSdk = 34
    const val minSdk = 21
    private const val majorVersion = 2
    private const val minorVersion = 0
    private const val patchVersion = 1
    const val versionName = "$majorVersion.$minorVersion.$patchVersion"
    const val versionCode = 3
    const val snapshotVersionName = "$majorVersion.$minorVersion.${patchVersion + 1}-SNAPSHOT"
    const val artifactGroup = "com.github.rroohit"
}