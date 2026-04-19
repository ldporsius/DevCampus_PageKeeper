package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data

import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.PageElement
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Section

class PageBuilder {

    private val _sections = mutableMapOf<Int, Section>()
    val sections
        get() = _sections.toMap()

    fun addElementToSection(section: Section, element: PageElement): PageBuilder {
        val currentOrNew = _sections.getOrDefault(section.id, null) ?: section
        val update = currentOrNew.copy(elements = currentOrNew.elements.plus(element))
        _sections[update.id] = update
        return this
    }

    fun clear() {
        _sections.clear()
    }
}