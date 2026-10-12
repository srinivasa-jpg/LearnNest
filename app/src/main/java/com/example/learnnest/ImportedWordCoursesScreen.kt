package com.example.learnnest

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import kotlin.math.min
import kotlin.math.roundToInt

private data class DocumentTopic(val id: String, val title: String, val type: String, val asset: String)
private data class DocumentUnit(val id: String, val title: String, val topics: List<DocumentTopic>)
private data class DocumentSubject(val id: String, val title: String, val units: List<DocumentUnit>)

private fun loadDocumentSubjects(context: Context): List<DocumentSubject> {
    val source = context.assets.open("generated_courses/index.json").bufferedReader().use { it.readText() }
    val subjectsJson = JSONObject(source).getJSONArray("subjects")
    return (0 until subjectsJson.length()).map { s ->
        val subject = subjectsJson.getJSONObject(s)
        val unitsJson = subject.getJSONArray("units")
        val units = (0 until unitsJson.length()).map { u ->
            val unit = unitsJson.getJSONObject(u)
            val topicsJson = unit.getJSONArray("topics")
            val topics = (0 until topicsJson.length()).map { t ->
                val topic = topicsJson.getJSONObject(t)
                val type = topic.getString("type")
                val asset = topic.getString("asset")
                val expected = if (type == "word") ".html" else ".pdf"
                require(type == "word" || type == "pdf") { "Unknown topic type" }
                require(asset.startsWith("generated_courses/" + (if (type == "word") "documents/" else "pdfs/")) &&
                        asset.endsWith(expected) && !asset.contains("..") && !asset.contains('\\')) {
                    "Invalid lesson asset path"
                }
                DocumentTopic(topic.getString("id"), topic.getString("title"), type, asset)
            }
            DocumentUnit(unit.getString("id"), unit.getString("title"), topics)
        }
        DocumentSubject(subject.getString("id"), subject.getString("title"), units)
    }
}

/** Offline subject -> unit -> topic browser for generated Word and PDF documents. */
@Composable
fun ImportedWordCoursesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val catalog = remember(context) { runCatching { loadDocumentSubjects(context) } }
    val subjects = catalog.getOrDefault(emptyList())
    var subjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var unitId by rememberSaveable { mutableStateOf<String?>(null) }
    var topicId by rememberSaveable { mutableStateOf<String?>(null) }
    val subject = subjects.firstOrNull { it.id == subjectId }
    val unit = subject?.units?.firstOrNull { it.id == unitId }
    val topic = unit?.topics?.firstOrNull { it.id == topicId }

    fun stepBack() {
        when {
            topicId != null -> topicId = null
            unitId != null -> unitId = null
            subjectId != null -> subjectId = null
            else -> onBack()
        }
    }
    BackHandler { stepBack() }

    Scaffold(
        containerColor = Color(0xFFF7F9FE),
        topBar = {
            Surface(color = Color.White, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().height(50.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = ::stepBack) { Text("‹ Back", color = Color(0xFF183976)) }
                    Text(
                        when {
                            topic != null -> ""
                            unit != null -> unit.title.substringBefore(" - ").take(18)
                            subject != null -> subject.title
                            else -> "Course Content"
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF182747)
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp).semantics { contentDescription = "Home" }
                    ) {
                        Text("⌂", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF183976))
                    }
                }
            }
        }
    ) { pad ->
        when {
            catalog.isFailure -> Text(
                "Import error: " + catalog.exceptionOrNull()?.message,
                Modifier.padding(pad).padding(20.dp)
            )
            topic != null && unit != null -> {
                val topicIndex = unit.topics.indexOfFirst { it.id == topic.id }
                Column(Modifier.padding(pad).fillMaxSize()) {
                    Text(
                        topic.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF152544),
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        maxLines = 2
                    )
                    key(topic.id) {
                        if (topic.type == "word") OfflineWordPage(topic.asset, Modifier.weight(1f))
                        else OfflinePdfPage(topic.asset, Modifier.weight(1f))
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            enabled = topicIndex > 0,
                            onClick = { topicId = unit.topics[topicIndex - 1].id }
                        ) { Text("‹ Previous") }
                        OutlinedButton(onClick = { topicId = null }) { Text("☷ Topics") }
                        Button(
                            enabled = topicIndex >= 0 && topicIndex < unit.topics.lastIndex,
                            onClick = { topicId = unit.topics[topicIndex + 1].id },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1974EF))
                        ) { Text("Next ›") }
                    }
                }
            }
            unit != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(unit.topics, key = { it.id }) { entry ->
                        LibraryBrowseCard(
                            title = entry.title,
                            subtitle = null,
                            kind = LibraryCardKind.TOPIC,
                            index = unit.topics.indexOf(entry),
                            onClick = { topicId = entry.id }
                        )
                    }
                }
            }
            subject != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(subject.units, key = { it.id }) { entry ->
                        LibraryBrowseCard(
                            title = entry.title.substringBefore(" - "),
                            subtitle = entry.title.substringAfter(" - ", entry.topics.size.toString() + " topics"),
                            kind = LibraryCardKind.UNIT,
                            index = subject.units.indexOf(entry),
                            onClick = { unitId = entry.id }
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (subjects.isEmpty()) item {
                        Text("No course documents yet. Add .docx or .pdf to course-content/Subject/Unit and rebuild.")
                    }
                    items(subjects, key = { it.id }) { entry ->
                        LibraryBrowseCard(
                            title = entry.title,
                            subtitle = entry.units.size.toString() + " units",
                            kind = LibraryCardKind.SUBJECT,
                            index = subjects.indexOf(entry),
                            onClick = { subjectId = entry.id }
                        )
                    }
                }
            }
        }
    }
}

private enum class LibraryCardKind { SUBJECT, UNIT, TOPIC }

@Composable
private fun LibraryBrowseCard(
    title: String,
    subtitle: String?,
    kind: LibraryCardKind,
    index: Int,
    onClick: () -> Unit
) {
    val backgrounds = listOf(
        Color(0xFFE6F3FF), Color(0xFFE8FAF0),
        Color(0xFFFFE9EA), Color(0xFFF0EAFE)
    )
    val accents = listOf(
        Color(0xFF1769DF), Color(0xFF0C9B62),
        Color(0xFFCE3B48), Color(0xFF694BCC)
    )
    val paletteIndex = if (kind == LibraryCardKind.SUBJECT) {
        when {
            title.contains("math", ignoreCase = true) -> 0
            title.contains("science", ignoreCase = true) -> 1
            title.contains("english", ignoreCase = true) -> 2
            title.contains("engineering", ignoreCase = true) -> 3
            else -> index.coerceAtLeast(0) % backgrounds.size
        }
    } else index.coerceAtLeast(0) % backgrounds.size
    val accent = if (kind == LibraryCardKind.UNIT) Color(0xFF6A4FC9)
                 else if (kind == LibraryCardKind.TOPIC) Color(0xFF6651B8)
                 else accents[paletteIndex]
    val background = when (kind) {
        LibraryCardKind.SUBJECT -> backgrounds[paletteIndex]
        LibraryCardKind.UNIT -> Color(0xFFF1EBFF)
        LibraryCardKind.TOPIC -> Color.White
    }
    val symbol = when (kind) {
        LibraryCardKind.SUBJECT -> listOf("▦", "⚛", "▤", "✎")[paletteIndex]
        LibraryCardKind.UNIT -> "▱"
        LibraryCardKind.TOPIC -> "▤"
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        border = if (kind == LibraryCardKind.TOPIC)
            BorderStroke(1.dp, Color(0xFFE5E8F0)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 19.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            androidx.compose.material3.Surface(
                shape = RoundedCornerShape(14.dp),
                color = accent.copy(alpha = 0.10f),
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Box(
                    modifier = Modifier.padding(13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        symbol,
                        style = MaterialTheme.typography.titleLarge,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF172747)
                )
                if (subtitle != null) Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF586783)
                )
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = Color(0xFF24395B))
        }
    }
}

@Composable
private fun OfflineWordPage(asset: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = false
                settings.domStorageEnabled = false
                settings.allowContentAccess = false
                settings.allowFileAccess = true
                settings.allowFileAccessFromFileURLs = false
                settings.allowUniversalAccessFromFileURLs = false
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                loadUrl("file:///android_asset/" + asset)
            }
        },
        update = { web ->
            val path = "file:///android_asset/" + asset
            if (web.url != path) web.loadUrl(path)
        },
        onRelease = { it.destroy() }
    )
}

private data class RenderedPdfPage(val bitmap: Bitmap, val pageCount: Int)

private fun drawPdfPage(context: Context, asset: String, pageIndex: Int): RenderedPdfPage {
    val temporary = File.createTempFile("learnnest-document-", ".pdf", context.cacheDir)
    try {
        context.assets.open(asset).use { source ->
            temporary.outputStream().use { target -> source.copyTo(target) }
        }
        val descriptor = ParcelFileDescriptor.open(temporary, ParcelFileDescriptor.MODE_READ_ONLY)
        descriptor.use {
            PdfRenderer(descriptor).use { renderer ->
                require(pageIndex in 0 until renderer.pageCount) { "PDF page is out of range" }
                renderer.openPage(pageIndex).use { page ->
                    val factor = min(1600f / page.width, 2400f / page.height)
                    val bitmap = Bitmap.createBitmap(
                        (page.width * factor).roundToInt().coerceAtLeast(1),
                        (page.height * factor).roundToInt().coerceAtLeast(1),
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    return RenderedPdfPage(bitmap, renderer.pageCount)
                }
            }
        }
    } finally {
        temporary.delete()
    }
}

@Composable
private fun OfflinePdfPage(asset: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var index by rememberSaveable(asset) { mutableStateOf(0) }
    val rendered by produceState<Result<RenderedPdfPage>?>(null, asset, index) {
        value = null
        value = withContext(Dispatchers.IO) { runCatching { drawPdfPage(context, asset, index) } }
    }
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        when {
            rendered == null -> Text("Loading PDF page…", Modifier.padding(16.dp))
            rendered?.isFailure == true -> Text(
                "Unable to display PDF: " + rendered?.exceptionOrNull()?.message,
                Modifier.padding(16.dp)
            )
            rendered?.isSuccess == true -> {
                val page = rendered!!.getOrThrow()
                var scale by remember(index) { mutableStateOf(1f) }
                var offset by remember(index) { mutableStateOf(Offset.Zero) }
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = page.bitmap.asImageBitmap(),
                        contentDescription = "PDF page " + (index + 1),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                            .pointerInput(index) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 4f)
                                    offset = if (scale == 1f) Offset.Zero else offset + pan
                                }
                            }
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            }
                    )
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { index-- }, enabled = index > 0) { Text("‹ Page") }
                    Text((index + 1).toString() + " / " + page.pageCount)
                    TextButton(onClick = { index++ }, enabled = index + 1 < page.pageCount) { Text("Page ›") }
                }
            }
        }
    }
}
