package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain

import kotlinx.coroutines.flow.Flow
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.util.Result

interface BookPager {

    suspend fun writePages(uri: String, book: Book, onProgress: suspend (written: Int, total: Int) -> Unit = { _, _ -> }): Result<Unit, BookParseError>

    suspend fun loadPages(book: Book, sectionIndex: Int = 0): Result<List<Section>, BookParseError>
}

interface LazyBookPager: BookPager{
    suspend fun hasPages(book: Book): Boolean
    suspend fun countPages(book: Book): Int
    suspend fun loadChapter(book: Book, sectionIndex: Int): Flow<Section>
}