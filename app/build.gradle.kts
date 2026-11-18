plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val backendBaseUrl = providers.gradleProperty("backendBaseUrl")
    .orElse("http://10.0.2.2:3000/")
    .get()
    .let { if (it.endsWith("/")) it else "$it/" }
val apiBaseUrl = providers.gradleProperty("apiBaseUrl")
    .orElse("${backendBaseUrl}api/v1/")
    .get()
    .let { if (it.endsWith("/")) it else "$it/" }
val apiLoginPath = providers.gradleProperty("apiLoginPath").orElse("login").get()
val apiLoginUserField = providers.gradleProperty("apiLoginUserField").orElse("email").get()
val apiLoginPasswordField = providers.gradleProperty("apiLoginPasswordField").orElse("password").get()

fun String.asBuildConfigString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.example.emy"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.emy"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BACKEND_BASE_URL", backendBaseUrl.asBuildConfigString())
        buildConfigField("String", "API_BASE_URL", apiBaseUrl.asBuildConfigString())
        buildConfigField("String", "API_LOGIN_PATH", apiLoginPath.asBuildConfigString())
        buildConfigField("String", "API_LOGIN_USER_FIELD", apiLoginUserField.asBuildConfigString())
        buildConfigField("String", "API_LOGIN_PASSWORD_FIELD", apiLoginPasswordField.asBuildConfigString())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("io.coil-kt:coil-compose:2.7.0")

    //retrofit para el consumo de la API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    // Navigation Compose para el menú
    implementation("androidx.navigation:navigation-compose:2.7.7")
    // ViewModel y Corrutinas
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")


    implementation("androidx.datastore:datastore-preferences:1.1.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")








}