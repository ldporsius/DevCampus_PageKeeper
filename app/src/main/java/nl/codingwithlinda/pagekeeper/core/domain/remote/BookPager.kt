package nl.codingwithlinda.pagekeeper.core.domain.remote

import nl.codingwithlinda.pagekeeper.core.domain.model.Book

interface BookPager {

    suspend fun writePages(uri: String, book: Book)

    suspend fun readPages(book: Book): List<Page>

    suspend fun loadPages(book: Book): List<Page>
}