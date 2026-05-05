package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.SliderState
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
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.typographySliderRange
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.AutoRotateControl
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.ReadingControlAction
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.ReadingControlItem


@Composable
fun ReadingControlsAdaptive(
    collapsed: Boolean,
    contentCollapsed: @Composable () -> Unit,
    contentExpanded: @Composable () -> Unit,
) {
    AnimatedContent(targetState = collapsed) {
        when (it) {
            true -> contentCollapsed()
            false -> contentExpanded()
        }
    }
}
@Composable
fun ControlsRow(
    modifier: Modifier = Modifier,
    items: List<ReadingControlItem>,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        items.forEach { item ->
            ReadingControlComp(
                modifier = Modifier.clickable {
                    item.onAction()
                },
                icon = item.icon,
                contentDescription = item.contentDescription,
                text = item.text
            )
        }
    }
}

@Composable
private fun ReadingControlComp(
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
        Text(text,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall
        )

    }
}

@Preview
@Composable
private fun ReadingControlsPreview() {
    @Composable
    fun ReadingControlsCollapsed(
        modifier: Modifier = Modifier,
        readingSettings: ReadingSettings,
        onAction: (ReadingControlAction) -> Unit,
    ) {
        ControlsRow(
            modifier = modifier,
            items = listOf(
                AutoRotateControl(
                    setting = readingSettings.orientation,
                    onAction = { onAction(ReadingControlAction.ToggleAutoRotate) }
                )
            )
        )
    }
    @Composable
   fun ReadingControlsExpanded(
        modifier: Modifier = Modifier,
        sliderState: SliderState,
        onAction: (ReadingControlAction) -> Unit,
        readingSettings: ReadingSettings,
        onThumbPositioned: (Rect) -> Unit = {},
        onTrackPositioned: (Rect) -> Unit = {}
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            val range = typographySliderRange()
            FontSizeSlider(
                modifier = Modifier,
                sliderState = sliderState,
                currentFontSize = readingSettings.fontSize,
                valueRange = range,
                onSizeChange = { onAction(ReadingControlAction.AdjustFontSize(it)) },
                onThumbPositioned = onThumbPositioned,
                onTrackPositioned = onTrackPositioned
            )
        }
    }
    ReadingControlsAdaptive(
       collapsed = true,
        contentCollapsed = {
            ReadingControlsCollapsed(
                modifier = Modifier.fillMaxWidth(),
                readingSettings = ReadingSettings(),
                onAction = {}
            )
        },
        contentExpanded = {
            ReadingControlsExpanded(
                modifier = Modifier.fillMaxWidth(),
                sliderState = SliderState(),
                readingSettings = ReadingSettings(),
                onAction = {}
            )
        }
    )

}