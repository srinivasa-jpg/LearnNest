# LearnNest Word Lessons — integration notes

This repository **does not yet contain the complete Android Studio application**. The files on this branch are the Word-import integration prepared from the `MainActivity.kt` and `app/build.gradle.kts` files the project owner shared.

## Functionality

After the rest of the Android project has been committed to this branch, this integration can:
- Read Word `.docx` documents from `word-course-inbox/` at build time.
- Generate offline HTML and embedded images in `app/src/main/assets/word_courses/`.
- Open the Word Lessons library from the LearnNest Home screen.
- Display the text and embedded diagrams on an Android phone, without PDF conversion or a backend.

Each Word file becomes one long reading lesson. Word layout, tables, equations, advanced shapes and floating diagrams may not fully match the source file. Review imported documents before releasing an app.

## One-time setup

1. Include the existing project's missing source files, Gradle scripts and wrappers (but exclude local settings, keys, caches and outputs).
2. Install Python 3 including the `py` launcher on the Windows build computer.
3. Create `word-course-inbox/` at the project root and put your `.docx` files there.
4. To run manually from the project root:
   ```powershell
   py -3 tools\import_word_courses.py word-course-inbox app\src\main\assets\word_courses
   ```
5. Gradle's `:app:preBuild` also runs the importer automatically.
6. Build the Android app and open **Word Lessons** on the Home screen.

Generated HTML, images and `index.json` are overwritten whenever the importer runs. Do not place your hand-edited work in `word_courses/`.

## Current limits and cautions

- This branch cannot be built as a standalone app yet: it lacks other Kotlin classes, the `courses.json` file, the Gradle wrapper and project settings from the owner's machine.
- The reader is a separate library; it has not been integrated into the standard Courses / Chapters / Progress route.
- Do not publish institutional notes or exam keys without permission.
- The Kotlin/Gradle integration has **not been compiled against the full Android project** here.
- No `.docx` documents have been committed to this public repository.

## Troubleshooting

- Compilation error about missing `WORD_COURSES` branch: this branch fixes the exhaustive `when (screen)` toolbar case.
- Missing `index.json`: confirm Python is installed and run the import command explicitly.
- Word Lessons empty: add `.docx` documents to `word-course-inbox` and rebuild.
- Missing image: check if the original Word image was an unsupported Office shape or image format.
