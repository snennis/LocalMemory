plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.map_test"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.map_test"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.osmdroid.android)
    implementation(libs.osmdroid.wms)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation ("androidx.work:work-runtime-ktx:2.9.0")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("org.osmdroid:osmdroid-android:6.1.11")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
}