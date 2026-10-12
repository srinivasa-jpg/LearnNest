package com.example.learnnest

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(topic?.title ?: unit?.title ?: subject?.title ?: "Imported Subjects", maxLines = 1)
            },
            navigationIcon = { TextButton(onClick = ::stepBack) { Text("Back") } },
            actions = { TextButton(onClick = onBack) { Text("Home") } }
        )
    }) { pad ->
        when {
            catalog.isFailure -> Text(
                "Import error: " + catalog.exceptionOrNull()?.message,
                Modifier.padding(pad).padding(20.dp)
            )
            topic != null && unit != null -> {
                val topicIndex = unit.topics.indexOfFirst { it.id == topic.id }
                Column(Modifier.padding(pad).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        subject?.title.orEmpty() + " / " + unit.title,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    key(topic.id) {
                        if (topic.type == "word") OfflineWordPage(topic.asset, Modifier.weight(1f))
                        else OfflinePdfPage(topic.asset, Modifier.weight(1f))
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            enabled = topicIndex > 0,
                            onClick = { topicId = unit.topics[topicIndex - 1].id }
                        ) { Text("‹ Previous") }
                        TextButton(onClick = { topicId = null }) { Text("Topics") }
                        TextButton(
                            enabled = topicIndex >= 0 && topicIndex < unit.topics.lastIndex,
                            onClick = { topicId = unit.topics[topicIndex + 1].id }
                        ) { Text("Next ›") }
                    }
                }
            }
            unit != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Text(unit.title, style = MaterialTheme.typography.headlineSmall) }
                    items(unit.topics, key = { it.id }) { entry ->
                        Card(onClick = { topicId = entry.id }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(18.dp)) {
                                Text(entry.title, style = MaterialTheme.typography.titleMedium)
                                Text(if (entry.type == "pdf") "PDF document" else "Word lesson")
                            }
                        }
                    }
                }
            }
            subject != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Text(subject.title, style = MaterialTheme.typography.headlineSmall) }
                    items(subject.units, key = { it.id }) { entry ->
                        Card(onClick = { unitId = entry.id }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(18.dp)) {
                                Text(entry.title, style = MaterialTheme.typography.titleMedium)
                                Text(entry.topics.size.toString() + " topics")
                            }
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Text("Imported Subjects", style = MaterialTheme.typography.headlineSmall) }
                    if (subjects.isEmpty()) item {
                        Text("No course documents yet. Add .docx or .pdf to course-content/Subject/Unit and rebuild.")
                    }
                    items(subjects, key = { it.id }) { entry ->
                        Card(onClick = { subjectId = entry.id }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(18.dp)) {
                                Text(entry.title, style = MaterialTheme.typography.titleMedium)
                                Text(entry.units.size.toString() + " units")
                            }
                        }
                    }
                }
            }
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
