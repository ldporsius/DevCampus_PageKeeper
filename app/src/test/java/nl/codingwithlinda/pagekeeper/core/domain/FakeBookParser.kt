package nl.codingwithlinda.pagekeeper.core.domain

import kotlinx.coroutines.CompletableDeferred
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookParser
import nl.codingwithlinda.pagekeeper.core.domain.util.BookImportError
import nl.codingwithlinda.pagekeeper.core.domain.util.Result

class FakeBookParser : BookParser {
    private val deferred = CompletableDeferred<Result<Book, BookImportError>>()

    override suspend fun fetch(uri: String): Result<Book, BookImportError> = deferred.await()

    fun complete(result: Result<Book, BookImportError>) = deferred.complete(result)
}