plugins {
    id("android-application-plugin")
}

android {
    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion = Versions.composeCompiler
}

dependencies {
    implementation(project(":core"))
    implementation(project(":utils"))
    kotlin()
    baseAndroid()
    compose()
}
