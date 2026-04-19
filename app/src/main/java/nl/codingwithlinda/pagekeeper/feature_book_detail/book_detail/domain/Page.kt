package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain

import kotlinx.serialization.Serializable
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data.PageElement


interface ITextSpan{
    val text: String
}
@Serializable
data class ElementTextSpan(
    val element: PageElement,
    val lines: List<FormattedLine> = emptyList()
)

@Serializable
data class TextSpan(
    override val text: String,
    val emphasis: Boolean = false,
    val bold: Boolean = false,
    val url: String? = null): ITextSpan

@Serializable
data class FormattedLine(val spans: List<TextSpan>)

@Serializable
sealed interface Page {
    @Serializable
    data class ElementPage(val elements: List<ElementTextSpan>) : Page
    @Serializable
    data class TextPage(val lines: List<FormattedLine>) : Page
    @Serializable
    data class ImagePage(val href: String) : Page
}