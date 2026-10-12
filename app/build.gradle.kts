plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.learnnest"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.learnnest"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testOptions.unitTests.all {
        it.systemProperty("learnnest.assets", file("src/main/assets").absolutePath)
        it.inputs.dir("src/main/assets")
        it.testLogging.showStandardStreams = true
    }
}

apply(from = "content-validation.gradle.kts")

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation("androidx.datastore:datastore-core:1.2.1")
    testImplementation("org.json:json:20240303")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
// Generate an offline subject/unit/topic catalog from Word and PDF source folders.
val courseContentFolder = rootProject.file("course-content")
val generatedCourseAssets = file("src/main/assets/generated_courses")
val courseContentImporter = rootProject.file("tools/import_word_courses.py")

tasks.register<Exec>("importCourseContent") {
    group = "course content"
    description = "Import offline Word/PDF lessons organized by subject and unit"
    inputs.files(fileTree(courseContentFolder) { include("**/*.docx", "**/*.DOCX", "**/*.pdf", "**/*.PDF") })
    inputs.file(courseContentImporter)
    outputs.dir(generatedCourseAssets)
    commandLine("py", "-3", courseContentImporter.absolutePath,
        courseContentFolder.absolutePath, generatedCourseAssets.absolutePath)
}
tasks.named("preBuild") { dependsOn("importCourseContent") }
