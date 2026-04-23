# SwayRider Navigation App

![Android](https://img.shields.io/badge/Android-8.0+-green?style=flat-square)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue?style=flat-square)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.09-blue?style=flat-square)
![Status](https://img.shields.io/badge/Status-Prototype-orange?style=flat-square)

SwayRider is a prototype navigation application for motorcycles and scooters. It provides route planning, location search, and map-based navigation optimized for urban micro-mobility.

> **Warning**  
> This application is a **prototype** and is not intended for production use.  
> APIs, features, and architecture may change significantly.

---

## Table of Contents

- [What is SwayRider](#what-is-swayrider)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [API Services](#api-services)
- [Configuration](#configuration)
- [Building the App](#building-the-app)
- [Running the App](#running-the-app)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

---

## What is SwayRider

SwayRider is an Android navigation application designed specifically for scooter and e-bike riders. It offers:

- **Route Planning**: Calculate optimal routes based on rider preferences
- **Location Search**: Find addresses, points of interest, and saved locations
- **Map Display**: Interactive maps powered by MapLibre
- **User Authentication**: Secure login and registration with email verification

The app communicates with backend microservices to provide routing, search, and map tile services.

---

## Features

| Feature | Description |
|---------|-------------|
| User Authentication | Login, registration, password reset, email verification |
| Location Search | Search for addresses and POIs with autocomplete |
| Route Planning | Calculate routes between origin and destination |
| Map Display | Interactive vector tiles with custom styling |
| Profile Management | View and manage user account |

---

## Technology Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin 2.2.10 |
| UI Framework | Jetpack Compose (BOM 2024.09) |
| Maps | MapLibre Android SDK 13.0.1 |
| Architecture | MVVM + Clean Architecture |
| Navigation | Jetpack Navigation Compose |
| Networking | Retrofit 3.0.0 + OkHttp 5.3.2 |
| JSON | Moshi 1.15.2 |
| Security | AndroidX Security Crypto |
| Min SDK | Android 8.0 (API 26) |
| Target SDK | Android 14 (API 34) |

### Key Dependencies

```kotlin
// Core
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.lifecycle.runtime.ktx)
implementation(libs.androidx.activity.compose)

// UI
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.compose.material3)

// Maps
implementation(libs.maplibre.android.sdk)
implementation(libs.maplibre.android.plugin.annotation)

// Networking
implementation(libs.retrofit)
implementation(libs.retrofit.moshi)
implementation(libs.okhttp)
implementation(libs.okhttp.logging)

// Security
implementation(libs.androidx.security.crypto)
```

---

## Architecture

The app follows **Clean Architecture** with three distinct layers:

```
+-------------------------------------------------------------+
|                        UI Layer                            |
|  +----------------------------------------------------+   |
|  | Screens (Compose)                                    |   |
|  | - LoginScreen, RegistrationScreen, HomeScreen     |   |
|  | - RoutePlanningScreen, ForgotPasswordScreen       |   |
|  +----------------------------------------------------+   |
|  +----------------------------------------------------+   |
|  | Components                                        |   |
|  | - LocationSearchBar, ProfileMenu, AppScaffold    |   |
|  +----------------------------------------------------+   |
+-------------------------------------------------------------+
                            |
                            v
+-------------------------------------------------------------+
|                    ViewModel Layer                         |
|  +----------------------------------------------------+   |
|  | ViewModels                                        |   |
|  | - AuthViewModel, LocationSearchViewModel        |   |
|  +----------------------------------------------------+   |
|  +----------------------------------------------------+   |
|  | State                                            |   |
|  | - LocationSearchState, AuthEvent                 |   |
|  +----------------------------------------------------+   |
+-------------------------------------------------------------+
                            |
                            v
+-------------------------------------------------------------+
|                     Domain Layer                          |
|  +----------------------------------------------------+   |
|  | Repositories (Interfaces)                        |   |
|  | - LocationSearchRepository                      |   |
|  | - AuthService, AuthStorage                     |   |
|  +----------------------------------------------------+   |
|  +----------------------------------------------------+   |
|  | Models                                           |   |
|  | - LocationSearchResult, UserProfile              |   |
|  | - AuthState, PasswordStrengthState              |   |
|  +----------------------------------------------------+   |
+-------------------------------------------------------------+
                            |
                            v
+-------------------------------------------------------------+
|                      Data Layer                            |
|  +----------------------------------------------------+   |
|  | Repository Implementations                    |   |
|  | - LocationSearchRepositoryImpl                  |   |
|  +----------------------------------------------------+   |
|  +----------------------------------------------------+   |
|  | Network                                          |   |
|  | - Retrofit APIs, OkHttp Client                 |   |
|  | - Auth Interceptor, Token Authenticator        |   |
|  +----------------------------------------------------+   |
|  +----------------------------------------------------+   |
|  | DTOs                                             |   |
|  | - Request/Response data classes                 |   |
|  +----------------------------------------------------+   |
+-------------------------------------------------------------+
```

### Navigation Flow

```
LoginScreen
    │
    ├──► RegistrationScreen
    │         └─> VerificationScreen (email sent)
    │
    ├──► ForgotPasswordScreen
    │         └─> ForgotPasswordConfirmationScreen
    │
    └──► HomeScreen (authenticated)
              │
              ├──► RoutePlanningScreen
              │         └─> Map with route display
              │
              └──► ProfileMenu
                        └─> Logout ──> LoginScreen
```

---

## Project Structure

```
mobile/
├── app/
│   └── src/
│       ├── main/
│       │   └── java/com/hevanto_it/swayrider/
│       │       ├── MainActivity.kt          # Single activity entry point
│       │       ├── SwayRiderApp.kt         # App composable root
│       │       ├── SwayRiderNavHost.kt     # Navigation host
│       │       │
│       │       ├── core/
│       │       │   └── network/
│       │       │       ├── HttpClientProvider.kt
│       │       │       ├── AuthInterceptor.kt
│       │       │       ├── TokenAuthenticator.kt
│       │       │       ├── AuthAnnotations.kt
│       │       │       ├── SafeApiCall.kt
│       │       │       └── NetworkResult.kt
│       │       │
│       │       ├── data/
│       │       │   ├── auth/
│       │       │   │   ├── dto/
│       │       │   │   └── remote/
│       │       │   └── search/
│       │       │       ├── dto/
│       │       │       └── remote/
│       │       │
│       │       ├── domain/
│       │       │   ├── auth/
│       │       │   └── search/
│       │       │
│       │       ├── ui/
│       │       │   ├── components/
│       │       │   ├── navigation/
│       │       │   ├── screens/
│       │       │   └── theme/
│       │       │
│       │       └── viewmodel/
│       │
│       ├── test/
│       │   └── java/com/hevanto_it/swayrider/
│       │
│       └── androidTest/
│           └── java/com/hevanto_it/swayrider/
│
├── build.gradle.kts                      # App module build config
├── gradle/
│   └── libs.versions.toml                # Version catalog
└── settings.gradle.kts                   # Project settings
```

---

## API Services

The app connects to several backend services. Each service is configured via build variants.

| Service | Purpose | Default Port |
|---------|--------|-------------|
| authservice | User authentication, JWT tokens | 34001 (debug) |
| regionservice | Region/municipality data | 34003 (debug) |
| routerservice | Route calculations | 34004 (debug) |
| tileservice | Map vector tiles | 34005 (debug) |
| searchservice | Location search | 34006 (debug) |

### Endpoint Configuration

Services are configured via `BuildConfig` fields:

```kotlin
// In app/build.gradle.kts
buildConfigField("String", "AUTH_SERVICE_HOST", "\"https://authservice.swayrider.com\"")
buildConfigField("String", "SEARCH_SERVICE_HOST", "\"http://192.168.1.222\"")
buildConfigField("Integer", "SEARCH_SERVICE_PORT", "34006")
```

### Build Variants

| Variant | Environment | API Hosts |
|---------|-------------|-----------|
| `debug` | Local development | `192.168.1.222:34xxx` |
| `alpha` | Staging/QA | `*.example.com` |
| `release` | Production | `swayrider.com` |

---

## Configuration

### Build Variants

The app uses Gradle build variants to configure different environments. To switch variants in Android Studio:

1. **Build** → **Select Build Variant**
2. Choose `debug`, `alpha`, or `release`

Or from command line:

```bash
./gradlew assembleDebug    # debug variant
./gradlew assembleAlpha  # alpha variant
./gradlew assembleRelease # release variant
```

### Custom Configuration

To add a custom environment (e.g., for local backend development):

1. Open `app/build.gradle.kts`
2. Add a new build variant block:

```kotlin
create("local") {
    initWith(buildTypes.getByName("debug"))
    applicationIdSuffix = ".local"
    versionNameSuffix = "-local"

    buildConfigField("String", "AUTH_SERVICE_HOST", "\"http://localhost\"")
    buildConfigField("Integer", "AUTH_SERVICE_PORT", "34001")
    buildConfigField("String", "AUTH_SERVICE_PREFIX", "\"/api/v1/auth/\"")

    buildConfigField("String", "SEARCH_SERVICE_HOST", "\"http://localhost\"")
    buildConfigField("Integer", "SEARCH_SERVICE_PORT", "34006")
    buildConfigField("String", "SEARCH_SERVICE_PREFIX", "\"/api/v1/search\"")

    // ... other services
}
```

3. Rebuild with `./gradlew assembleLocal`

### Service URL Pattern

Services follow this URL pattern:

```
{HOST}:{PORT}{PREFIX}{endpoint}
```

Example (debug variant):

```
http://192.168.1.222:34001/api/v1/auth/login
```

---

## Building the App

### Prerequisites

| Requirement | Version |
|-------------|---------|
| Java JDK | 11+ |
| Android SDK | API 34 (compileSdk) |
| Gradle | 9.x (wrapped) |
| Android Studio | Ladybug or later |

### Build Commands

```bash
# Navigate to mobile directory
cd mobile

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease

# Build alpha APK
./gradlew assembleAlpha

# Run tests
./gradlew test

# Run unit tests only
./gradlew testDebugUnitTest
```

### Output Location

APKs are generated at:

```
mobile/app/build/outputs/apk/{variant}/app-{variant}.apk
```

Example:

```
mobile/app/build/outputs/apk/debug/app-debug.apk
```

---

## Running the App

### Using Android Studio

1. Open the `mobile/` directory as an Android project
2. Wait for Gradle sync to complete
3. **Run** → **Run 'app'** or press `Shift + F10`
4. Select target device/emulator

### Using Command Line

```bash
# Install debug APK to connected device/emulator
./gradlew installDebug

# Install to specific device
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Emulator Setup

Recommended emulator configuration:

```
Device: Pixel 8 Pro
API Level: 34
ABI: arm64-v8a
```

### Device Requirements

| Requirement | Minimum |
|-------------|---------|
| Android Version | 8.0 (API 26) |
| Storage | ~50MB |
| RAM | 2GB+ recommended |

---

## Troubleshooting

### Common Issues

#### 1. Connection Errors

**Symptom**: `Connection refused` or `Connection timeout`

**Solution**:
- Verify the backend services are running
- Check the IP address in `build.gradle.kts` matches your network
- Ensure firewall allows connections on service ports

**Debug variant default ports**:
```bash
# Check connectivity
telnet 192.168.1.222 34001  # authservice
telnet 192.168.1.222 34003  # regionservice
telnet 192.168.1.222 34004  # routerservice
telnet 192.168.1.222 34005  # tileservice
telnet 192.168.1.222 34006  # searchservice
```

#### 2. SSL Certificate Errors

**Symptom**: `SSLHandshakeException`

**Solution**:
- For development, ensure `debug` variant uses HTTP (not HTTPS)
- For staging/production, install valid certificates

#### 3. Map Not Loading

**Symptom**: Blank map or tiles not displaying

**Solution**:
- Verify tileservice is running
- Check network connectivity
- Check MapLibre style URL configuration

#### 4. Login/Registration Failing

**Symptom**: Authentication errors

**Solution**:
- Verify authservice is running
- Check JWT configuration
- Review logs for specific error messages

#### 5. Build Errors

**Symptom**: Compilation failures

**Solutions**:
- Clean and rebuild: `./gradlew clean assembleDebug`
- Invalidate caches: **File** → **Invalidate Caches**
- Check Java version: `java -version` (requires 11+)

### Debugging Tips

#### Enable Network Logs

The app includes OkHttp logging interceptor for debug builds. Check `build.gradle.kts`:

```kotlin
implementation(libs.okhttp.logging)
```

#### Check Build Config

In code, access configuration:

```kotlin
import com.hevanto_it.swayrider.BuildConfig

// Example usage
val authHost = BuildConfig.AUTH_SERVICE_HOST
val authPort = BuildConfig.AUTH_SERVICE_PORT
```

#### View Logs

```bash
# Filter app logs
adb logcat | grep swayrider

# Filter network logs
adb logcat | grep -E "(OkHttp|Retrofit)"
```

---

## Contributing

This is a prototype application. Contributions are welcome but should align with the project roadmap.

### Development Workflow

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Make changes following code style
3. Test locally with `./gradlew testDebugUnitTest`
4. Submit a pull request

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable/function names
- Add KDoc for public APIs
- Keep functions focused and small

### Testing

- Write unit tests for business logic
- Test new features on emulator and physical device

---

## License

Copyright 2024. All rights reserved.

This is a prototype. No license is granted for production use.

---

## Related Projects

The SwayRider platform consists of multiple services:

| Service | Description |
|---------|-------------|
| [authservice](../authservice/) | Authentication & authorization |
| [regionservice](../regionservice/) | Region/municipality data |
| [routerservice](../routerservice/) | Route calculations |
| [tilesservice](../tilesservice/) | Map vector tiles |
| [searchservice](../searchservice/) | Location search |
| [swlib](../swlib/) | Shared Go library |
| [grpcclients](../grpcclients/) | Go service clients |
