package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.ui.theme.Typography
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Chapter
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Citation
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Epigraph
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.PageElement
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Paragraph
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Section
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Title

// Design-intent bounds for the reading font size range.
// These express what the smallest and largest readable text should be,
// without assuming anything about the base font size.
private const val MIN_FONT_SP = 10f
private const val MAX_FONT_SP = 40f

// Must match the half-range of the slider in FontSizeSlider (-N..N).
private const val SLIDER_HALF_RANGE = 0f


fun sliderValueRange(baseSp: Float): ClosedFloatingPointRange<Float> =
    MIN_FONT_SP / baseSp..MAX_FONT_SP / baseSp

/**
 * Provides a scaled [LocalTextStyle] to all composables in [content].
 *
 * [rawSliderValue] is the raw value from the centered slider (-N..0..+N).
 * Zero means no change. The actual font size in sp is never assumed —
 * min/max scales are derived from [MaterialTheme.typography.bodyMedium]
 * at composition time, so they adapt to any theme or device configuration.
 */
fun sliderValueToScale(rawSliderValue: Float, baseSp: Float): Float {
    val minScale = MIN_FONT_SP / baseSp
    val maxScale = MAX_FONT_SP / baseSp
    /*return if (rawSliderValue <= 0f) {
        1f + rawSliderValue * ((1f - minScale) / SLIDER_HALF_RANGE)
    } else {
        1f + rawSliderValue * ((maxScale - 1f) / SLIDER_HALF_RANGE)
    }*/

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
    is Chapter -> Typography.bodyMedium
    is Citation -> Typography.bodyMedium
    is Epigraph -> Typography.bodyMedium
    is Paragraph -> Typography.bodyMedium
    is Section -> Typography.bodyMedium
    is Title -> Typography.titleLarge
}

@Composable
fun PageElement.toScaledTextStyle(): androidx.compose.ui.text.TextStyle {
    val base = LocalTextStyle.current
    val bodyBaseSp = LocalDefaultTextStyle.current.fontSize.value
    val ratio = toTextStyle().fontSize.value / bodyBaseSp
    return base.copy(fontSize = base.fontSize * ratio)
}


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
}