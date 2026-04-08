package nl.codingwithlinda.pagekeeper.core.domain.remote

import kotlinx.serialization.Serializable

@Serializable
data class TextSpan(val text: String, val emphasis: Boolean = false)

@Serializable
data class FormattedLine(val spans: List<TextSpan>) {
    fun toPlainText(): String = spans.joinToString("") { it.text }
    fun isFullyEmphasized(): Boolean = spans.all { it.emphasis }
}

@Serializable
sealed interface Page {
    @Serializable
    data class TextPage(val lines: List<FormattedLine>) : Page
    @Serializable
    data class ImagePage(val href: String) : Page
}