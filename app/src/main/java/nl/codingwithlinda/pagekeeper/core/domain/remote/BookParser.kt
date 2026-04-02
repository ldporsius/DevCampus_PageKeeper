package nl.codingwithlinda.pagekeeper.core.domain.remote

import nl.codingwithlinda.pagekeeper.core.domain.model.Book

interface BookParser {
    suspend fun fetch(uri: String): Book?
}