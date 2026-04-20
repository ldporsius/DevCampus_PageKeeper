package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model

import kotlinx.serialization.Serializable
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.PageElement

interface ITextSpan {
    val text: String
}

@Serializable
data class TextSpan(
    override val text: String,
    val emphasis: Boolean = false,
    val bold: Boolean = false,
    val url: String? = null
) : ITextSpan

@Serializable
data class FormattedLine(val spans: List<TextSpan>)

@Serializable
data class ElementTextSpan(
    val element: PageElement,
    val lines: List<FormattedLine> = emptyList()
)

@Serializable
sealed interface Page {
    @Serializable
    data class ElementPage(val elements: List<ElementTextSpan>) : Page
    @Serializable
    data class ImagePage(val href: String) : Page
}