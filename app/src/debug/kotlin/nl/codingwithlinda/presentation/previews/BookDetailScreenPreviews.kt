package nl.codingwithlinda.presentation.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.ui.theme.PageKeeperTheme
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Citation
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Paragraph
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.ReadingSettings
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Title
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.BookDetailRoot
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.BookDetailScaffold
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components.BookDetailScreen
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system.FormFactorWrapper
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailAction
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.interaction.BookDetailState
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.ElementTextSpan
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.FormattedLine
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.Page.ElementPage
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.model.TextSpan
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi

private val tomSawyerBook = BookUi(
    isbn = "9780743273565",
    title = "The Adventures of Tom Sawyer",
    author = "Mark Twain",
    imgUrl = "",
    formattedDate = "1 Jan 2024",
    isFavorite = false,
    isFinished = false,
)

private val tomSawyerPage = ElementPage(
    elements = listOf(
        ElementTextSpan(
            element = Title(text = "Chapter I — Y-O-U-T-H"),
            lines = listOf(FormattedLine(listOf(TextSpan(text = "Chapter I — Y-O-U-T-H"))))
        ),
        ElementTextSpan(
            element = Paragraph(
                text = "\"TOM!\" No answer. \"TOM!\" No answer. \"What's gone with that boy, I wonder? You TOM!\""
            ),
            lines = listOf(
                FormattedLine(listOf(
                    TextSpan(text = "\"TOM!\"", bold = true),
                    TextSpan(text = " No answer. "),
                    TextSpan(text = "\"TOM!\"", bold = true),
                    TextSpan(text = " No answer. "),
                    TextSpan(text = "\"What's gone with that boy, I wonder? You TOM!\"", emphasis = true),
                ))
            )
        ),
        ElementTextSpan(
            element = Paragraph(
                text = "The old lady pulled her spectacles down and looked over them about the room; then she put them up and looked out under them. She seldom or never looked through them for so small a thing as a boy."
            ),
            lines = listOf(
                FormattedLine(listOf(
                    TextSpan(text = "The old lady pulled her spectacles down and looked over them about the room; then she put them up and looked out under them. She seldom or never looked "),
                    TextSpan(text = "through", emphasis = true),
                    TextSpan(text = " them for so small a thing as a boy."),
                ))
            )
        ),
        ElementTextSpan(
            element = Citation(
                text = "There are lies, damned lies, and statistics. — Mark Twain"
            ),
            lines = listOf(
                FormattedLine(listOf(
                    TextSpan(text = "There are lies, damned lies, and statistics. — "),
                    TextSpan(text = "Mark Twain", bold = true),
                ))
            )
        ),
        ElementTextSpan(
            element = Paragraph(
                text = "Tom appeared on the sidewalk with a bucket of whitewash and a long-handled brush. He surveyed the fence, and all gladness left him and a deep melancholy settled down upon his spirit."
            ),
            lines = listOf(
                FormattedLine(listOf(
                    TextSpan(text = "Tom appeared on the sidewalk with a bucket of whitewash and a long-handled brush. He surveyed the fence, and all gladness left him and a "),
                    TextSpan(text = "deep melancholy", emphasis = true),
                    TextSpan(text = " settled down upon his spirit."),
                ))
            )
        ),
    )
)

private val previewState = BookDetailState(
    book = tomSawyerBook,
    pages = mapOf(tomSawyerPage.sectionId to tomSawyerPage),
    isLoading = false,
)

@Preview(name = "Phone — Portrait", device = Devices.PHONE, showBackground = true)
@Composable
private fun BookDetailPhonePortraitPreview() {
    PageKeeperTheme {
        BookDetailScaffold(
            state = previewState,
            readingSettings = ReadingSettings(),
            onAction = {},
        ) {
            FormFactorWrapper() {
                BookDetailScreen(
                    state = previewState,
                    readingSettings = ReadingSettings(),
                    onAction = {},
                )
            }
        }
    }
}

@Preview(name = "Phone — Landscape", device = "spec:width=891dp,height=411dp,dpi=420,orientation=landscape", showBackground = true)
@Composable
private fun BookDetailPhoneLandscapePreview() {
    PageKeeperTheme {

        BookDetailScaffold(
            state = previewState,
            readingSettings = ReadingSettings(),
            onAction = {},
        ) {
            FormFactorWrapper() {
                BookDetailScreen(
                    state = previewState,
                    readingSettings = ReadingSettings(),
                    onAction = {},
                )
            }
        }
    }
}

@Preview(name = "Tablet — Portrait", device = Devices.TABLET, showBackground = true)
@Composable
private fun BookDetailTabletPreview() {
    PageKeeperTheme {

            BookDetailScaffold(
                state = previewState,
                readingSettings = ReadingSettings(),
                onAction = {},
            ) {
                FormFactorWrapper() {

                BookDetailScreen(
                    state = previewState,
                    readingSettings = ReadingSettings(),
                    onAction = {},
                )
            }
        }
    }
}

@Preview(name = "Foldable — Unfolded", device = Devices.FOLDABLE, showBackground = true)
@Composable
private fun BookDetailFoldablePreview() {
    PageKeeperTheme {
        BookDetailScaffold(
            state = previewState,
            readingSettings = ReadingSettings(),
            onAction = {},
        ) {
            BookDetailScreen(
                state = previewState,
                readingSettings = ReadingSettings(),
                onAction = {},
            )
        }
    }
}