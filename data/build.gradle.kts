plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "fr.leboncoin.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
    }

    dependencies {
        implementation(project(":domain"))

        implementation(libs.retrofit.core)
        implementation(libs.retrofit.kotlin.serialization)
        implementation(libs.okhttp.logging)

        implementation(libs.kotlin.serialization.json)

        implementation(libs.androidx.room.runtime)
        implementation(libs.androidx.room.ktx)
        ksp(libs.androidx.room.compiler)

        implementation(libs.androidx.datastore.preferences)

        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.coroutines.android)

        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit) // Useless dependency
        androidTestImplementation(libs.androidx.espresso.core) // Useless dependency
    }
}
