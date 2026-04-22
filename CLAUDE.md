# CLAUDE.md — mobile/android

This directory contains the SwayRider Android mobile application.
Refer to the root `CLAUDE.md` for global repository rules.

## Scope

Work is **strictly** limited to `mobile/android/` unless explicitly instructed otherwise.
Do NOT inspect or modify backend services, data-pipeline, or infra code.

---

## Build Configuration Rules

### Build Variants

This app has **three build variants**:
- `debug` — local development (localhost endpoints)
- `alpha` — dev environment testing (dev server endpoints)
- `release` — production builds (prod server endpoints)

Build variant configuration is in `app/build.gradle.kts`.

### BuildConfig Invariants

- API endpoints are defined **per variant** via `buildConfigField`
- Never hardcode endpoints in Kotlin code
- Never change build variants, SDK versions, or dependencies without explicit permission

### Gradle Rules

- Do NOT modify `build.gradle.kts` files without permission
- Do NOT add dependencies without permission
- Do NOT change minSdk, targetSdk, or compileSdk without permission

---

## Architecture & Dependency Injection

### Layer Boundaries

This app uses **Clean Architecture** with strict layer separation:

- `ui/` — Jetpack Compose UI components
- `viewmodel/` — ViewModels (StateFlow state management)
- `domain/` — Use cases and business logic
- `data/` — Repositories and data sources
- `core/` — Shared utilities (network, auth, storage)

**Dependency flow**: ui → viewmodel → domain → data → core

### Manual Dependency Injection

- All DI is **manual** via `SwayRiderApp.kt`
- No Hilt, Koin, or other DI frameworks
- Follow existing DI pattern in `SwayRiderApp.kt`

### Clean Architecture Rules

- **Never** call data layer directly from UI
- **Never** skip viewmodel layer
- **Never** put business logic in viewmodels
- Domain layer is **framework-agnostic** (no Android imports)

---

## UI & Jetpack Compose

### Compose Rules

- All UI is **Jetpack Compose** (no XML layouts)
- Use **Material3** components only
- Follow existing component patterns in `ui/components/`

### State Management

- Use **StateFlow** in viewmodels
- Collect state via `collectAsState()` in composables
- No mutable state in composables (hoist to viewmodel)

### Existing Patterns

- `AppScaffold.kt` — main app scaffold with navigation
- `Screen.kt` — sealed class for navigation routes
- Follow established navigation and theming patterns

---

## Security & API Rules

### Token Security

- **Only** use `EncryptedSharedPreferences` for token storage
- Token access **only** via `AuthStorage.kt`
- **Never** log tokens or store them unencrypted
- Clear tokens on logout

### API Client Rules

- Use **Retrofit** for all network calls (configured in `core/network/`)
- `AuthInterceptor.kt` adds auth headers automatically
- `TokenAuthenticator.kt` handles 401 token refresh
- **Never** manually add Authorization headers in API calls

### Credential Storage

- Use `EncryptedSharedPreferences` for all sensitive data
- Clear all credentials on logout
- No plain SharedPreferences for auth data

### Network Security

- `network_security_config.xml` controls cleartext traffic
- Debug variant **only** allows localhost cleartext
- Alpha/release use HTTPS only

---

## Permissions & Android Components

### AndroidManifest Rules

- Required permissions: `INTERNET`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`
- **Never** add permissions without explicit user request
- Document why each permission is needed

### Runtime Permissions

- Use **Accompanist Permissions** library for runtime permission handling
- Request location permissions at appropriate UI points
- Handle permission denial gracefully

### Activity Architecture

- **Single activity** architecture (MainActivity.kt)
- All screens are Composables, not Activities/Fragments

---

## Testing

### Test Structure

- `src/test/` — unit tests (JVM)
- `src/androidTest/` — instrumented tests (Android device/emulator)

### Testing Rules

- Create unit tests for viewmodels and domain layer
- Use test doubles (fakes/mocks) for repositories
- **Never** make real network calls in tests

### Test Execution

```bash
./gradlew test                 # Run unit tests
./gradlew connectedAndroidTest # Run instrumented tests
```

---

## Execution Rules

1. Follow the approved plan exactly
2. Apply changes in a single pass
3. No re-analysis or commentary during execution
4. No out-of-scope refactors

### Gradle-Specific

- After code changes, iterate until tests pass
- Clean build artifacts after verification: `./gradlew clean`

---

## Documentation

### Do NOT Read by Default

Ask explicit permission before reading:
- `README.md`
- Architecture documentation
- Build scripts outside app/

### Tooling

- Use **context7** only when adding new external libraries
- Prefer Android official documentation for Jetpack libraries
- Prefer Kotlin official documentation for language features
