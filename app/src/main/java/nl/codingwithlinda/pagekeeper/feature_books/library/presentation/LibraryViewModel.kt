package nl.codingwithlinda.pagekeeper.feature_books.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository

class LibraryViewModel(
    private val bookRepository: BookRepository
): ViewModel() {

    val books = bookRepository.books
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

}