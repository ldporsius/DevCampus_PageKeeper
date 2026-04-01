package nl.codingwithlinda.pagekeeper.core.di

import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository

interface IAppModule {
    val bookRepository: BookRepository
}