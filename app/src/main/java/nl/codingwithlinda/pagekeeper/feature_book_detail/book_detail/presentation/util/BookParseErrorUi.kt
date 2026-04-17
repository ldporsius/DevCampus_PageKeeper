package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.util

import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.core.presentation.util.UiText
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookParseError

fun BookParseError.toUi(): UiText {
    return when (this) {
        BookParseError.GeneralBookParseError -> UiText.StringResource(R.string.error_book_parse_general)
        BookParseError.NoPagesFound -> UiText.StringResource(R.string.error_book_parse_no_pages)
        BookParseError.OOM -> UiText.StringResource(R.string.error_book_parse_oom)
    }
}