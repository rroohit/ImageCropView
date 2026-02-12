plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        // languageVersion.set(JavaLanguageVersion.of(17))
        languageVersion.set(JavaLanguageVersion.of(24))

    }
}

