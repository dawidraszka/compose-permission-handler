plugins {
    id("android-library-plugin")
    id(AndroidConfig.Plugin.mavenPublish)
}

publishComposePermissionHandler("core")

android {
    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion = Versions.composeCompiler
    namespace = "com.dawidraszka.composepermissionhandler.core"
}

dependencies {
    implementation(Libs.AndroidX.Compose.ui)
    implementation(Libs.AndroidX.Compose.activity)
}
