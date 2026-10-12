# LearnNest — approved design preview implementation

Based on the approved LearnNest 8-screen mockup, this development branch now contains the refreshed Compose Home, Course Content browser, lesson controls and quiz subject selector.

## What changed

**Home:** centered LearnNest branding and subtitle, blue **Course Content** card, green **Quizzes** card. No other Home cards, tabs or subscription features. The Home scaffold toolbar is hidden to avoid duplicating the app title.

**Course Content:** the existing `course-content/Subject/Unit/` importer remains unchanged. Subject cards use blue/green/red/purple pastels; unit cards use lavender; topic cards use white. Top bar has **Back** and **Home** with a short, centered title. The unit screen no longer duplicates a long heading in the toolbar. Topic cards show just lesson names.

**Readers:** one lesson title, original imported Word text/diagrams or PDF, and **Previous · Topics · Next** buttons. PDF also retains page navigation. When there is only one topic, Previous/Next are disabled.

**Quizzes:** Home opens a pastel quiz-subject selector based on actual existing quiz/course catalog data; select a subject to reach the original `QuizListScreen`, attempt a quiz, and review saved results. Only subjects with real quizzes are listed. Existing quiz questions, answer cards and results UI **have not yet been restyled**, because the full local `quiz/` source files are not in GitHub.

## Installing into your existing Android Studio LearnNest project

The GitHub repo still does **not contain the full Android Studio project**, so do not replace your entire existing project with this branch.

1. Make a **backup** of your working `C:\Users\ammik\AndroidStudioProjects\LearnNest` project.
2. In this branch download two updated files (or the branch ZIP) and copy them to:
   - `app/src/main/java/com/example/learnnest/MainActivity.kt`
   - `app/src/main/java/com/example/learnnest/ImportedWordCoursesScreen.kt`
3. If you have newer local changes to either file, **merge** rather than blindly overwrite.
4. **Keep** `tools/import_word_courses.py`, `course-content/`, `app/src/main/assets/generated_courses/`, and your other existing source files. This UI-only step does not require reimporting if the generated assets are current.
5. Android Studio → **File → Sync Project with Gradle Files** → **Build → Rebuild Project** → **Run** on your phone.
6. Test both Home cards, Engineering Graphics → Unit → Topic, Back/Home, Word diagrams/PDF, Previous/Topics/Next, quiz subject list, quiz attempt/results.

## Status and known limitations

- The code is committed to `word-import-integration-review` and is **not merged into `main`**.
- No full Android compilation or device test has been performed here because the rest of the app remains on your Windows computer.
- The mockup is illustrative; exact icons, device fonts and colors may differ.
- The full 8-screen match needs the current Kotlin files under `app/src/main/java/com/example/learnnest/quiz/` to style actual question/options and results screens.
- No account, online database, subscription, or internet connection has been added.

## Reader UI refinements after on-device feedback

The owner tested the previous UI on a Xiaomi phone. The Home design was acceptable, but the lesson reader did not match the mockup closely enough.

This branch now also:
- Replaces the large, mostly empty reading-screen app bar with a **compact 50dp Back / centered title / outlined Home-icon** row; reader topics keep an intentionally blank top-center title to avoid duplication.
- Uses a white bottom navigation strip with equally sized **Previous**, **Topics**, and **Next** controls. Disabled Previous/Next are visibly disabled.
- Reduces title padding and improves the Word HTML reading font (15px) and margins.
- Leaves Word document content, engineering diagrams and PDF documents intact.

**To apply the reader-only fix** (if you already installed the approved Home design), update only:
- `app/src/main/java/com/example/learnnest/ImportedWordCoursesScreen.kt`
- `tools/import_word_courses.py`

Then run at your LearnNest project root:

```cmd
py -3 tools\import_word_courses.py course-content app\src\main\assets\generated_courses
```

Finally rebuild and run from Android Studio. A Gradle Sync alone does not reinstall the app or repackage its content.

**Note:** Very long/raw Word paragraphs reflect the original document. This styling change improves reading density but does not rewrite or restructure the source material.
