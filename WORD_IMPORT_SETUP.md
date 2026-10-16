# LearnNest: folder-based Word/PDF lessons

## Your source folder (on your PC)

```text
LearnNest/
  course-content/
    Engineering Graphics/
      Unit 01 - Engineering Curves/
        01 - Epicycloid.docx
        02 - Hyperbola.pdf
      Unit 03 - Previous Papers/
        EG scheme.docx
    Mathematics/
      Unit 01 - Algebra/
        01 - Algebra Introduction.docx
```

**Subject = first folder; Unit = second folder; Topic = Word/PDF filename.** Sort order follows folder and filename, so use numeric prefixes where important. A single Word document may contain many sections or exam questions; for now, the whole document appears as **one topic**, with all text and supported diagrams in order. This importer does **not** automatically turn heading sections into separate topic cards.

## To use in your existing Android Studio project

The current GitHub repository only includes this integration draft, not the complete local LearnNest Android app.

1. Back up your existing LearnNest project.
2. Download the `word-import-integration-review` branch and copy these files **into the same paths** in your existing project:
   - `tools/import_word_courses.py`
   - `app/src/main/java/com/example/learnnest/ImportedWordCoursesScreen.kt`
   - `app/src/main/java/com/example/learnnest/MainActivity.kt` — replace **only if it matches the original version you provided and has no newer local edits**; otherwise merge.
   - `app/build.gradle.kts` — same caution: merge instead of overwriting newer changes.
3. Ensure Python 3 with the `py` launcher is installed: `py -3 --version`.
4. Put Word/PDF files under `course-content/<Subject>/<Unit>/`.
5. Open Android Studio and **Sync Project with Gradle Files**, then **Build > Rebuild Project**.
6. Alternatively generate content from Command Prompt inside project root:
   ```powershell
   py -3 tools\import_word_courses.py course-content app\src\main\assets\generated_courses
   ```
7. On your phone open LearnNest → **Imported Subjects** → Subject → Unit → Topic.
8. In the reader, use **Back**, **Home**, **Topics**, **Previous**, and **Next**. PDFs also have page controls.

The build task is named `:app:importCourseContent` and runs before `:app:preBuild`. Files are generated into `app/src/main/assets/generated_courses/` and bundled offline.

## Known limitations

- **Not yet build-verified against your complete Android Studio project**. Remaining project files live on your computer, not in this GitHub repository.
- Uses a separate **Imported Subjects** entry on the Home screen. Integration into the app's original Subjects/Courses/Progress structures is future work.
- Each file is one topic, even when one Word document contains multiple chapters/questions. Word headings can be displayed as headings, not split into multiple topic entries yet.
- Supports embedded PNG/JPEG/GIF/WebP/BMP Word images. Complex Office drawings, embedded vectors, equations, text boxes, and layout may not convert faithfully.
- PDF is displayed through Android PdfRenderer using page images; check complex pages and zoom on a real device.
- Do not commit copyrighted or private teaching materials to the public repository without permission.
- File path is used to derive topic IDs. Renaming/moving source files changes IDs, which affects future progress integration.
- Python 3 is required only on the development PC; students do not need Python.
- New lessons reach existing students only after a new Google Play app update.

## Important content safety

`course-content/` is gitignored by default so source documents do not get published to your public GitHub repository accidentally. This also means GitHub alone cannot reproduce a content-filled build unless you separately provide licensed source content.

The previous `word-course-inbox/` is legacy; it is **not used** by the new `:app:importCourseContent` task.
