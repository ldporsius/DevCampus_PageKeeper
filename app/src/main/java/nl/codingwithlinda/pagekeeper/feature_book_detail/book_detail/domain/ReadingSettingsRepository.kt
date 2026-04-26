package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain

import kotlinx.coroutines.flow.Flow
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingOrientation

interface ReadingSettingsRepository {
    val settings: Flow<ReadingSettings>
    suspend fun setOrientation(orientation: ReadingOrientation)
    suspend fun setFontSize(fontSize: Float)
}