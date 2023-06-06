plugins {
    id("android-application-plugin")
}

android {
    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion = Versions.composeCompiler
    namespace = "com.dawidraszka.composepermissionhandler.sample"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":utils"))
    kotlin()
    baseAndroid()
    compose()
}
