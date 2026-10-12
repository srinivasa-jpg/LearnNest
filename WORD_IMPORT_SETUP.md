# LearnNest: subject → unit → topic content importer

## Goal

Copy Word (.docx) and PDF (.pdf) files into `course-content/Subject/Unit/`; building LearnNest generates a bundled offline catalog. Students can browse **Imported Subjects → Units → Topics**, read text/images or PDF pages, and use Back, Home, Topics, Previous and Next. No backend is required.

## Exact content folder

```text
LearnNest/
  course-content/
    Engineering Graphics/
      Unit 01 - Engineering Curves/
        01 - Epicycloid.docx
      Unit 03 - Previous Papers/
        EG scheme.docx
    Mathematics/
      Unit 01 - Algebra/
        Introduction.pdf
```

The second-level folder name becomes the unit. Each file becomes one topic. A large Word document (like `EG scheme.docx`) is kept together as a topic with its embedded images. Heading 1/2 within Word can be presented as section headers; splitting one large Word file into *separate app topics* is **not implemented** yet.

## Import manually (Windows)

Run from the LearnNest project root:

```powershell
py -3 tools\import_word_courses.py course-content app\src\main\assets\generated_courses
```

This requires Python 3 with the Windows `py` launcher but no third-party Python packages.

The Android `app/build.gradle.kts` defines an `importCourseContent` task and invokes it before `preBuild`, so importing runs again when you build or run the app. This process creates `generated_courses/index.json`, HTML/images for Word, and copies of PDFs. **Do not edit files under generated_courses; the importer rewrites that folder.**

## On the phone

1. Open LearnNest.
2. On Home, select **Imported Subjects / Browse imported subjects**.
3. Open Engineering Graphics → Unit 03 - Previous Papers → EG scheme.
4. Use Previous and Next to move among topics in the selected unit; Topics to return to the topic list; Home to return to LearnNest.
5. PDF documents have an additional page navigator. Pinch to zoom PDFs and Word lessons.

## Development status (important)

This is a **draft integration** for previously shared files; GitHub `main` does not yet contain the complete Android Studio project. The draft branch therefore cannot be built or verified against all of the user's original Android sources. Do not overwrite newer local files blindly.

This is a *separate imported-subjects library*, not yet merged into `CourseRepository`'s built-in Subjects/Courses/Progress system. The normal existing student progress and quizzes have not been modified. Merging imported topics into that same progress/navigation flow will require the project's current `CourseRepository.kt`, course data models, and any content validation script.

Public GitHub repo caution: do not commit copyrighted or private course documents without permission. Both DOCX/PDF source files in `course-content` and generated assets are ignored by this branch's `.gitignore`.

## Known Word/PDF limitations

- Floating diagrams and advanced Office shapes may not display exactly like Word; verify your engineering graphics document on the device.
- PDF remains a complete lesson; page-per-topic splitting is not implemented.
- Naming is folder-based, and alphabetical prefixes like `Unit 01`, `01 -` determine intended ordering.
- Renaming or moving a topic changes its generated stable identifier. Future progress integration should account for that.
