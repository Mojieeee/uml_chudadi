import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.example.uml_chudadi"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.uml_chudadi"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.register<Exec>("runDebug") {
    group = "install"
    description = "Builds, installs, and launches the debug app on the selected emulator/device."
    dependsOn("installDebug")

    val localProperties = Properties().apply {
        rootProject.file("local.properties").inputStream().use { load(it) }
    }
    val sdkDir = localProperties.getProperty("sdk.dir")
        ?: error("sdk.dir is missing from local.properties")
    val adb = file("$sdkDir/platform-tools/adb")

    commandLine(
        adb.absolutePath,
        "shell",
        "monkey",
        "-p",
        "com.example.uml_chudadi",
        "-c",
        "android.intent.category.LAUNCHER",
        "1"
    )
}

tasks.register<Exec>("runRelease") {
    group = "install"
    description = "Builds, installs, and launches the signed release app on the selected emulator/device."
    dependsOn("assembleRelease")

    val localProperties = Properties().apply {
        rootProject.file("local.properties").inputStream().use { load(it) }
    }
    val sdkDir = localProperties.getProperty("sdk.dir")
        ?: error("sdk.dir is missing from local.properties")
    val adb = file("$sdkDir/platform-tools/adb").absolutePath
    val apk = rootProject.file("app/build/outputs/apk/release/app-release.apk").absolutePath
    val packageName = "com.example.uml_chudadi"

    commandLine(
        "bash",
        "-lc",
        """
        set -e
        "$adb" install -r "$apk" || {
          echo "Release cannot overwrite an app signed with a different key. Reinstalling fresh..."
          "$adb" uninstall "$packageName" || true
          "$adb" install -r "$apk"
        }
        "$adb" shell monkey -p "$packageName" -c android.intent.category.LAUNCHER 1
        """.trimIndent()
    )
}
