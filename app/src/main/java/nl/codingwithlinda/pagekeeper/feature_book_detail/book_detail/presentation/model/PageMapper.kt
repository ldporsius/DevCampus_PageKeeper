package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model

import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.PageElement
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Section

suspend fun Section.toPage(): Page =
    Page.ElementPage(elements = elements.map { it.toElementTextSpan() })

suspend fun PageElement.toElementTextSpan() =
    ElementTextSpan(
        element = this,
        lines = listOf(parseSpans(toPlainText()))
    )