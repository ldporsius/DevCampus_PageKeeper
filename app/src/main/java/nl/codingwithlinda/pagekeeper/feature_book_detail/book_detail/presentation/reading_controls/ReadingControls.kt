package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.ReadingSettings
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.ReadingControlAction

@Composable
fun ReadingControls(
    modifier: Modifier = Modifier,
    onAction: (ReadingControlAction) -> Unit,
    readingSettings: ReadingSettings,
    showAdjustFontSize: Boolean,
    toggleAdjustFontSize: () -> Unit,
    onThumbPositioned: (Rect) -> Unit = {}
) {

    AnimatedContent(showAdjustFontSize) { show ->
        when(show){
            true -> {
                Row(modifier = modifier,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    FontSizeSlider(
                        modifier = Modifier,
                        currentFontSize = readingSettings.fontSize,
                        onSizeChange = { onAction(ReadingControlAction.AdjustFontSize(it)) },
                        onThumbPositioned = onThumbPositioned
                    )
                }
            }
            false -> {
                ControlsRow(
                    modifier = modifier,
                    readingOrientation = readingSettings.orientation,
                    onAction = onAction,
                    showAdjustFontSize = {
                        toggleAdjustFontSize()
                    }
                )
            }
        }
    }

}

@Composable
fun ControlsRow(
    modifier: Modifier = Modifier,
    readingOrientation: ReadingOrientation,
    onAction: (ReadingControlAction) -> Unit,
    showAdjustFontSize: ()-> Unit
) {
    Row(modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        ReadingControlItem(
            modifier = Modifier
                .clickable(){
                    onAction(ReadingControlAction.ToggleAutoRotate)
                }
            ,
            icon = readingOrientation.toUi().icon,
            contentDescription = "",
            text = readingOrientation.toUi().text
        )

        ReadingControlItem(
            modifier = Modifier.clickable(){
                showAdjustFontSize()
            },
            icon = R.drawable.font_size,
            contentDescription = "adjust_font_size",
            text = "Font Size"
        )
    }
}

@Composable
fun ReadingControlItem(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    contentDescription: String,
    text: String,
) {

    Column(modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription
        )
        Text(text)

    }
}

@Preview
@Composable
private fun ReadingControlsPreview() {
    ReadingControls(
        modifier = Modifier.fillMaxWidth(),
        readingSettings = ReadingSettings(),
        showAdjustFontSize = false,
        toggleAdjustFontSize = {},
        onAction = {},
    )

}