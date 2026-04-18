package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.ui.theme.PageKeeperTheme
import nl.codingwithlinda.pagekeeper.core.presentation.util.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.core.presentation.util.UiText
import nl.codingwithlinda.pagekeeper.core.presentation.util.asString
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.FormattedLine
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Page
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.ReadingSettings
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.ProvideReadingTextStyle
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.sliderValueToActualSp
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.TextSpan
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.navigation.BookDetailEvent
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailAction
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailState
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.ReadingMode
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingControls
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingControlsViewModel
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingOrientation
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.ReadingControlAction
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

@Composable
fun BookDetailRoot(
    isbn: String,
    onNavigateBack: () -> Unit,
    viewModel: BookDetailViewModel = koinViewModel(key = isbn) { parametersOf(isbn) },
    readingControlsViewModel: ReadingControlsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val readingSettings by readingControlsViewModel.state.collectAsStateWithLifecycle()

    val activity = LocalActivity.current

    activity?.let{
        it.requestedOrientation = when(readingSettings.orientation){
            ReadingOrientation.AUTO_ROTATE -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            ReadingOrientation.LOCKED_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is BookDetailEvent.NavigateBack -> onNavigateBack()
        }
    }

    @Composable
    fun content() = ProvideReadingTextStyle(rawSliderValue = readingSettings.fontSize) {
        BookDetailScreen(
            state = state,
            onAction = viewModel::onAction,
            modifier = Modifier.pointerInput(true) {
                detectTapGestures(
                    onTap = { viewModel.onAction(BookDetailAction.ToggleReadingMode) }
                )
            }
        )
    }

    when(state.readingMode){
        ReadingMode.IMMERSIVE -> {
            content()
        }
        ReadingMode.CONTROLS -> {
            state.book?.let { book ->
                BookDetailScaffold(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    readingSettings = readingSettings,
                    onAction = readingControlsViewModel::onAction,
                    content = {
                        content()
                    }
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScaffold(
    modifier: Modifier = Modifier,
    state: BookDetailState,
    readingSettings: ReadingSettings,
    onAction: (ReadingControlAction) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    state.book?.title ?: ""
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = "back_to_library"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.favorites),
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
        var indicatorNaturalBounds by remember { mutableStateOf(Rect.Zero) }

        Column(modifier = Modifier.padding(innerPadding)) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                if (showAdjustFontSize) {
                    // onGloballyPositioned before offset → reports natural (pre-offset) bounds.
                    // offset then moves the indicator so its center-x aligns with the thumb
                    // center-x and its bottom sits just above the thumb top.
                    // The 4dp gap is the only intentional design constant here.
                    Box(
                        modifier = Modifier
                            .alpha(if (indicatorNaturalBounds == Rect.Zero) 0f else 1f)
                            .onGloballyPositioned { indicatorNaturalBounds = it.boundsInRoot() }
                            .offset {
                                if (thumbBounds == Rect.Zero || indicatorNaturalBounds == Rect.Zero)
                                    return@offset IntOffset.Zero
                                val dx = thumbBounds.center.x - indicatorNaturalBounds.center.x
                                val dy = thumbBounds.top - indicatorNaturalBounds.bottom - 4.dp.toPx()
                                IntOffset(dx.roundToInt(), dy.roundToInt())
                            }
                            .shadow(elevation = 3.dp, shape = CircleShape)
                            .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val baseSp = MaterialTheme.typography.bodyMedium.fontSize.value
                        val actualSp = sliderValueToActualSp(readingSettings.fontSize, baseSp)
                        Text("${actualSp.roundToInt()}")
                    }
                }

                ReadingControls(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    readingSettings = readingSettings,
                    showAdjustFontSize = showAdjustFontSize,
                    toggleAdjustFontSize = { showAdjustFontSize = true },
                    onAction = onAction,
                    onThumbPositioned = { thumbBounds = it }
                )
            }
        }
    }
}

@Composable
fun BookDetailScreen(
    state: BookDetailState,
    onAction: (BookDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier
        .fillMaxSize()
        .safeContentPadding()
        .testTag("book_detail_screen")){
        if (state.isWriting) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        state.error?.let { error ->
            BookParseErrorContent(
                error = error,
                modifier = Modifier.align(Alignment.Center)
            )
            return@Box
        }

        val listState = rememberLazyListState()
        val nearBottom by remember {
            derivedStateOf {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
                val total = listState.layoutInfo.totalItemsCount
                total > 0 && lastVisible >= total - 2
            }
        }
        LaunchedEffect(nearBottom, state.isLoading) {
            if (nearBottom && !state.isLoading) onAction(BookDetailAction.LoadNextSection)
        }

        LazyColumn(state = listState) {
            items(
                state.pages,
                key = { page -> page.hashCode() }

            ){ page ->
                when (page) {
                    is Page.TextPage -> {
                        Column() {
                            page.lines.forEach { line ->
                                Text(text = buildAnnotatedString {
                                    line.spans.forEach { span ->
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
                                            else -> append(span.text)
                                        }
                                    }
                                })
                            }
                        }
                    }
                    is Page.ImagePage -> {
                        val img = page.href
                        AsyncImage(
                            model = img,
                            contentDescription = null
                        )
                    }
                }
            }
            if (state.isLoading){
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ){
                        CircularProgressIndicator()
                    }

                }
            }
        }
    }
}

@Composable
private fun BookParseErrorContent(
    error: UiText,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.emoticon_sad_outline),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error.asString(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BookDetailScreenPreview() {
    PageKeeperTheme {
        BookDetailScreen(
            state = BookDetailState(
                pages = listOf(
                    Page.TextPage(
                        lines = listOf(
                            FormattedLine(
                                spans = listOf(
                                    TextSpan(text = "The Great Gatsby", bold = true)
                                )
                            ),
                            FormattedLine(
                                spans = listOf(
                                    TextSpan(text = "by ", emphasis = true),
                                    TextSpan(text = "F. Scott Fitzgerald")
                                )
                            )
                        )
                    ),
                    Page.TextPage(
                        lines = listOf(
                            FormattedLine(
                                spans = listOf(
                                    TextSpan(text = "In my younger and more vulnerable years my father gave me some advice that I've been turning over in my mind ever since.")
                                )
                            )
                        )
                    )
                ),
                isLoading = false
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BookParseErrorContentPreview() {
    PageKeeperTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            BookParseErrorContent(
                error = UiText.DynamicString("Could not parse book content. Please try again later.")
            )
        }
    }
}