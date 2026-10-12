package com.example.learnnest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learnnest.data.Chapter
import com.example.learnnest.data.Course
import com.example.learnnest.data.CourseRepository
import com.example.learnnest.data.Lesson
import com.example.learnnest.data.LessonContent
import com.example.learnnest.quiz.QuizEmptyScreen
import com.example.learnnest.quiz.QuizListScreen
import com.example.learnnest.quiz.QuizQuestionScreen
import com.example.learnnest.quiz.QuizRepository
import com.example.learnnest.quiz.QuizResultsScreen
import com.example.learnnest.quiz.QuizReviewScreen
import com.example.learnnest.quiz.QuizViewModel
import com.example.learnnest.ui.theme.LearnNestTheme
import com.example.learnnest.progress.*
import com.example.learnnest.quiz.scoreQuiz

private val Ink: Color @Composable get() = MaterialTheme.colorScheme.onSurface
private val MutedInk: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val Canvas: Color @Composable get() = MaterialTheme.colorScheme.background
internal val Brand: Color @Composable get() = MaterialTheme.colorScheme.primary

private enum class Screen { HOME, SUBJECTS, COURSES, CHAPTERS, LESSONS, READER, QUIZZES, QUIZ_QUESTION, QUIZ_RESULTS, QUIZ_REVIEW, PROGRESS, SETTINGS, WORD_COURSES }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearnNestTheme {
                LearnNestApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LearnNestApp() {
    val context = LocalContext.current
    val catalogState by androidx.compose.runtime.produceState<Result<List<Course>>?>(null, context) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            runCatching { CourseRepository(context.assets).loadCourses() }
        }
    }
    val catalog = catalogState ?: Result.success(emptyList())
    val courses = catalog.getOrDefault(emptyList())
    val quizState by androidx.compose.runtime.produceState<Result<List<com.example.learnnest.quiz.Quiz>>?>(null, catalogState) {
        value = null
        if (catalogState != null) value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            if (catalog.isSuccess) runCatching { QuizRepository(context.assets).loadQuizzes(courses) }
            else Result.success(emptyList())
        }
    }
    val quizCatalog = quizState ?: Result.success(emptyList())
    val quizzes = quizCatalog.getOrDefault(emptyList())
    val quizViewModel: QuizViewModel = viewModel()
    val progressViewModel: ProgressViewModel = viewModel()
    val progress = progressViewModel.progress
    val selectedQuiz = quizzes.firstOrNull { it.id == quizViewModel.quizId }
    var screenName by rememberSaveable { mutableStateOf(Screen.HOME.name) }
    var subject by rememberSaveable { mutableStateOf<String?>(null) }
    var quizCourseId by rememberSaveable { mutableStateOf<String?>(null) }
    var quizChapterId by rememberSaveable { mutableStateOf<String?>(null) }
    var quizReturn by rememberSaveable { mutableStateOf(Screen.HOME.name) }
    var courseId by rememberSaveable { mutableStateOf<String?>(null) }
    var chapterId by rememberSaveable { mutableStateOf<String?>(null) }
    var lessonId by rememberSaveable { mutableStateOf<String?>(null) }
    val screen = Screen.entries.firstOrNull { it.name == screenName } ?: Screen.HOME
    val course = courses.firstOrNull { it.id == courseId }
    val chapter = course?.chapters?.firstOrNull { it.id == chapterId }
    val lesson = chapter?.lessons?.firstOrNull { it.id == lessonId }
    val isPdfReader = screen == Screen.READER && lesson?.content is LessonContent.Pdf

    LaunchedEffect(screen, course?.id, lesson?.id, progressViewModel.ready) {
        if (screen == Screen.READER && course != null && lesson != null && lesson.isPublished() && progressViewModel.ready) {
            progressViewModel.accessLesson(LessonKey(course.id, lesson.id))
        }
    }

    fun goBack() {
        if (progressViewModel.busy) return
        screenName = when (screen) {
            Screen.HOME -> Screen.HOME.name
            Screen.PROGRESS, Screen.SETTINGS, Screen.WORD_COURSES -> Screen.HOME.name
            Screen.SUBJECTS -> Screen.HOME.name
            Screen.COURSES -> Screen.SUBJECTS.name
            Screen.CHAPTERS -> Screen.COURSES.name
            Screen.LESSONS -> Screen.CHAPTERS.name
            Screen.READER -> Screen.LESSONS.name
            Screen.QUIZZES -> quizReturn
            Screen.QUIZ_QUESTION -> Screen.QUIZZES.name
            Screen.QUIZ_RESULTS -> Screen.QUIZZES.name
            Screen.QUIZ_REVIEW -> Screen.QUIZ_RESULTS.name
        }
    }

    BackHandler(enabled = screen != Screen.HOME && screen != Screen.WORD_COURSES) { goBack() }

    if (screen == Screen.WORD_COURSES) {
        ImportedWordCoursesScreen(onBack = { screenName = Screen.HOME.name })
        return
    }

    Scaffold(
        containerColor = Canvas,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (screen == Screen.HOME || screen == Screen.SUBJECTS ||
                screen == Screen.PROGRESS || (screen == Screen.QUIZZES && quizCourseId == null)
            ) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(
                        selected = screen == Screen.HOME,
                        onClick = { screenName = Screen.HOME.name },
                        icon = { Text("⌂", style = MaterialTheme.typography.titleLarge) },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = screen == Screen.SUBJECTS,
                        onClick = { screenName = Screen.SUBJECTS.name },
                        icon = { Text("▦", style = MaterialTheme.typography.titleLarge) },
                        label = { Text("Subjects") }
                    )
                    NavigationBarItem(
                        selected = screen == Screen.QUIZZES,
                        onClick = {
                            quizCourseId = null
                            quizChapterId = null
                            quizReturn = Screen.HOME.name
                            screenName = Screen.QUIZZES.name
                        },
                        icon = { Text("✎", style = MaterialTheme.typography.titleLarge) },
                        label = { Text("Practice") }
                    )
                    NavigationBarItem(
                        selected = screen == Screen.PROGRESS,
                        onClick = { screenName = Screen.PROGRESS.name },
                        icon = { Text("▤", style = MaterialTheme.typography.titleLarge) },
                        label = { Text("Progress") }
                    )
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (screen) {
                            Screen.HOME -> "LearnNest"
                            Screen.WORD_COURSES -> "Imported Subjects"
                            Screen.PROGRESS -> "Progress"
                            Screen.SETTINGS -> "Settings"
                            Screen.SUBJECTS -> "Subjects"
                            Screen.COURSES -> subject ?: "Courses"
                            Screen.CHAPTERS -> "Chapters"
                            Screen.LESSONS -> "Lessons"
                            Screen.READER -> if (isPdfReader) "PDF lesson" else "Lesson"
                            Screen.QUIZZES -> "Quizzes"
                            Screen.QUIZ_QUESTION -> "Quiz"
                            Screen.QUIZ_RESULTS -> "Results"
                            Screen.QUIZ_REVIEW -> "Review"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                expandedHeight = if (isPdfReader) 48.dp else 64.dp,
                actions = {
                    if (isPdfReader && course != null && chapter != null && lesson != null) {
                        val key = LessonKey(course.id, lesson.id)
                        val completed = key in progress.completedLessons
                        PdfReaderActions(
                            course, chapter, lesson, completed,
                            progressViewModel.ready && !progressViewModel.busy,
                            onToggleCompleted = { progressViewModel.setCompleted(key, !completed) },
                            onLesson = { nextChapter, nextLesson ->
                                chapterId = nextChapter.id
                                lessonId = nextLesson.id
                            },
                            onCourse = { screenName = Screen.CHAPTERS.name },
                            onQuizzes = {
                                quizCourseId = course.id
                                quizChapterId = chapter.id
                                quizReturn = screen.name
                                screenName = Screen.QUIZZES.name
                            }
                        )
                    }
                    if (!isPdfReader && screen in listOf(Screen.CHAPTERS, Screen.LESSONS, Screen.READER) && course != null) {
                        TextButton(onClick = {
                            quizCourseId = course.id
                            quizChapterId = if (screen == Screen.CHAPTERS) null else chapter?.id
                            quizReturn = screen.name
                            screenName = Screen.QUIZZES.name
                        }) { Text("Quizzes") }
                    }
                    if (screen == Screen.HOME) {
                        TextButton(onClick = { screenName = Screen.SETTINGS.name }) { Text("Settings") }
                    }
                },
                navigationIcon = {
                    if (screen != Screen.HOME) {
                        IconButton(onClick = ::goBack) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "Back",
                                tint = Ink
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = Ink
                )
            )
        }
    ) { padding ->
        val modifier = Modifier.padding(padding).consumeWindowInsets(padding)
        when {
            catalogState == null || quizState == null -> Column(modifier.padding(24.dp)) { Text("Loading offline content…") }
            !progressViewModel.ready -> Column(modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Loading local learning progress…")
                Button(onClick = progressViewModel::reload) { Text("Retry") }
            }
            screen == Screen.SETTINGS -> SettingsScreen(
                enabled = !progressViewModel.busy,
                onReset = { progressViewModel.reset { quizViewModel.clearAttempt() } },
                modifier = modifier
            )
            screen == Screen.PROGRESS -> ProgressScreen(courses, quizzes, progress, modifier)
            catalog.isFailure -> CatalogError(modifier, catalog.exceptionOrNull()?.message.orEmpty())
            screen == Screen.HOME -> HomeScreen(
                courses = courses,
                modifier = modifier,
                onViewCourses = { screenName = Screen.SUBJECTS.name },
                onViewWordCourses = { screenName = Screen.WORD_COURSES.name },
                onSubject = { subject = it; screenName = Screen.COURSES.name },
                onViewQuizzes = {
                    quizCourseId = null
                    quizChapterId = null
                    quizReturn = Screen.HOME.name
                    screenName = Screen.QUIZZES.name
                },
                progress = progress,
                onContinue = { target ->
                    subject = target.course.subject
                    courseId = target.course.id
                    chapterId = target.chapter.id
                    lessonId = target.lesson.id
                    screenName = Screen.READER.name
                }
            )
            screen == Screen.SUBJECTS -> SubjectList(courses, modifier) {
                subject = it
                screenName = Screen.COURSES.name
            }
            screen == Screen.COURSES -> CoursesScreen(
                courses = courses.filter { it.subject == (subject ?: course?.subject) },
                progress = progress,
                modifier = modifier,
                onSelectCourse = {
                    subject = it.subject
                    courseId = it.id
                    chapterId = null
                    lessonId = null
                    screenName = Screen.CHAPTERS.name
                }
            )
            screen == Screen.CHAPTERS && course != null -> ChaptersScreen(
                course = course,
                progress = progress,
                modifier = modifier,
                onSelectChapter = {
                    chapterId = it.id
                    lessonId = null
                    screenName = Screen.LESSONS.name
                }
            )
            screen == Screen.LESSONS && course != null && chapter != null -> LessonsScreen(
                course = course,
                chapter = chapter,
                progress = progress,
                modifier = modifier,
                onSelectLesson = {
                    lessonId = it.id
                    screenName = Screen.READER.name
                }
            )
            screen == Screen.READER && course != null && chapter != null && lesson != null -> {
                val key = LessonKey(course.id, lesson.id)
                val completed = key in progress.completedLessons
                Column(modifier.fillMaxSize()) {
                    if (!isPdfReader) {
                    LessonCompletion(completed, !progressViewModel.busy && (lesson.content as? LessonContent.Pdf)?.placeholder != true) {
                        progressViewModel.setCompleted(key, !completed)
                    }
                    LessonNavigation(course, lesson, onLesson = { nextChapter, nextLesson ->
                        chapterId = nextChapter.id
                        lessonId = nextLesson.id
                    }, onCourse = { screenName = Screen.CHAPTERS.name })
                    }
                    androidx.compose.runtime.key(course.id, lesson.id) {
                        when (val content = lesson.content) {
                            is LessonContent.Text -> LessonReaderScreen(course, chapter, lesson, content, Modifier.weight(1f))
                            is LessonContent.Pdf -> PdfLessonScreen(lesson, Modifier.weight(1f))
                        }
                    }
                }
            }
            screen == Screen.QUIZZES -> {
                if (quizCatalog.isFailure) QuizEmptyScreen(
                    "Quizzes are unavailable: ${quizCatalog.exceptionOrNull()?.message}",
                    modifier
                ) else QuizListScreen(
                    quizzes = quizzes.filter {
                        (quizCourseId == null || it.courseId == quizCourseId) &&
                            (quizChapterId == null || it.chapterId == null || it.chapterId == quizChapterId)
                    },
                    courses = courses,
                    latestScore = { progress.latest(it)?.percentage?.toInt() },
                    onStart = { quiz ->
                        courseId = quiz.courseId
                        quizViewModel.start(quiz)
                        screenName = Screen.QUIZ_QUESTION.name
                    },
                    modifier = modifier
                )
            }
            screen == Screen.QUIZ_QUESTION && selectedQuiz != null -> {
                when {
                    !quizViewModel.matches(selectedQuiz) -> Column(modifier.padding(24.dp)) {
                        Text("This quiz has changed. Start a new attempt; saved results are kept.")
                        Button(onClick = { quizViewModel.start(selectedQuiz) }) { Text("Restart quiz") }
                    }
                    progressViewModel.busy -> Column(modifier.padding(24.dp)) { Text("Saving quiz result…") }
                    else -> QuizQuestionScreen(
                        quiz = selectedQuiz,
                        viewModel = quizViewModel,
                        onSubmitted = {
                            val result = scoreQuiz(selectedQuiz, quizViewModel.answers)
                            progressViewModel.saveAttempt(
                                QuizAttempt(
                                    quizViewModel.attemptId, selectedQuiz.courseId, selectedQuiz.id,
                                    selectedQuiz.title, result.correctAnswers, result.totalQuestions,
                                    quizViewModel.submissionTime()
                                )
                            ) {
                                quizViewModel.submit(selectedQuiz)
                                screenName = Screen.QUIZ_RESULTS.name
                            }
                        },
                        modifier = modifier
                    )
                }
            }
            screen == Screen.QUIZ_RESULTS && selectedQuiz != null -> {
                val result = quizViewModel.result(selectedQuiz)
                if (result == null) QuizEmptyScreen("No submitted result is available.", modifier)
                else QuizResultsScreen(
                    quiz = selectedQuiz,
                    result = result,
                    onReview = { screenName = Screen.QUIZ_REVIEW.name },
                    onRetry = {
                        quizViewModel.start(selectedQuiz)
                        screenName = Screen.QUIZ_QUESTION.name
                    },
                    onBackToCourse = {
                        subject = courses.firstOrNull { it.id == selectedQuiz.courseId }?.subject
                        courseId = selectedQuiz.courseId
                        quizReturn = Screen.CHAPTERS.name
                        chapterId = null
                        lessonId = null
                        screenName = Screen.CHAPTERS.name
                    },
                    modifier = modifier
                )
            }
            screen == Screen.QUIZ_REVIEW && selectedQuiz != null -> {
                val result = quizViewModel.result(selectedQuiz)
                if (result == null) QuizEmptyScreen("No submitted result is available.", modifier)
                else QuizReviewScreen(selectedQuiz, result, modifier)
            }
            else -> QuizEmptyScreen("This content is no longer available. Return Home to continue learning. Saved progress is kept.", modifier)
        }
    }
    progressViewModel.error?.let { message ->
        AlertDialog(
            onDismissRequest = progressViewModel::dismissError,
            title = { Text("Learning progress") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = progressViewModel::dismissError) { Text("OK") } }
        )
    }
}

@Composable
private fun HomeScreen(
    courses: List<Course>,
    modifier: Modifier = Modifier,
    onViewCourses: () -> Unit,
    onViewQuizzes: () -> Unit,
    onViewWordCourses: () -> Unit = {},
    progress: LearningProgress = LearningProgress(),
    onContinue: (ResumeLesson) -> Unit = {},
    onSubject: (String) -> Unit = {}
) {
    val subjects = courses.groupBy { it.subject }.toSortedMap()
    val continueTarget = resumeLesson(courses, progress)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "YOUR LEARNING DASHBOARD",
                    style = MaterialTheme.typography.labelSmall,
                    color = Brand,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Learn at your pace.",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    "Your courses, notes and practice — available offline.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk
                )
            }
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF142448), Color(0xFF334EA9))
                        )
                    )
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "LEARNNEST  /  OFFLINE ACADEMY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC6D4FF)
                )
                Text(
                    "Everything you need to keep learning.",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Organized lessons, Word and PDF notes, and practice in one place.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE4E9FF)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onViewCourses,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF263F8E)
                        )
                    ) { Text("Explore courses") }
                    TextButton(onClick = onViewWordCourses) {
                        Text("Study materials  ›", color = Color.White)
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardMetric(
                    value = subjects.size.toString(),
                    label = "Subjects",
                    modifier = Modifier.weight(1f)
                )
                DashboardMetric(
                    value = progress.completedLessons.size.toString(),
                    label = "Lessons completed",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        continueTarget?.let { target ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "PICK UP WHERE YOU LEFT OFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = Brand,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            target.lesson.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            target.course.title + "  ·  " + target.chapter.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Button(onClick = { onContinue(target) }) {
                            Text("Continue lesson  →")
                        }
                    }
                }
            }
        }
        item {
            Text(
                "Quick access",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardActionCard(
                    title = "Study materials",
                    description = "Word & PDF lessons",
                    symbol = "▤",
                    onClick = onViewWordCourses,
                    modifier = Modifier.weight(1f)
                )
                DashboardActionCard(
                    title = "Practice",
                    description = "Quizzes & results",
                    symbol = "✎",
                    onClick = onViewQuizzes,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Explore subjects",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                TextButton(onClick = onViewCourses) { Text("See all  →") }
            }
        }
        items(subjects.keys.toList(), key = { it }) { name ->
            val count = subjects[name]?.size ?: 0
            NavigationCard(
                title = name,
                description = "Open lessons and learning resources",
                detail = "${count} course" + if (count == 1) "" else "s",
                onClick = { onSubject(name) }
            )
        }
    }
}

@Composable
private fun DashboardMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            Text(label, style = MaterialTheme.typography.labelMedium, color = MutedInk)
        }
    }
}

@Composable
private fun DashboardActionCard(
    title: String,
    description: String,
    symbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        symbol,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk,
                maxLines = 2
            )
            Text(
                "Open  →",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Brand
            )
        }
    }
}

@Composable
private fun CoursesScreen(
    courses: List<Course>,
    modifier: Modifier = Modifier,
    onSelectCourse: (Course) -> Unit,
    progress: LearningProgress = LearningProgress()
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenIntro(
                title = "Choose a course",
                description = "Explore lessons at your own pace."
            )
        }
        items(courses, key = { it.id }) { course ->
            CourseCard(course = course, percentage = courseProgress(course, progress).percentage, onClick = { onSelectCourse(course) })
        }
    }
}

@Composable
private fun ChaptersScreen(
    course: Course,
    modifier: Modifier = Modifier,
    onSelectChapter: (Chapter) -> Unit,
    progress: LearningProgress
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenIntro(
                title = course.title,
                description = course.description
            )
        }
        if (course.chapters.isEmpty()) item { Text("No chapters have been published yet.") }
        items(course.chapters, key = { it.id }) { chapter ->
            NavigationCard(
                title = chapter.title,
                description = chapter.description,
                detail = "${chapter.lessons.count { it.isPublished() }} available lessons" + if (chapterCompleted(course.id, chapter, progress)) " · ✓ Completed" else "",
                onClick = { onSelectChapter(chapter) }
            )
        }
    }
}

@Composable
private fun LessonsScreen(
    course: Course,
    chapter: Chapter,
    modifier: Modifier = Modifier,
    onSelectLesson: (Lesson) -> Unit,
    progress: LearningProgress
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenIntro(
                title = chapter.title,
                description = "${course.title} · ${chapter.description}"
            )
        }
        if (chapter.lessons.isEmpty()) item { Text("No lessons have been published in this chapter yet.") }
        items(chapter.lessons, key = { it.id }) { lesson ->
            NavigationCard(
                title = (if (LessonKey(course.id, lesson.id) in progress.completedLessons) "✓ " else "") + lesson.title,
                description = lesson.summary,
                detail = when (lesson.content) {
                    is LessonContent.Text -> "Text lesson"
                    is LessonContent.Pdf -> if (lesson.content.placeholder) "PDF placeholder · not supplied" else "PDF lesson"
                },
                onClick = { onSelectLesson(lesson) }
            )
        }
    }
}

@Composable
private fun LessonReaderScreen(
    course: Course,
    chapter: Chapter,
    lesson: Lesson,
    content: LessonContent.Text,
    modifier: Modifier = Modifier
) {
    val header = com.example.learnnest.data.LessonSection("heading", text = lesson.title)
    val summary = com.example.learnnest.data.LessonSection("paragraph", text = "${course.title} / ${chapter.title}\n${lesson.summary}")
    val body = content.sections.ifEmpty {
        content.body.split("\n\n").map { com.example.learnnest.data.LessonSection("paragraph", text = it) }
    }
    StructuredLesson(content.copy(sections = listOf(header, summary) + body), modifier)
}

@Composable
private fun ScreenIntro(title: String, description: String) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Ink
        )
        Spacer(Modifier.height(8.dp))
        Text(
            description,
            style = MaterialTheme.typography.bodyLarge,
            color = MutedInk
        )
    }
}

@Composable
private fun CourseCard(course: Course, percentage: Int, onClick: () -> Unit) {
    Card(
        onClick = { if (course.status == "available") onClick() },
        enabled = course.status == "available",
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = Color(android.graphics.Color.parseColor(course.lightAccent))
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        course.initial,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(android.graphics.Color.parseColor(course.accent))
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                course.thumbnail?.let { AssetImage(it, "${course.title} thumbnail") }
                if (course.status == "coming_soon") Text("Coming soon")
                Text(
                    course.subtitle.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Brand
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    course.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    course.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk
                )
                Spacer(Modifier.height(10.dp))
                Text("$percentage% complete", style = MaterialTheme.typography.labelLarge, color = Brand)
                LinearProgressIndicator(progress = { percentage / 100f }, modifier = Modifier.fillMaxWidth())
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = MutedInk)
        }
    }
}

@Composable
private fun NavigationCard(
    title: String,
    description: String,
    detail: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    detail.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Brand
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk
                )
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = MutedInk)
        }
    }
}

@Composable
private fun CatalogError(modifier: Modifier = Modifier, detail: String = "") {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Learning content is unavailable",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Ink
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Bundled lessons could not be loaded. Your learning progress is kept.\n$detail",
            style = MaterialTheme.typography.bodyLarge,
            color = MutedInk
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    val context = LocalContext.current
    val result = remember(context) { runCatching { CourseRepository(context.assets).loadCourses() } }
    LearnNestTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            result.fold(
                onSuccess = { HomeScreen(courses = it, onViewCourses = {}, onViewQuizzes = {}) },
                onFailure = { CatalogError(detail = it.message.orEmpty()) }
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CoursesPreview() {
    val context = LocalContext.current
    val result = remember(context) { runCatching { CourseRepository(context.assets).loadCourses() } }
    LearnNestTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            result.fold(
                onSuccess = { CoursesScreen(courses = it, onSelectCourse = {}) },
                onFailure = { CatalogError(detail = it.message.orEmpty()) }
            )
        }
    }
}