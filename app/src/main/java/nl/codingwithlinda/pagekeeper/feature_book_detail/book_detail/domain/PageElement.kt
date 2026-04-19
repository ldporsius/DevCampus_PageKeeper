package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain

import kotlinx.serialization.Serializable

@Serializable
sealed interface PageElement {
    fun toPlainText(): String
}

@Serializable
data class Section(
    val id: Int = 0,
    val elements: List<PageElement> = emptyList()
) : PageElement {
    override fun toPlainText(): String = elements.joinToString("\n") { it.toPlainText() }
}

data class Chapter(val title: String, val elements: List<PageElement>) : PageElement {
    override fun toPlainText(): String = title + "\n" + elements.joinToString("\n") { it.toPlainText() }
}

typealias BookParagraph = Paragraph

@Serializable
data class Paragraph(val text: String) : PageElement {
    override fun toPlainText(): String = text
}

@Serializable
data class Title(val text: String) : PageElement {
    override fun toPlainText(): String = text
}

@Serializable
data class Citation(val text: String) : PageElement {
    override fun toPlainText(): String = text
}

@Serializable
data class Epigraph(val text: String) : PageElement {
    override fun toPlainText(): String = text
}