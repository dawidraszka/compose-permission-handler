plugins {
    id("android-library-plugin")
    id(AndroidConfig.Plugin.mavenPublish)
}

publishComposePermissionHandler("utils")

android {
    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion = Versions.composeCompiler
    namespace = "com.dawidraszka.composepermissionhandler.utils"
}

dependencies {
    implementation(project(":core"))

    implementation(Libs.AndroidX.Compose.material3)
}
