import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// La URL de la API se lee de local.properties (un archivo que NUNCA se sube
// al repositorio: cada integrante del equipo tiene el suyo). Así, cada quien
// puede apuntar a su propio backend (su computador, o uno en la nube) sin
// tocar ni un archivo de código ni generar conflictos de Git con el resto
// del equipo. Si no se define nada, se usa 10.0.2.2, la dirección con la que
// el EMULADOR ve el "localhost" del mismo computador (ver README del backend).
val localProperties = Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) {
        FileInputStream(archivo).use { load(it) }
    }
}
val apiBaseUrl: String = (localProperties.getProperty("API_BASE_URL") ?: "http://10.0.2.2:3000/api/")
    .let { if (it.endsWith("/")) it else "$it/" }

android {
    namespace = "co.edu.ue.uebank"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "co.edu.ue.uebank"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.recyclerview)
    implementation(libs.swiperefreshlayout)
    implementation(libs.core.splashscreen)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
