plugins {
    id("android-library-plugin")
    id(AndroidConfig.Plugin.mavenPublish)
}

publishComposePermissionHandler("compose-permission-handler")

android {
    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion = Versions.composeCompiler
}

dependencies {
    implementation(Libs.Jetbrains.kotlinStdlib)
    implementation(Libs.AndroidX.appCompat)
    implementation(Libs.Google.Accompanist.permissions)
    implementation(Libs.AndroidX.Compose.material)
}
