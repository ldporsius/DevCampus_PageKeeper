package nl.codingwithlinda.pagekeeper.core.domain

import kotlinx.coroutines.CompletableDeferred
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookParser

class FakeBookParser : BookParser {
    private val deferred = CompletableDeferred<Book?>()

    override suspend fun fetch(uri: String): Book? = deferred.await()

    fun complete(book: Book?) = deferred.complete(book)
}