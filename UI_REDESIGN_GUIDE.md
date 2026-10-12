# LearnNest — offline SaaS-style visual refresh

This branch contains the first implementation of a modern SaaS-inspired student interface, **not** a SaaS business model. LearnNest remains offline-first and account-free.

## Implemented in this update

- Compact Home learning dashboard rather than stacked oversized marketing cards
- Gradient hero with links to courses and imported study materials
- Real summary counts from the existing bundled course catalog and progress state
- Continue Learning card based on the already-saved lesson state
- Quick access to Word/PDF materials and quizzes
- Four-item bottom navigation on Home, Subjects, unfiltered Quizzes, and Progress
- Modern outlined cards and compact headings for imported Subjects → Units → Topics
- Reader logic, importer, saved progress, and content formats are unchanged

## Design direction

- Navy / cobalt accent with the existing Material 3 theme
- Rounded cards, subtle borders, consistent readable typography
- A single scrollable dashboard; compact reader controls stay visible
- No cloud services, subscriptions, sign-in, analytics, ads, or network permissions added

## Installing into the existing Android Studio project

**Do not download this branch as a replacement Android project.** This repository does not yet contain the full local app.

1. Back up your current local LearnNest project.
2. From this branch, copy the updated:
   - `app/src/main/java/com/example/learnnest/MainActivity.kt`
   - `app/src/main/java/com/example/learnnest/ImportedWordCoursesScreen.kt`
3. Replace them only if your current local files match the versions used to create this draft. **If you have edited them locally, merge changes rather than overwriting.**
4. In Android Studio, select **File → Sync Project with Gradle Files**, then **Build → Rebuild Project**, then **Run**.
5. Check Home, Subjects, Practice, Progress, Study Materials, Back/Home/Topics, and Word/PDF reader navigation on the Xiaomi phone.

## Important limitations

- This is a draft UI iteration; it has **not** been compiled against the complete local Android Studio project or tested on a device.
- The dashboard's subject count reflects the existing `courses.json` catalog, **not** the separate imported-subject library.
- The completed-lesson count reflects the existing progress model; imported topics are not yet included in progress tracking.
- Theme colors may differ based on the app's existing Material theme/dynamic color settings.
- The repository does not yet contain all Android project files; it is not buildable as-is.
