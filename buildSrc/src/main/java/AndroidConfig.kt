import org.gradle.api.JavaVersion

object AndroidConfig {
    object Plugin {
        const val androidApp = "com.android.application"
        const val androidLib = "com.android.library"
        const val kotlinAndroid = "kotlin-android"
        const val kapt = "kotlin-kapt"
        const val kotlin = "kotlin"
        const val ktlint = "org.jlleitschuh.gradle.ktlint"
        const val mavenPublish = "maven-publish"
        const val detekt = "io.gitlab.arturbosch.detekt"
    }

    const val sdkVersion = 34
    const val minSdkVersion = 21
    val javaVersion = JavaVersion.VERSION_17
}
