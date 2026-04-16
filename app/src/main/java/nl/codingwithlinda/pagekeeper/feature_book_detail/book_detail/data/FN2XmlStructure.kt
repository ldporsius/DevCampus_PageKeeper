package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data

import kotlinx.serialization.Serializable
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.FormattedLine
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Page
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.TextSpan


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
    override fun toFormattedText(): String = text.replace(Regex(paragraphTag), "$1")
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

fun Section.toPages(): List<Page> {
    return elements.map { it.toPage() }
}

fun PageElement.toPage(): Page {

    return Page.TextPage(
        lines = listOf(
            FormattedLine(
                spans = listOf(
                    TextSpan(
                        text = toFormattedText(),
                        emphasis = toPlainText().contains("<emphasis>"),
                        url = null
                    )
                )
            )
        )
    )
}

