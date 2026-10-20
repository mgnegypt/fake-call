# Dependency versions

All versions below are the **latest stable releases** (no alpha, beta, RC, or
snapshot), looked up from the official sources on **2026-10-20**:

- Google Maven (`dl.google.com/dl/android/maven2`) for AGP and AndroidX
- Maven Central for Kotlin, KSP, kotlinx libraries, and JUnit
- `services.gradle.org/versions/all` for Gradle

| Component | Version | Source |
|---|---|---|
| Gradle | 9.8.2 | gradle.org releases (latest final) |
| Android Gradle Plugin | 9.4.1 | Google Maven (9.5.x is alpha) |
| Kotlin | 2.4.21 | Maven Central (2.5.x is beta) |
| KSP | 2.3.12 | Maven Central — latest stable KSP line; no 2.4-aligned KSP release exists yet, KSP 2.3.x is the current stable processor line |
| Compose BOM | 2026.09.00 | Google Maven |
| androidx.core:core-ktx | 1.19.1 | Google Maven |
| androidx.core:core-splashscreen | 1.2.0 | Google Maven |
| androidx.appcompat:appcompat | 1.8.0 | Google Maven |
| androidx.activity:activity-compose | 1.13.0 | Google Maven (1.14.x is alpha) |
| androidx.lifecycle | 2.11.0 | Google Maven (2.12.x is alpha) |
| androidx.navigation:navigation-compose | 2.10.2 | Google Maven |
| androidx.room | 2.8.5 | Google Maven |
| androidx.datastore:datastore-preferences | 1.2.1 | Google Maven (1.3.x is RC) |
| androidx.work:work-runtime-ktx | 2.12.0 | Google Maven |
| kotlinx-serialization-json | 1.12.0 | Maven Central |
| kotlinx-coroutines-android | 1.11.0 | Maven Central |
| JUnit 4 | 4.13.2 | Maven Central |

Platform levels: `compileSdk`/`targetSdk` 37 (Android 17, minor API level 0 —
the SDK platform package is `platforms;android-37.0`), `minSdk` 30 (Android 11),
build-tools 37.0.0, JDK 17.
