package nl.codingwithlinda.pagekeeper.feature_books.favorites.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import nl.codingwithlinda.pagekeeper.core.domain.local_cache.BookRepository
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.toBookUi
import nl.codingwithlinda.pagekeeper.feature_books.favorites.presentation.interaction.FavoritesState

class FavoritesViewModel(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state = _state.asStateFlow()

    init {
        bookRepository.books
            .onEach { books ->
                _state.update {
                    it.copy(books = books.filter { b -> b.isFavorite }.map { b -> b.toBookUi() })
                }
            }
            .launchIn(viewModelScope)
    }
}