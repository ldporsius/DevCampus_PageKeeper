package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.FormattedLine
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Page
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.TextSpan
import kotlin.sequences.forEach


@Serializable
sealed interface PageElement{
    fun toPlainText(): String
    fun toFormattedText(): String = toPlainText()
}

@Serializable
data class Section(
    val id: Int = 0,
    val elements: List<PageElement> = emptyList()
): PageElement {
    override fun toPlainText(): String  = elements.map { it.toPlainText() }.joinToString("\n")
    override fun toFormattedText(): String {
        //return elements.map { it.toFormattedText() }.joinToString("")
        return Chapter(
            title = elements.first().toFormattedText(),
            elements = elements.drop(1)
        ).toFormattedText()
    }
}

data class Chapter(val title: String, val elements: List<PageElement>): PageElement{
    override fun toPlainText(): String {
        return StringBuilder()
            .appendLine(title)
            .appendLine(elements.map { it.toPlainText() })
            .toString()
    }

    override fun toFormattedText(): String {
        return StringBuilder()
            .appendLine(title)
            .append(elements.map { it.toFormattedText() })
            .toString()
    }

}
@Serializable
data class Paragraph(val text: String): PageElement{
    override fun toPlainText(): String = text
    override fun toFormattedText(): String = text.replace(Regex(paragraphTag), "$1").plus("\n")
}

@Serializable
data class Title(val text: String): PageElement{
    override fun toPlainText(): String = text
}

@Serializable
data class Citation(val text: String): PageElement{
    override fun toPlainText(): String = text
}

@Serializable
data class Epigraph(val text: String): PageElement{
    override fun toPlainText(): String = text
}

class PageBuilder{

    private val _sections = mutableMapOf<Int,Section>()
    val sections
        get() = _sections.toMap()

    fun addElementToSection(section: Section, element: PageElement): PageBuilder{

        val currentOrNew = _sections
            .getOrDefault(section.id, null) ?: section

        val update = currentOrNew.copy(
            elements = currentOrNew.elements.plus(element)
        )

        _sections[update.id] = update

        return this
    }

    fun clear(){
        _sections.clear()
    }
}


////////////////////////presentation really////////////////////////////////
suspend fun Section.toPages(): List<Page> {
    return elements.map { it.toPage() }
}

suspend fun PageElement.toPage(): Page {
    return Page.TextPage(
        lines = listOf(
            parseSpans(toFormattedText()))
        )
}

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
    spans.flatMap { span -> splitByPattern(span, regex) { match, parent ->
        parent.copy(text = match.groupValues[1], bold = true)
    }}
}

private val emphasisDecorator = SpanDecorator { spans ->
    val regex = Regex("<emphasis>(.*?)</emphasis>", RegexOption.DOT_MATCHES_ALL)
    spans.flatMap { span -> splitByPattern(span, regex) { match, parent ->
        parent.copy(text = match.groupValues[1], emphasis = true)
    }}
}

private val urlDecorator = SpanDecorator { spans ->
    val regex = Regex("""<a\s[^>]*?\w+:href="([^"]*)"[^>]*?>(.*?)</a>""", RegexOption.DOT_MATCHES_ALL)
    spans.flatMap { span -> splitByPattern(span, regex) { match, parent ->
        val text = tagStripRegex.replace(match.groupValues[2], "")
        parent.copy(text = text, url = match.groupValues[1])
    }}
}

// strong before emphasis so nested <strong><emphasis> inherits bold when emphasis is processed
private val spanDecorators = listOf(strongDecorator, emphasisDecorator, urlDecorator)

private suspend fun parseSpans(content: String): FormattedLine = withContext(Dispatchers.Default) {
    val spans = spanDecorators
        .fold(listOf(TextSpan(content))) { acc, decorator -> decorator.decorate(acc) }
        .map { span -> span.copy(text = tagStripRegex.replace(span.text, "")) }
        //.filter { it.text.isNotBlank() }
    FormattedLine(spans)
}
