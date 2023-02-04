plugins {
    id("android-application-plugin")
}

android {
    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion = Versions.composeCompiler
}

dependencies {
    implementation(project(":core"))
    kotlin()
    baseAndroid()
    compose()
}
