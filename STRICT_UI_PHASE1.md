# LearnNest — Strict UI, Phase 1

This stage implements the **foundation and Home screen only** from the selected burgundy/cream 8-screen reference. It does not claim the rest of the app is pixel-identical yet.

## What is included

- `ui/design/LearnNestColors.kt`: one central, shared reference-matched burgundy/ivory palette.
- `ui/design/LearnNestComponents.kt`: reusable Home action cards, original vector graduation cap, book and quiz icons, decorative book landscape.
- `ui/design/LearnNestHomeScreen.kt`: accessible, scrollable two-option Home screen (**Course Content** and **Quizzes** only).
- `MainActivity.kt`: uses the new Home and shared palette; existing navigation, quizzes, importer and saved progress still use their previous code paths.

This phase uses original drawn Compose vector artwork, not the source image from the mockup. The screenshot's book/foliage illustrations will not be pixel-identical without approved source assets.

## REQUIRED one-time file installation

**The 4 files must be applied together**. If `MainActivity.kt` is copied without the new `ui/design/` files, Kotlin will report unresolved references.

Make a backup of the currently working local LearnNest project first. Download this branch ZIP or take the individual files:

```text
LearnNest/
  app/src/main/java/com/example/learnnest/
    MainActivity.kt                                  REPLACE/MERGE
    ui/design/LearnNestColors.kt                      NEW
    ui/design/LearnNestComponents.kt                  NEW
    ui/design/LearnNestHomeScreen.kt                  NEW
```

In Android Studio, switch the Project pane to **Project** view if the nested folders are hard to find. Keep file packages and spelling exactly as committed.

If local files have evolved, carefully **merge** updates; do not overwrite unsaved changes. Do **not** copy over the `quiz/`, `data/`, `progress/` or `ui/theme/` packages.

Then do:

1. **File → Sync Project with Gradle Files**
2. **Build → Rebuild Project**
3. **Run** on the Xiaomi phone
4. Confirm Home has the two cards and that they open the previously working Course Content and Quizzes flows.

Keep `course-content/`, `tools/import_word_courses.py`, `generated_courses/`, your Word/PDF originals and JSON files. No source-content regeneration is needed for Phase 1.

## Testing and limits

- This GitHub repository does not contain the rest of the Android app, so a complete Gradle build/device test cannot be performed here.
- Phase 1 leaves subjects, units, topics, readers and question card layouts essentially unchanged except inherited palette styling.
- Later phases will replace UI screens in sequence while preserving functionality.
- Use a backup and test a debug build before publishing.
