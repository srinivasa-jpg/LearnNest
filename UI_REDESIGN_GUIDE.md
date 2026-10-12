# LearnNest — simplified two-option Home screen

This GitHub development branch implements the user's latest UI decision.

## Home screen

The Home screen contains only **two clickable cards**:

1. **Course Content** → reads generated local subjects, units and topic documents from the existing `course-content/` importer.
2. **Quizzes** → opens the existing quiz catalog and results flow.

Removed from Home: dashboard hero, subject lists, metrics, progress cards, Continue Learning cards, Settings shortcut, and the four-tab bottom navigation. Internal quiz, progress, settings and existing course logic have not been deleted.

## Course Content browser

- Top app bar has **Back** and **Home** only. No long, repeated title.
- Subject screen: subject title and unit cards.
- Unit screen: unit title and topic cards displaying only the filename-based lesson titles.
- Reader: document content with **Previous**, **Topics**, and **Next** buttons; PDFs retain page controls.
- Redundant display labels such as "Imported Subjects", "Your Offline Library", "Word Lesson", and "Select a Word or PDF lesson" are removed from these screens.
- Import script and source file structure are unchanged.

## Update local Android Studio files

The GitHub repository still does **not** contain the complete Android app. Code is staged on the branch `word-import-integration-review` only.

1. Back up your existing `LearnNest` folder.
2. Download the branch and copy the updated `MainActivity.kt` and `ImportedWordCoursesScreen.kt` into the matching `app/src/main/java/com/example/learnnest/` paths.
3. If your current local files have other changes, **merge rather than replace**.
4. In Android Studio, sync Gradle, rebuild and run.
5. Verify Home only shows Course Content and Quizzes. Tap Course Content → Engineering Graphics → Unit → Topic and test Back/Home/Previous/Topics/Next. Tap Quizzes and check an attempt/results screen.

**Not yet compiled or verified on the owner's complete Android Studio project or phone.** Continue using `course-content/` for your original Word and PDF files. The importer uses those files on future builds.
