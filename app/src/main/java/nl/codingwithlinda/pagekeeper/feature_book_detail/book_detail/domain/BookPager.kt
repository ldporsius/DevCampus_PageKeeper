package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain

import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data.Section

interface BookPager {

    suspend fun writePages(uri: String, book: Book)

    suspend fun readPages(book: Book): List<Section>

    suspend fun loadPages(book: Book, sectionIndex: Int = 0): List<Section>
}