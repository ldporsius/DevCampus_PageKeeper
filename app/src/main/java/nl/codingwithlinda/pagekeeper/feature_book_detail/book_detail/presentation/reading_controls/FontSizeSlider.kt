package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.ui.theme.PageKeeperTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FontSizeSlider(
    modifier: Modifier = Modifier,
    sliderState: SliderState,
    currentFontSize: Float = 1f,
    valueRange: ClosedFloatingPointRange<Float> = 1f..3f,
    onSizeChange: (Float) -> Unit,
    onThumbPositioned: (Rect) -> Unit = {},
    onTrackPositioned: (Rect) -> Unit = {}
) {

    val interactionSource = remember { MutableInteractionSource() }

    val isDragged by interactionSource.collectIsDraggedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(currentFontSize) {
        sliderState.value = currentFontSize
    }

    LaunchedEffect(isDragged, isPressed) {
        if (!isDragged && !isPressed) {
            onSizeChange(sliderState.value)
        }
    }

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepButton(
            symbol = "−",
            onClick = { onSizeChange((sliderState.value - 0.05f).coerceAtLeast(valueRange.start)) }
        )

        Slider(
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned { onTrackPositioned(it.boundsInRoot()) },
            state = sliderState,
            interactionSource = interactionSource,
            thumb = {
                Box(modifier = Modifier.onGloballyPositioned {
                    onThumbPositioned(it.boundsInRoot())
                }) {
                    SliderDefaults.Thumb(interactionSource = interactionSource)
                }
            },
            track = {
                SliderDefaults.CenteredTrack(
                    sliderState = sliderState,
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                )
            }
        )

        StepButton(
            symbol = "+",
            onClick = { onSizeChange((sliderState.value + 0.05f).coerceAtMost(valueRange.endInclusive)) }
        )
    }
}

@Composable
private fun StepButton(
    symbol: String,
    onClick: () -> Unit
) {
    // 48dp touch target wrapping a 32dp outlined circle
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                ,
            contentAlignment = Alignment.Center
        ) {
            Text(symbol,
                fontSize = 16.sp,
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onPrimary
                )
        }
    }
}

@Preview
@Composable
private fun FontSizeSliderPreview() {
    PageKeeperTheme() {
        FontSizeSlider(
            currentFontSize = 1f,
            sliderState = SliderState(),
            onSizeChange = {})
    }
}