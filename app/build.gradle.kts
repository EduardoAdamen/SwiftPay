plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.swiftpay"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.swiftpay"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // --- AndroidX Core ---
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.swiperefreshlayout)

    // --- Material Design 3 ---
    implementation(libs.material)

    // --- Room (Base de datos local) ---
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.room.paging)

    // --- Lifecycle (ViewModel + LiveData) ---
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.process)

    // --- Navigation Component ---
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // --- Paging 3 ---
    implementation(libs.paging.runtime)

    // --- Security (EncryptedSharedPreferences) ---
    implementation(libs.security.crypto)

    // --- BCrypt (Hash de contraseñas) ---
    implementation(libs.jbcrypt)

    // --- Glide (Carga de imágenes) ---
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // --- ZXing (Escáner de código de barras) ---
    implementation(libs.zxing.embedded)

    // --- iText 7 (Generación de PDF) ---
    implementation(libs.itext.core)

    // --- Fragment y Activity Result API ---
    implementation(libs.fragment)
    implementation(libs.activity)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}