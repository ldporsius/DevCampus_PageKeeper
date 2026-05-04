package nl.codingwithlinda.pagekeeper.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.codingwithlinda.pagekeeper.core.domain.AppStateRepository

class DataStoreAppStateRepository(
    private val dataStore: DataStore<Preferences>
) : AppStateRepository {

    override val lastOpenedBookIsbn: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_LAST_BOOK_ISBN]
    }

    override suspend fun setLastOpenedBook(isbn: String) {
        dataStore.edit { it[KEY_LAST_BOOK_ISBN] = isbn }
    }

    private companion object {
        val KEY_LAST_BOOK_ISBN = stringPreferencesKey("last_opened_book_isbn")
    }
}