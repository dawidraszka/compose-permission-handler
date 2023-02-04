import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.kotlin() {
    implementation(Libs.Jetbrains.kotlinStdlib)
}

fun DependencyHandler.baseAndroid() {
    implementation(Libs.AndroidX.coreKtx)
    implementation(Libs.AndroidX.appCompat)
}

fun DependencyHandler.compose() {
    implementation(Libs.AndroidX.Compose.material)
    implementation(Libs.AndroidX.Compose.activity)
}

fun DependencyHandler.accompanist() {
    implementation(Libs.Google.Accompanist.permissions)
}
