# libxray-android

Android library embedding the Xray core (via [libxray](https://github.com/xtls/libxray) and native `libxray-go`).  

## License

This project is licensed under the **GNU General Public License v3.0**. See the [`LICENSE`](LICENSE) file.

## Add with JitPack

1. Add the JitPack repository:

**Kotlin DSL (`settings.gradle.kts`)**

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}
```

**Groovy (`settings.gradle`)**

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add the dependency. JitPack publishes the `libxray` module as artifact **`libxray-android`** (no extra module segment in the coordinate):

**Kotlin DSL**

```kotlin
dependencies {
    implementation("com.github.thebytearray:libxray-android:VERSION")
}
```

**Groovy**

```groovy
dependencies {
    implementation 'com.github.thebytearray:libxray-android:VERSION'
}
```

Replace `VERSION` with a [Git tag](https://github.com/thebytearray/libxray-android/tags), branch name, or commit hash (for example `1.0.1` or `abc1234`).

## Usage

Public API entry points:

- `org.thebytearray.libxray.sdk.LibXray`
- `org.thebytearray.libxray.sdk.ProtectHandler`

`minSdk` is **23**. The AAR ships native libraries for `armeabi-v7a`, `arm64-v8a`, and `x86_64`.

## Build locally

```bash
./gradlew :libxray:assembleRelease
```

Publish to Maven Local (same as JitPack’s Gradle step):

```bash
./gradlew :libxray:publishReleasePublicationToMavenLocal
```

