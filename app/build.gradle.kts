import org.gradle.api.tasks.bundling.Zip

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.gradleexperiment"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.gradleexperiment"
        // minSdk de base (le plus bas supporté)
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // --- Dimensions & Flavors ---

    // 1ère dimension : type d’app (free / paid)
    flavorDimensions += "paidMode"

    // 2ème dimension : niveau de compatibilité SDK (21 / 30)
    flavorDimensions += "minSdk"

    productFlavors {
        // ----- Dimension paidMode -----
        create("free") {
            dimension = "paidMode"
            applicationIdSuffix = ".free"
        }
        create("paid") {
            dimension = "paidMode"
            applicationIdSuffix = ".paid"
        }

        // ----- Dimension minSdk -----
        create("minSdk21") {
            dimension = "minSdk"
            minSdk = 21
        }
        create("minSdk30") {
            dimension = "minSdk"
            minSdk = 30
        }
    }

    // --- Build Types : debug (par défaut), release, beta ---
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        create("beta") {
            isMinifyEnabled = false
            applicationIdSuffix = ".beta"
            versionNameSuffix = "-beta"
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

// 1) Copie tous les APK + génère un checksum pour chacun
tasks.register<Copy>("copyAndVerifyApks") {
    group = "automation"
    description = "Copie tous les APK générés dans /apk et génère un checksum pour chacun."

    // 🔹 Important : on déclare qu'on dépend des tasks qui créent les APK
    dependsOn(
        "packageFreeMinSdk21Debug",
        "packageFreeMinSdk21Release",
        "packageFreeMinSdk30Debug",
        "packageFreeMinSdk30Release",
        "packagePaidMinSdk21Debug",
        "packagePaidMinSdk21Release",
        "packagePaidMinSdk30Debug",
        "packagePaidMinSdk30Release"
    )

    val sourceDir = layout.buildDirectory.dir("outputs/apk")
    val destDir = layout.projectDirectory.dir("apk")

    from(sourceDir)
    include("**/*.apk")          // tous les APK de toutes les variantes
    into(destDir)

    doLast {
        // Pour chaque APK copié, calculer un checksum
        destDir.asFileTree.matching { include("**/*.apk") }.files.forEach { apkFile ->
            ant.invokeMethod("checksum", mapOf("file" to apkFile.path))
            println("APK copié : ${apkFile.name}")
            println("Checksum généré pour : ${apkFile.name}")
        }
        println("Tous les APK ont été copiés dans : ${destDir.asFile.absolutePath}")
    }
}

// 2) Crée un ZIP avec tous les APK + fichiers .MD5
tasks.register<Zip>("zipApkFolder") {
    group = "automation"
    description = "Crée une archive ZIP contenant tous les APK et leurs checksums."

    // On veut zipper le résultat de copyAndVerifyApks
    dependsOn("copyAndVerifyApks")

    val apkDir = layout.projectDirectory.dir("apk")

    from(apkDir)                                // contenu du dossier apk/
    archiveFileName.set("apk_bundle.zip")       // nom du zip
    destinationDirectory.set(layout.projectDirectory.dir("dist")) // dossier dist/

    doLast {
        println("Archive créée : ${archiveFile.get().asFile.absolutePath}")
    }
}

// 3) Pipeline complète : tests + build + copie + checksum + zip
tasks.register("fullPipeline") {
    group = "automation"
    description = "Lance les tests, assemble les APK, copie + vérifie + zippe les APK."

    // 1) Tests
    // 2) Assemblage des APK (debug + release)
    // 3) zipApkFolder -> dépend déjà de copyAndVerifyApks
    dependsOn("test", "assembleDebug", "assembleRelease", "zipApkFolder")
}




