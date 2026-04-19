package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.FormattedLine
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.TextSpan

private val tagStripRegex = Regex("<[^>]+>")

private fun interface SpanDecorator {
    fun decorate(spans: List<TextSpan>): List<TextSpan>
}

private fun splitByPattern(
    span: TextSpan,
    regex: Regex,
    transform: (match: MatchResult, parent: TextSpan) -> TextSpan
): List<TextSpan> {
    val result = mutableListOf<TextSpan>()
    var cursor = 0
    regex.findAll(span.text).forEach { match ->
        if (match.range.first > cursor) {
            result += span.copy(text = span.text.substring(cursor, match.range.first))
        }
        result += transform(match, span)
        cursor = match.range.last + 1
    }
    if (cursor < span.text.length) {
        result += span.copy(text = span.text.substring(cursor))
    }
    return result.ifEmpty { listOf(span) }
}

private val strongDecorator = SpanDecorator { spans ->
    val regex = Regex("<strong>(.*?)</strong>", RegexOption.DOT_MATCHES_ALL)
    spans.flatMap { span ->
        splitByPattern(span, regex) { match, parent -> parent.copy(text = match.groupValues[1], bold = true) }
    }
}

private val emphasisDecorator = SpanDecorator { spans ->
    val regex = Regex("<emphasis>(.*?)</emphasis>", RegexOption.DOT_MATCHES_ALL)
    spans.flatMap { span ->
        splitByPattern(span, regex) { match, parent -> parent.copy(text = match.groupValues[1], emphasis = true) }
    }
}

private val urlDecorator = SpanDecorator { spans ->
    val regex = Regex("""<a\s[^>]*?\w+:href="([^"]*)"[^>]*?>(.*?)</a>""", RegexOption.DOT_MATCHES_ALL)
    spans.flatMap { span ->
        splitByPattern(span, regex) { match, parent ->
            val text = tagStripRegex.replace(match.groupValues[2], "")
            parent.copy(text = text, url = match.groupValues[1])
        }
    }
}

// strong before emphasis so nested <strong><emphasis> inherits bold when emphasis is processed
private val spanDecorators = listOf(strongDecorator, emphasisDecorator, urlDecorator)

suspend fun parseSpans(content: String): FormattedLine = withContext(Dispatchers.Default) {
    val spans = spanDecorators
        .fold(listOf(TextSpan(content))) { acc, decorator -> decorator.decorate(acc) }
        .map { span -> span.copy(text = tagStripRegex.replace(span.text, "")) }
    FormattedLine(spans)
}