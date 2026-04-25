plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

group = findProperty("group")?.toString() ?: "com.github.thebytearray"
version = findProperty("version")?.toString() ?: "0-SNAPSHOT"

android {
    namespace = "org.thebytearray.libxray.sdk"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 23
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        externalNativeBuild {
            cmake {
                cppFlags("")
                arguments(
                    "-DANDROID_PACKAGE_NAME=org.thebytearray.libxray.android",
                )
                targets("libxray-go.so")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = group.toString()
                artifactId = "libxray-android"
                version = project.version.toString()
                from(components["release"])
                pom {
                    name.set("libxray-android")
                    description.set("Android embedding of Xray (libxray / native core).")
                    url.set("https://github.com/thebytearray/libxray-android")
                    licenses {
                        license {
                            name.set("GNU General Public License v3.0")
                            url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/thebytearray/libxray-android.git")
                        developerConnection.set("scm:git:ssh://git@github.com/thebytearray/libxray-android.git")
                        url.set("https://github.com/thebytearray/libxray-android")
                    }
                }
            }
        }
    }
}
