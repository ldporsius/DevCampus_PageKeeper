package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.ReadingSettings
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.ReadingSettingsRepository
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingOrientation

class DataStoreReadingSettingsRepository(
    private val dataStore: DataStore<Preferences>
) : ReadingSettingsRepository {

    override val settings: Flow<ReadingSettings> = dataStore.data.map { prefs ->
        ReadingSettings(
            orientation = prefs[KEY_ORIENTATION]
                ?.let { runCatching { ReadingOrientation.valueOf(it) }.getOrNull() }
                ?: ReadingOrientation.AUTO_ROTATE,
            fontSize = prefs[KEY_FONT_SIZE] ?: 1f,
            currentSection = prefs[KEY_CURRENT_SECTION] ?: 0,
        )
    }

    override suspend fun setOrientation(orientation: ReadingOrientation) {
        dataStore.edit { it[KEY_ORIENTATION] = orientation.name }
    }

    override suspend fun setFontSize(fontSize: Float) {
        dataStore.edit { it[KEY_FONT_SIZE] = fontSize }
    }

    override suspend fun setCurrentSection(section: Int) {
        dataStore.edit { it[KEY_CURRENT_SECTION] = section }
    }

    private companion object {
        val KEY_ORIENTATION = stringPreferencesKey("reading_orientation")
        val KEY_FONT_SIZE = floatPreferencesKey("reading_font_size")
        val KEY_CURRENT_SECTION = intPreferencesKey("reading_current_section")
    }
}