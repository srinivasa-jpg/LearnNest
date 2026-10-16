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
// Automatically refresh offline subject/unit/topic assets when *any* source file changes.
// Watching the entire folder also tracks HTML dependencies (CSS, JS, images and fonts).
val courseContentFolder = rootProject.file("course-content")
val generatedCourseAssets = file("src/main/assets/generated_courses")
val courseContentImporter = rootProject.file("tools/import_word_courses.py")

val importCourseContent = tasks.register<Exec>("importCourseContent") {
    group = "course content"
    description = "Import offline DOCX, PDF, HTML and Excel lessons organized by subject and unit"
    // Gradle considers the importer out-of-date when a file is added, edited or deleted.
    // Watching only .docx/.pdf here caused HTML/Excel changes to be missed.
    inputs.files(fileTree(courseContentFolder) { include("**/*") })
    inputs.file(courseContentImporter)
    outputs.dir(generatedCourseAssets)
    commandLine("py", "-3", courseContentImporter.absolutePath,
        courseContentFolder.absolutePath, generatedCourseAssets.absolutePath)
}
// Ensure source lessons are regenerated BEFORE Android captures, merges and packages assets.
// Depending on preBuild alone does not guarantee ordering against generateDebugAssets
// or mergeDebugAssets, because Gradle can schedule sibling prerequisites independently.
tasks.named("preBuild") { dependsOn(importCourseContent) }
tasks.matching { task ->
    (task.name.startsWith("generate") || task.name.startsWith("merge")) &&
        task.name.endsWith("Assets")
}.configureEach {
    dependsOn(importCourseContent)
}
