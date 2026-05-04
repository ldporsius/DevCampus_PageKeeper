package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.ui.Alignment
import kotlin.math.roundToInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.util.rememberDeviceConfig
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.ReadingSettings
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.toReadingControls
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.typographySliderRange
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailState
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ControlsRow
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.FontSizeSlider
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingControlsAdaptive
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.AutoRotateControl
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.FontSizeControl
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.ReadingControl
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.ReadingControlAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScaffold(
    modifier: Modifier = Modifier,
    state: BookDetailState,
    readingSettings: ReadingSettings,
    onAction: (ReadingControlAction) -> Unit,
    onNavBack: () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (state.book == null) return
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(state.book.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavBack()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = "back_to_library"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onAction(ReadingControlAction.ToggleFavorite(state.book.isbn ))

                    }) {
                        val icon = if (state.book.isFavorite) R.drawable.menu_favorites_active else R.drawable.favorites
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = "mark_favorite"
                        )
                    }
                }
            )
        },

        ) {innerPadding ->

        var showAdjustFontSize by rememberSaveable(state.readingMode) {
            mutableStateOf(false)
        }
        var thumbBounds by remember { mutableStateOf(Rect.Zero) }
        var trackBounds by remember { mutableStateOf(Rect.Zero) }

        val valueRange = typographySliderRange()

        val sliderState = rememberSliderState(
            value = readingSettings.fontSize,
            valueRange = valueRange,
        )

        LaunchedEffect(readingSettings.fontSize) {
            sliderState.value = readingSettings.fontSize
        }

        Column(modifier = Modifier
            .safeContentPadding()
            .consumeWindowInsets(innerPadding)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {

                val device = rememberDeviceConfig()
                val listControls = device.deviceType.toReadingControls().map {
                    when(it){
                        ReadingControl.AUTO_ROTATE -> {
                            AutoRotateControl(
                                setting = readingSettings.orientation,
                                onAction = { onAction(ReadingControlAction.ToggleAutoRotate) }
                            )
                        }
                        ReadingControl.FONT_SIZE -> {
                            FontSizeControl(
                                onAction = { showAdjustFontSize = true }
                            )
                        }
                    }
                }
                if (showAdjustFontSize) {
                    FontSizeIndicator(
                        fontSize = readingSettings.fontSize,
                        thumbBounds = thumbBounds,
                        trackBounds = trackBounds,
                        valueRange = valueRange,
                        sliderState = sliderState,
                        onAction = onAction,
                    )
                }

                ReadingControlsAdaptive(
                    collapsed = !showAdjustFontSize,
                    contentCollapsed = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${(state.readingProgress * 100).roundToInt()}%",
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 8.dp),
                                style = MaterialTheme.typography.labelMedium
                            )
                            LinearProgressIndicator(
                                progress = { state.readingProgress },
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                            )
                            ControlsRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                items = listControls
                            )
                        }
                    },
                    contentExpanded = {
                        FontSizeSlider(
                            modifier = Modifier,
                            sliderState = sliderState,
                            currentFontSize = readingSettings.fontSize,
                            valueRange = valueRange,
                            onSizeChange = { onAction(ReadingControlAction.AdjustFontSize(it)) },
                            onThumbPositioned = { thumbBounds = it },
                            onTrackPositioned = { trackBounds = it }
                        )
                    }

                )
            }
        }
    }
}
