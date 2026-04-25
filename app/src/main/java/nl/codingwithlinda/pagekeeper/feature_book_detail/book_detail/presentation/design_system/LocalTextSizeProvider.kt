package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.ui.theme.Typography
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Citation
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Epigraph
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.PageElement
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Paragraph
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Section
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Title
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.Page

// Design-intent bounds for the reading font size range.
// These express what the smallest and largest readable text should be,
// without assuming anything about the base font size.
private const val MIN_FONT_SP = 10f
private const val MAX_FONT_SP = 40f

fun sliderValueToScale(rawSliderValue: Float, baseSp: Float): Float {
    val minScale = MIN_FONT_SP / baseSp
    val maxScale = MAX_FONT_SP / baseSp

    return rawSliderValue.coerceIn(minScale, maxScale)
}

fun sliderValueToActualSp(rawSliderValue: Float, baseSp: Float): Float =
    baseSp * sliderValueToScale(rawSliderValue, baseSp)


val LocalDefaultTextStyle = staticCompositionLocalOf {
    Typography.bodyMedium
}

fun typographySliderRange(): ClosedFloatingPointRange<Float> {
    val bodyBaseSp = Typography.bodyMedium.fontSize.value
    return Typography.bodySmall.fontSize.value / bodyBaseSp..Typography.headlineMedium.fontSize.value / bodyBaseSp
}


fun PageElement.toTextStyle() = when(this){
    is Section -> Typography.bodyMedium
    is Title -> Typography.titleLarge
    is Citation -> Typography.bodyMedium
    is Epigraph -> Typography.bodyMedium
    is Paragraph -> Typography.bodyMedium
}


/*
@Composable
fun ProvideReadingTextStyle(
    rawSliderValue: Float,
    content: @Composable () -> Unit
) {
    val baseStyle = LocalDefaultTextStyle.current
    val baseSp = baseStyle.fontSize.value
    val scale = sliderValueToScale(rawSliderValue, baseSp)

    val scaledStyle = baseStyle.copy(
        fontSize = baseStyle.fontSize * scale,
        lineHeight = if (baseStyle.lineHeight.isSpecified) baseStyle.lineHeight * scale
        else TextUnit.Unspecified
    )

    CompositionLocalProvider(LocalTextStyle provides scaledStyle) {
        content()
    }
}*/

@Composable
fun Page.ElementPage.toScaledText(
    rawSliderValue: Float,
) {

    this.elements.forEach {element ->
        val el = element.element
        val baseStyle = el.toTextStyle()

        val scaledStyle = baseStyle.copy(
            fontSize = baseStyle.fontSize * rawSliderValue,
            lineHeight = if (baseStyle.lineHeight.isSpecified) baseStyle.lineHeight * rawSliderValue
            else TextUnit.Unspecified
        )

        CompositionLocalProvider(LocalTextStyle provides scaledStyle) {
            element.lines.forEach { line ->
                line.spans.forEach {  span ->
                    Text(
                        text = buildAnnotatedString {
                            when {
                                span.url != null -> withLink(LinkAnnotation.Url(span.url)) {
                                    append(span.text)
                                }
                                span.emphasis -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                    append(span.text)
                                }
                                span.bold -> withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                                    append(span.text)
                                }
                                else -> {
                                    append(span.text)
                                }
                            }
                        },

                        )
                }

            }
        }
    }

}