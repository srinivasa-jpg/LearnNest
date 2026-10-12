package com.example.learnnest

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

private data class ImportedWordDoc(val id: String, val title: String, val htmlAsset: String)

private fun loadImportedWordDocs(context: Context): List<ImportedWordDoc> {
    val index = context.assets.open("word_courses/index.json").bufferedReader().use { it.readText() }
    val items = JSONObject(index).getJSONArray("documents")
    return (0 until items.length()).mapNotNull { i ->
        val item = items.getJSONObject(i)
        val path = item.getString("htmlAsset")
        if (!path.startsWith("word_courses/documents/") || !path.endsWith(".html") ||
            path.contains("..") || path.contains('\\')) null
        else ImportedWordDoc(item.getString("id"), item.getString("title"), path)
    }
}

/** A separate offline library; does not alter CourseRepository or student progress. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportedWordCoursesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val result = remember(context) { runCatching { loadImportedWordDocs(context) } }
    val docs = result.getOrDefault(emptyList())
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val current = docs.firstOrNull { it.id == selected }

    fun navigateBack() {
        if (selected == null) onBack() else selected = null
    }
    BackHandler { navigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(current?.title ?: "Word Lessons", maxLines = 1) },
                navigationIcon = { TextButton(onClick = ::navigateBack) { Text("‹ Back") } }
            )
        }
    ) { innerPadding ->
        when {
            result.isFailure -> Text(
                "Could not load Word lessons: " + (result.exceptionOrNull()?.message ?: "Unknown error"),
                modifier = Modifier.padding(innerPadding).padding(20.dp)
            )
            current != null -> AndroidView(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
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
                        loadUrl("file:///android_asset/" + current.htmlAsset)
                    }
                },
                update = { webView ->
                    val url = "file:///android_asset/" + current.htmlAsset
                    if (webView.url != url) webView.loadUrl(url)
                },
                onRelease = { webView -> webView.destroy() }
            )
            docs.isEmpty() -> Text(
                "No documents found. Put .docx files in word-course-inbox and rebuild.",
                modifier = Modifier.padding(innerPadding).padding(20.dp)
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(docs, key = { it.id }) { doc ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { selected = doc.id }) {
                        Column(Modifier.padding(18.dp)) {
                            Text(doc.title)
                            Text("Read text and diagrams offline")
                        }
                    }
                }
            }
        }
    }
}
