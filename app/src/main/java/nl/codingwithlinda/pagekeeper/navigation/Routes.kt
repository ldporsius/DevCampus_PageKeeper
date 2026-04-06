package nl.codingwithlinda.pagekeeper.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter

sealed interface Destination
@Serializable
data object BookListRoute: NavKey, Destination

@Serializable
data class BookDetailRoute(val ISBN: String): NavKey, Destination

@Serializable
data object FavoritesRoute: NavKey, Destination

@Serializable
data object FinishedRoute: NavKey, Destination

@Serializable
data class SearchRoute(val filter: BookFilter): NavKey, Destination

@Serializable
data class MultiSelectRoute(val filter: BookFilter): NavKey, Destination

