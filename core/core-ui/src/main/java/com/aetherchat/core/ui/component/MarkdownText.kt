package com.aetherchat.core.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.aetherchat.core.ui.theme.AppSpacing

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
) {
    SelectionContainer {
        androidx.compose.material3.Text(
            text = parseMarkdown(text),
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier,
        )
    }
}

@Composable
fun CodeBlock(
    code: String,
    language: String = "",
    onCopy: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(AppSpacing.md)) {
                if (language.isNotBlank()) {
                    androidx.compose.material3.Text(
                        text = language,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SelectionContainer {
                    androidx.compose.material3.Text(
                        text = code,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

private fun parseMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        var inCodeBlock = false
        var codeBlockLanguage = ""
        val codeBlockContent = StringBuilder()

        for (line in lines) {
            when {
                line.trimStart().startsWith("```") && !inCodeBlock -> {
                    inCodeBlock = true
                    codeBlockLanguage = line.trimStart().removePrefix("```").trim()
                    codeBlockContent.clear()
                }
                line.trimStart().startsWith("```") && inCodeBlock -> {
                    inCodeBlock = false
                    append(codeBlockContent.toString())
                    append("\n")
                }
                inCodeBlock -> {
                    codeBlockContent.append(line)
                    codeBlockContent.append("\n")
                }
                line.startsWith("### ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.titleMedium.fontSize)) {
                        append(line.removePrefix("### "))
                    }
                    append("\n")
                }
                line.startsWith("## ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.titleLarge.fontSize)) {
                        append(line.removePrefix("## "))
                    }
                    append("\n")
                }
                line.startsWith("# ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.headlineMedium.fontSize)) {
                        append(line.removePrefix("# "))
                    }
                    append("\n")
                }
                line.startsWith("**") && line.endsWith("**") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("**").removeSuffix("**"))
                    }
                    append("\n")
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    append("• ")
                    append(line.removePrefix("- ").removePrefix("* "))
                    append("\n")
                }
                line.startsWith("`") && line.endsWith("`") -> {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.3f))) {
                        append(line.removePrefix("`").removeSuffix("`"))
                    }
                    append("\n")
                }
                else -> {
                    append(line)
                    append("\n")
                }
            }
        }
    }
}

@Composable
private fun Column(modifier: Modifier, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Column(modifier = modifier, content = content)
}
