plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.gratus.ratiocalculator"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gratus.ratiocalculator"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "3.0.a"

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
    }

    buildFeatures{compose = true}

    composeOptions {
        kotlinCompilerExtensionVersion = "2.2.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
    implementation(platform(libs.compose.bom))
    implementation(libs.material3)      // or material2
    implementation(libs.compose.theme.adapter)
}