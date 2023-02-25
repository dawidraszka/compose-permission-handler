object Libs {

    object Plugins {
        const val buildGradle = "com.android.tools.build:gradle:${Versions.gradle}"
        const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
        const val ktlintGradle = "org.jlleitschuh.gradle:ktlint-gradle:${Versions.ktlint}"
    }

    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:${Versions.ktx}"
        const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"

        object Compose {
            const val material3 = "androidx.compose.material3:material3:${Versions.material3}"
            const val activity = "androidx.activity:activity-compose:${Versions.composeActivity}"
        }
    }

    object Google {
        object Accompanist {
            const val permissions = "com.google.accompanist:accompanist-permissions:${Versions.accompanistPermissions}"
        }
    }

    object Jetbrains {
        const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    }
}
