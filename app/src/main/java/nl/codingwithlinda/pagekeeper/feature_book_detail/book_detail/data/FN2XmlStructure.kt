package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data


interface PageElement
data class Section(
    val id: Int = 0,
    val elements: List<PageElement> = emptyList()): PageElement

data class Paragraph(val text: String): PageElement

data class Title(val text: String): PageElement

data class Citation(val text: String): PageElement

data class Epigraph(val text: String): PageElement

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
}