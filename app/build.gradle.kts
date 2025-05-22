plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.sena.qfinder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sena.qfinder"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "2.9"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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

            applicationIdSuffix = ".debug"
            // Nombre personalizado para el APK debug
            applicationIdSuffix = "Qfinder"

        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }


    kotlinOptions {
        jvmTarget = "11"
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/LICENSE.md",
            "META-INF/LICENSE-notice.md"
        )
    // Corregido: Evitar errores de duplicación
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module"
            )
        }
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

    implementation("com.github.prolificinteractive:material-calendarview:2.0.1") {
        exclude(group = "org.threeten", module = "threetenbp")
    }

    implementation(libs.threetenabp) // Dependencias para consumo de API
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.gson)

    // Para logging de las peticiones HTTP (opcional pero útil para debug)
    implementation(libs.logging.interceptor)
    implementation (libs.material.v170)
}
}
dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
}

