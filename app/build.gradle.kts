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
// Import Word documents from the project-level word-course-inbox folder at build time.
val wordCourseInbox = rootProject.file("word-course-inbox")
val wordCourseGeneratedAssets = file("src/main/assets/word_courses")
val wordImporterScript = rootProject.file("tools/import_word_courses.py")

tasks.register<Exec>("importWordCourses") {
    group = "course content"
    description = "Generate offline LearnNest lessons from .docx files"
    inputs.files(fileTree(wordCourseInbox) { include("*.docx", "*.DOCX") })
    inputs.file(wordImporterScript)
    outputs.dir(wordCourseGeneratedAssets)
    commandLine("py", "-3", wordImporterScript.absolutePath, wordCourseInbox.absolutePath, wordCourseGeneratedAssets.absolutePath)
}

tasks.named("preBuild") { dependsOn("importWordCourses") }
