package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain

import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingOrientation

data class ReadingSettings(
    val orientation: ReadingOrientation = ReadingOrientation.AUTO_ROTATE,
    val fontSize: Float = 1f,
)