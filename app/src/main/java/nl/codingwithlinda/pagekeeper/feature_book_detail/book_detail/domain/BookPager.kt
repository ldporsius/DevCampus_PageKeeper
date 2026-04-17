package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain

import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.util.Result
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data.Section

interface BookPager {

    suspend fun writePages(uri: String, book: Book): Result<Unit, BookParseError>

    suspend fun readPages(book: Book): Result<List<Section>, BookParseError>

    suspend fun loadPages(book: Book, sectionIndex: Int = 0): Result<List<Section>, BookParseError>
}