object Libs {

    object Plugins {
        const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    }

    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:${Versions.ktx}"
        const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"

        object Compose {
            const val material3 = "androidx.compose.material3:material3:${Versions.material3}"
            const val activity = "androidx.activity:activity-compose:${Versions.composeActivity}"
            const val ui = "androidx.compose.ui:ui:${Versions.compose}"
        }
    }

    object Jetbrains {
        const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    }
}
