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
    override fun toFormattedText(): String = elements.map { it.toFormattedText() }.joinToString("\n")
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

private val spanRegex = Regex(
    """<emphasis>(.*?)</emphasis>|<a\s[^>]*?\w+:href="([^"]*)"[^>]*?>(.*?)</a>|<strong>(.*?)</strong>""",
    RegexOption.DOT_MATCHES_ALL
)
private val tagStripRegex = Regex("<[^>]+>")

private suspend fun parseSpans(content: String): FormattedLine = withContext(Dispatchers.Default){
    val spans = mutableListOf<TextSpan>()
    var cursor = 0
    spanRegex.findAll(content).forEach { match ->
        if (match.range.first > cursor) {
            spans += TextSpan(content.substring(cursor, match.range.first))
        }
        if (match.groups[1] != null) {
            spans += TextSpan(match.groupValues[1], emphasis = true)
        } else if (match.groups[4] != null) {
            spans += TextSpan(match.groupValues[4], bold = true)
        } else {
            val url = match.groupValues[2]
            val text = tagStripRegex.replace(match.groupValues[3], "")
            if (text.isNotBlank()) spans += TextSpan(text, url = url)
        }
        cursor = match.range.last + 1
    }
    if (cursor < content.length) {
        spans += TextSpan(content.substring(cursor))
    }
    return@withContext FormattedLine(spans)
}
