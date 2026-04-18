package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Label
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn( ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FontSizeSlider(
    modifier: Modifier = Modifier,
    currentFontSize: Float = 1f,
    onSizeChange: (Float) -> Unit
) {
    val sliderState = rememberSliderState(
        value = currentFontSize,
        valueRange = .1f..5f)
    val interactionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isDragged, isPressed) {
        println("isDragged: $isDragged, isPressed: $isPressed, value: ${sliderState.value}")
        if (!isDragged && !isPressed) {
            onSizeChange(sliderState.value)
        }
    }
    Column(modifier = modifier.padding(horizontal = 16.dp)) {

        Slider(
            state = sliderState,
            interactionSource = interactionSource,
            thumb = {
                Label(
                    label = {
                        PlainTooltip(
                            modifier = Modifier
                                .sizeIn(minWidth = 45.dp, minHeight = 25.dp)
                                .wrapContentWidth()
                        ) {
                            Text(sliderState.value.toInt().toString())
                        }
                    },
                    interactionSource = interactionSource
                ) {
                    // This is the actual thumb
                    SliderDefaults.Thumb(
                        interactionSource = interactionSource
                    )
                }
            }
        )
    }
}

@Preview
@Composable
private fun FontSizeSliderPreview() {
    FontSizeSlider(
        modifier = Modifier,
        onSizeChange = {}
    )
}