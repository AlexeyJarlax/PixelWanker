package com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components

import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp200
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp244
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

@Composable
fun PrivacyPolicyContent(
    assetName: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val markdownText = remember(assetName) { loadAssetText(context, assetName) }
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val backgroundColor = MaterialTheme.colorScheme.surface.toArgb()
    val htmlContent = remember(markdownText, textColor, backgroundColor) {
        val body = markdownToHtml(markdownText)
        """
            |<html>
            |<head>
            |<meta charset="utf-8">
            |<style>
            |body {
            |  color: ${textColor.toHexColor()};
            |  background-color: ${backgroundColor.toHexColor()};
            |  font-family: sans-serif;
            |  line-height: 1.5;
            |  padding: 12px;
            |}
            |h1, h2, h3 {
            |  font-weight: 600;
            |}
            |a { color: ${textColor.toHexColor()}; }
            |</style>
            |</head>
            |<body>$body</body>
            |</html>
        """.trimMargin()
    }

    AndroidView(
        factory = { webContext ->
            WebView(webContext).apply {
                setBackgroundColor(backgroundColor)
                loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
            }
        },
        update = { webView ->
            webView.setBackgroundColor(backgroundColor)
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = dp200, max = dp244)
    )
}

private fun loadAssetText(context: android.content.Context, assetName: String): String {
    return context.assets.open(assetName).bufferedReader().use { it.readText() }
}

private fun markdownToHtml(markdown: String): String {
    val parser = Parser.builder().build()
    val document = parser.parse(markdown)
    return HtmlRenderer.builder().build().render(document)
}

private fun Int.toHexColor(): String = String.format("#%06X", 0xFFFFFF and this)
