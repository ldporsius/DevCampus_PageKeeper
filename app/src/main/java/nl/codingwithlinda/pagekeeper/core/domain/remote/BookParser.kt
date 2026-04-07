package nl.codingwithlinda.pagekeeper.core.domain.remote

import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.util.BookImportError
import nl.codingwithlinda.pagekeeper.core.domain.util.Result

interface BookParser {
    suspend fun fetch(uri: String): Result<Book, BookImportError>
}