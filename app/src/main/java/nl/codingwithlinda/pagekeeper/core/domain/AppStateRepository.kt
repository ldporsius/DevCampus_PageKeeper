package nl.codingwithlinda.pagekeeper.core.domain

import kotlinx.coroutines.flow.Flow

interface AppStateRepository {
    val lastOpenedBookIsbn: Flow<String?>
    suspend fun setLastOpenedBook(isbn: String)
}