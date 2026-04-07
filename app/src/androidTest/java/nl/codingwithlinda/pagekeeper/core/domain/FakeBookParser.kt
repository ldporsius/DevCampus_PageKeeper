package nl.codingwithlinda.pagekeeper.core.domain

import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookParser
import nl.codingwithlinda.pagekeeper.core.domain.util.BookImportError
import nl.codingwithlinda.pagekeeper.core.domain.util.Result

class FakeBookParser : BookParser {
    override suspend fun fetch(uri: String): Result<Book, BookImportError> =
        Result.Failure(BookImportError.BookImportOtherError)
}
