package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain

import kotlinx.serialization.Serializable

@Serializable
sealed interface PageElement {
    val id: Int
    fun toPlainText(): String
}

@Serializable
data class Section(
    override val id: Int = 0,
    val elements: List<PageElement> = emptyList()
) : PageElement {
    override fun toPlainText(): String = elements.joinToString("") { it.toPlainText() }
}

@Serializable
data class InnerSection(
    override val id: Int = 0,
    val elements: List<PageElement> = emptyList()
): PageElement{
    override fun toPlainText(): String = elements.joinToString("") { it.toPlainText() }
}

typealias BookParagraph = Paragraph

@Serializable
data class Paragraph(override val id: Int = 0, val text: String) : PageElement {
    override fun toPlainText(): String = text
}

@Serializable
data class Title(override val id: Int = 0, val text: String) : PageElement {
    override fun toPlainText(): String = text
}

@Serializable
data class Citation(override val id: Int = 0, val text: String) : PageElement {
    override fun toPlainText(): String = text
}

@Serializable
data class Epigraph(override val id: Int = 0, val text: String) : PageElement {
    override fun toPlainText(): String = text
}