plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.sena.qfinder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sena.qfinder"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.5" // Puedes personalizar la versión

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Nombre personalizado para el APK debug
            applicationIdSuffix = ".debug"
        }
    }



    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}