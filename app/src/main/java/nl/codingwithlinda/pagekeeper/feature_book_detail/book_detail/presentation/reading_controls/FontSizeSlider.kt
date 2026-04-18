package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSizeSlider(
    modifier: Modifier = Modifier,
    currentFontSize: Float = 1f,
    onSizeChange: (Float) -> Unit
) {
    val sliderState = rememberSliderState(
        value = currentFontSize,
        valueRange = .1f..5f
    )
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
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onSizeChange((sliderState.value - 0.01f).coerceAtLeast(0.1f)) }) {
            Text("−", fontSize = 16.sp)
        }

        Slider(
            modifier = Modifier.weight(1f),
            state = sliderState,
            interactionSource = interactionSource,
            thumb = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "%.2f".format(sliderState.value),
                        style = MaterialTheme.typography.labelSmall
                    )
                    SliderDefaults.Thumb(interactionSource = interactionSource)
                }
            }
        )

        IconButton(onClick = { onSizeChange((sliderState.value + 0.01f).coerceAtMost(5f)) }) {
            Text("+", fontSize = 16.sp)
        }
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