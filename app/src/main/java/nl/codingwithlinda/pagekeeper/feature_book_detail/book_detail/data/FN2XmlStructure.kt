package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data

import kotlinx.serialization.Serializable



interface PageElement{
    fun toPlainText(): String
}

@Serializable
data class Section(
    val id: Int = 0,
    val elements: List<PageElement> = emptyList()
): PageElement {
    override fun toPlainText(): String  = elements.joinToString("\n")
}

@Serializable
data class Paragraph(val text: String): PageElement{
    override fun toPlainText(): String = text
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