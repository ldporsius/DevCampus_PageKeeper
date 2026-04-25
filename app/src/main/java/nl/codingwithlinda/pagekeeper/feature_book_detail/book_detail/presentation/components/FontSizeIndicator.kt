package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.LocalDefaultTextStyle
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.sliderValueToActualSp
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.ReadingControlAction
import kotlin.math.roundToInt

@Composable
fun FontSizeIndicator(
    fontSize: Float,
    thumbBounds: Rect,
    trackBounds: Rect,
    valueRange: ClosedFloatingPointRange<Float>,
    sliderState: SliderState,
    onAction: (ReadingControlAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var indicatorNaturalBounds by remember { mutableStateOf(Rect.Zero) }

    // onGloballyPositioned before offset → reports natural (pre-offset) bounds.
    // offset then moves the indicator so its center-x aligns with the thumb
    // center-x and its bottom sits just above the thumb top.
    // The 4dp gap is the only intentional design constant here.
    Box(
        modifier = modifier
            .alpha(if (indicatorNaturalBounds == Rect.Zero) 0f else 1f)
            .onGloballyPositioned { indicatorNaturalBounds = it.boundsInRoot() }
            .offset {
                if (thumbBounds == Rect.Zero || indicatorNaturalBounds == Rect.Zero)
                    return@offset IntOffset.Zero
                val dx = thumbBounds.center.x - indicatorNaturalBounds.center.x
                val dy = thumbBounds.top - indicatorNaturalBounds.bottom - 4.dp.toPx()
                IntOffset(dx.roundToInt(), dy.roundToInt())
            }
            .pointerInput(true) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        val rangeSize = valueRange.endInclusive - valueRange.start
                        val valueDelta = dragAmount / trackBounds.width * rangeSize
                        val newFontSize = (sliderState.value + valueDelta)
                            .coerceIn(valueRange.start, valueRange.endInclusive)
                        sliderState.value = newFontSize
                    },
                    onDragEnd = {
                        onAction(ReadingControlAction.AdjustFontSize(sliderState.value))
                    }
                )
            }
            .shadow(elevation = 3.dp, shape = CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, shape = CircleShape)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        val baseSp = LocalDefaultTextStyle.current.fontSize.value
        val actualSp = sliderValueToActualSp(fontSize, baseSp)
        Text("${actualSp.roundToInt()}")
    }
}