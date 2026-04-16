package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain

import nl.codingwithlinda.pagekeeper.core.domain.model.Book

interface BookPager {

    suspend fun writePages(uri: String, book: Book)

    suspend fun readPages(book: Book): List<Page>

    suspend fun loadPages(book: Book): List<Page>
}