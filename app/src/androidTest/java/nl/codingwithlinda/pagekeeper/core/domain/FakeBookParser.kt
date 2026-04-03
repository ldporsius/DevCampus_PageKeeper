package nl.codingwithlinda.pagekeeper.core.domain

import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookParser

class FakeBookParser : BookParser {
    override suspend fun fetch(uri: String): Book? = null
}
