import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

// ---------------------------------------------------------------------------
// SIGNING
//
// Mirrors the house precedent in Mihrabtv/wrappers/android/app/build.gradle.kts:
// the properties file is resolved from $MIHRAB_KEY_PROPERTIES first, so the
// upload key's password lives in exactly one file on this machine and is never
// copied into this repo. `key.properties`, `*.jks` and `*.keystore` are
// gitignored regardless.
//
// The watch has its own applicationId (faith.mihrab.watch), so it is a separate
// app in Play and sharing the upload key with the phone build is not a conflict.
// ---------------------------------------------------------------------------
val keystorePropsFile: File = System.getenv("MIHRAB_KEY_PROPERTIES")
    ?.takeIf { it.isNotBlank() }
    ?.let { File(it) }
    ?: rootProject.file("key.properties")

val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}

android {
    namespace = "faith.mihrab.watch"
    compileSdk = 35

    defaultConfig {
        applicationId = "faith.mihrab.watch"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${localProps.getProperty("SUPABASE_URL", "")}\"",
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"${localProps.getProperty("SUPABASE_ANON_KEY", "")}\"",
        )
    }

    signingConfigs {
        create("release") {
            if (keystorePropsFile.exists()) {
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
                keystoreProps.getProperty("storeFile")?.let { storeFile = File(it) }
                storePassword = keystoreProps.getProperty("storePassword")
            }
            // minSdk is 33, so v1 (JAR) signing buys nothing on the APK. The .aab is
            // JAR-signed by AGP regardless, which is what lets `keytool -printcert
            // -jarfile` read the certificate back out of the bundle without a password.
            enableV1Signing = false
            enableV2Signing = true
        }
    }

    buildTypes {
        release {
            // R8 is OFF deliberately. supabase-kt 3.0.3 ships ZERO consumer ProGuard
            // rules while routing every decode through the runtime reflective
            // serializer(KType) lookup, and no test in this project exercises
            // serialization — so a broken shrink would surface only on a real watch.
            // See proguard-rules.pro for the full reasoning and the rules that would
            // be required before this can be flipped.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    bundle {
        // The watch sets its UI locale at RUNTIME from the phone's display_language
        // (LocaleManager.apply -> AppCompatDelegate.setApplicationLocales). Play's
        // per-language split delivery installs only the resources matching the DEVICE
        // locale, so an English watch paired to an Arabic account would have no Arabic
        // strings on disk and would silently render English. Bundling every language
        // into the base costs ~1 MB and is what keeps the twelve locales working.
        language { enableSplit = false }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose (BOM for core Compose libraries)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // Wear OS Compose
    implementation(libs.wear.compose.material3)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.navigation)

    // Tiles + ProtoLayout
    implementation(libs.wear.tiles)
    implementation(libs.wear.protolayout)
    implementation(libs.wear.protolayout.material)
    implementation(libs.wear.protolayout.expression)
    debugImplementation(libs.wear.tiles.renderer)

    // Complications
    implementation(libs.wear.complications.data.source.ktx)

    // Supabase + Ktor (pairing flow: Postgrest INSERT + Realtime subscription)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.realtime)
    implementation(libs.ktor.client.okhttp)

    // Prayer time computation (Option B — local adhan calculation)
    implementation(libs.adhan2)

    // Local credential storage (pairingId + pairedUserId after successful pair)
    implementation(libs.androidx.datastore.preferences)

    // AppCompat — AppCompatDelegate.setApplicationLocales for i18n (display_language from sync_payload)
    implementation(libs.androidx.appcompat)

    // ListenableFuture helper for Tile service (all variants — Guava was debug-only via tiles-renderer)
    implementation(libs.androidx.concurrent.futures)

    // Serialization + coroutines (Supabase-kt requirements — pinned explicitly)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}

// ---------------------------------------------------------------------------
// Fail a release PACKAGING task loudly and early when the signing material is
// missing or unreadable, rather than letting AGP emit its opaque "keystore not
// set" error — or, worse, letting an unsigned artefact reach Play, which looks
// identical until the upload is refused.
//
// Deliberately scoped to assemble/bundle/package so that `gradlew test`, `lint`
// and `assembleDebug` still run on a machine without the key.
// ---------------------------------------------------------------------------
gradle.taskGraph.whenReady {
    val packagesRelease = allTasks.any {
        it.name.matches(Regex("^(assemble|bundle|package)Release.*"))
    }
    if (!packagesRelease) return@whenReady

    if (!keystorePropsFile.exists()) {
        throw GradleException(
            "Signing material not found at ${keystorePropsFile.absolutePath}.\n" +
                "Release builds must be signed with the Mihrab upload key. Either:\n" +
                "  * set MIHRAB_KEY_PROPERTIES to an existing key.properties, or\n" +
                "  * create key.properties at the repo root with storeFile / storePassword /\n" +
                "    keyAlias / keyPassword (storeFile must use forward slashes).\n" +
                "That file is gitignored and must never be committed.",
        )
    }

    val missing = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
        .filter { keystoreProps.getProperty(it).isNullOrBlank() }
    if (missing.isNotEmpty()) {
        throw GradleException(
            "${keystorePropsFile.absolutePath} is missing required keys: ${missing.joinToString()}.\n" +
                "A release build must never fall back to debug or unsigned output.",
        )
    }

    val store = File(keystoreProps.getProperty("storeFile"))
    if (!store.canRead()) {
        throw GradleException(
            "Keystore at ${store.absolutePath} is missing or unreadable.\n" +
                "A release build must never fall back to debug or unsigned output.",
        )
    }
}
