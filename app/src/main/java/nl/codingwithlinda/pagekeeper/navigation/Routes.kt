package nl.codingwithlinda.pagekeeper.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object BookListRoute: NavKey

@Serializable
data class BookDetailRoute(val ISBN: String): NavKey

