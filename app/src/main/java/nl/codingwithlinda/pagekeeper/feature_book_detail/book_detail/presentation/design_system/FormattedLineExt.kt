package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.ElementTextSpan
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.FormattedLine

fun FormattedLine.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    spans.forEach { span ->
        when {
            span.url != null -> withLink(LinkAnnotation.Url(span.url)) { append(span.text) }
            span.emphasis    -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(span.text) }
            span.bold        -> withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) { append(span.text) }
            else             -> append(span.text)
        }
    }
}

fun ElementTextSpan.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    lines.forEach { append(it.toAnnotatedString()) }
}