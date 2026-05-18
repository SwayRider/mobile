import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.hevanto_it.swayrider"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hevanto_it.swayrider"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String", "AUTH_SERVICE_HOST", "\"https://authservice.swayrider.com\"")
            buildConfigField("Integer", "AUTH_SERVICE_PORT", "443")
            buildConfigField("String", "AUTH_SERVICE_PREFIX", "\"/api/v1/auth/\"")

            buildConfigField("String", "AUTH_SERVICE_WEB_HOST", "\"https://authservice.swayrider.com\"")
            buildConfigField("Integer", "AUTH_SERVICE_WEB_PORT", "443")
            buildConfigField("String", "AUTH_SERVICE_WEB_PREFIX", "\"/web/\"")

            buildConfigField("String", "REGION_SERVICE_HOST", "\"https://regionservice.swayrider.com\"")
            buildConfigField("Integer", "REGION_SERVICE_PORT", "443")
            buildConfigField("String", "REGION_SERVICE_PREFIX", "\"/api/v1/region/\"")

            buildConfigField("String", "ROUTER_SERVICE_HOST", "\"https://routerservice.swayrider.com\"")
            buildConfigField("Integer", "ROUTER_SERVICE_PORT", "443")
            buildConfigField("String", "ROUTER_SERVICE_PREFIX", "\"/api/v1/router/\"")

            buildConfigField("String", "TILES_SERVICE_HOST", "\"http://192.168.1.222\"")
            buildConfigField("Integer", "TILES_SERVICE_PORT", "34000")
            buildConfigField("String", "TILES_SERVICE_PREFIX", "\"\"")

            buildConfigField("String", "SEARCH_SERVICE_HOST", "\"http://192.168.1.222\"")
            buildConfigField("Integer", "SEARCH_SERVICE_PORT", "34006")
            buildConfigField("String", "SEARCH_SERVICE_PREFIX", "\"/api/v1/search\"")

            buildConfigField("Long", "HTTP_CONNECT_TIMEOUT", "10L")
            buildConfigField("Long", "HTTP_READ_TIMEOUT", "10L")
            buildConfigField("Long", "HTTP_WRITE_TIMEOUT", "10L")
        }
        getByName("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            buildConfigField("String", "AUTH_SERVICE_HOST", "\"http://192.168.1.222\"")
            buildConfigField("Integer", "AUTH_SERVICE_PORT", "34001")
            buildConfigField("String", "AUTH_SERVICE_PREFIX", "\"/api/v1/auth/\"")

            buildConfigField("String", "AUTH_SERVICE_WEB_HOST", "\"http://192.168.1.222\"")
            buildConfigField("Integer", "AUTH_SERVICE_WEB_PORT", "34201")
            buildConfigField("String", "AUTH_SERVICE_WEB_PREFIX", "\"/web/\"")

            buildConfigField("String", "REGION_SERVICE_HOST", "\"http://192.168.1.222\"")
            buildConfigField("Integer", "REGION_SERVICE_PORT", "34003")
            buildConfigField("String", "REGION_SERVICE_PREFIX", "\"/api/v1/region/\"")

            buildConfigField("String", "ROUTER_SERVICE_HOST", "\"http://192.168.1.222\"")
            buildConfigField("Integer", "ROUTER_SERVICE_PORT", "34004")
            buildConfigField("String", "ROUTER_SERVICE_PREFIX", "\"/api/v1/router/\"")

            buildConfigField("String", "TILES_SERVICE_HOST", "\"http://192.168.1.222\"")
            buildConfigField("Integer", "TILES_SERVICE_PORT", "34000")
            buildConfigField("String", "TILES_SERVICE_PREFIX", "\"\"")

            buildConfigField("String", "SEARCH_SERVICE_HOST", "\"http://192.168.1.222\"")
            buildConfigField("Integer", "SEARCH_SERVICE_PORT", "34006")
            buildConfigField("String", "SEARCH_SERVICE_PREFIX", "\"/api/v1/search\"")

            buildConfigField("Long", "HTTP_CONNECT_TIMEOUT", "10L")
            buildConfigField("Long", "HTTP_READ_TIMEOUT", "60L")
            buildConfigField("Long", "HTTP_WRITE_TIMEOUT", "60L")
        }
        create("alpha") {
            initWith(buildTypes.getByName("debug"))
            applicationIdSuffix = ".alpha"
            versionNameSuffix = "-alpha"

            buildConfigField("String", "AUTH_SERVICE_HOST", "\"https://authservice.swayrider-dev.example.com\"")
            buildConfigField("Integer", "AUTH_SERVICE_PORT", "443")
            buildConfigField("String", "AUTH_SERVICE_PREFIX", "\"/api/v1/auth/\"")

            buildConfigField("String", "AUTH_SERVICE__WEB_HOST", "\"https://authservice.swayrider-dev.example.com\"")
            buildConfigField("Integer", "AUTH_SERVICE_WEB_PORT", "443")
            buildConfigField("String", "AUTH_SERVICE_WEB_PREFIX", "\"/web/\"")

            buildConfigField("String", "REGION_SERVICE_HOST", "\"https://regionservice.swayrider-dev.example.com\"")
            buildConfigField("Integer", "REGION_SERVICE_PORT", "443")
            buildConfigField("String", "REGION_SERVICE_PREFIX", "\"/api/v1/region/\"")

            buildConfigField("String", "ROUTER_SERVICE_HOST", "\"https://routerservice.swayrider-dev.example.com\"")
            buildConfigField("Integer", "ROUTER_SERVICE_PORT", "443")
            buildConfigField("String", "ROUTER_SERVICE_PREFIX", "\"/api/v1/router/\"")

            buildConfigField("String", "TILES_SERVICE_HOST", "\"http://192.168.1.222\"")
            buildConfigField("Integer", "TILES_SERVICE_PORT", "34005")
            buildConfigField("String", "TILES_SERVICE_PREFIX", "\"\"")

            buildConfigField("String", "SEARCH_SERVICE_HOST", "\"http://192.168.1.222\"")
            buildConfigField("Integer", "SEARCH_SERVICE_PORT", "34006")
            buildConfigField("String", "SEARCH_SERVICE_PREFIX", "\"/api/v1/search\"")

            buildConfigField("Long", "HTTP_CONNECT_TIMEOUT", "10L")
            buildConfigField("Long", "HTTP_READ_TIMEOUT", "60L")
            buildConfigField("Long", "HTTP_WRITE_TIMEOUT", "60L")
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

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
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
    implementation(libs.androidx.navigation.compose)
    implementation(libs.maplibre.android.sdk)
    implementation(libs.maplibre.android.plugin.annotation)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.security.crypto)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.androidx.compose.runtime)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
