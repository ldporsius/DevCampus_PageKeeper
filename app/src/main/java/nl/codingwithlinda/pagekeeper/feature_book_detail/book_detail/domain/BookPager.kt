package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain

import kotlinx.coroutines.flow.Flow
import nl.codingwithlinda.pagekeeper.core.domain.util.Result

interface BookPager {

    suspend fun writePages(uri: String, isbn: String, onProgress: suspend (written: Int, total: Int) -> Unit = { _, _ -> }): Result<Unit, BookParseError>

    suspend fun loadSections(isbn: String, sectionIndex: Int = 0): Result<List<Section>, BookParseError>
    suspend fun hasPages(isbn: String): Boolean
    suspend fun hasElementMeta(isbn: String): Boolean
    suspend fun countPages(isbn: String): Int
    suspend fun countElements(isbn: String): Int
    suspend fun loadSection(isbn: String, sectionIndex: Int): Flow<Section>

}
