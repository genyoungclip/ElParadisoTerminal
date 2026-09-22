// Top-level build file for El Paradiso Terminal
plugins {
    id("com.android.application") version "9.3.2" apply false
    id("org.jetbrains.kotlin.android") version "2.4.20" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
