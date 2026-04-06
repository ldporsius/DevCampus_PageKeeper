package nl.codingwithlinda.pagekeeper.feature_books.search.width_compact

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.design_system.components.AppNavigation
import nl.codingwithlinda.pagekeeper.design_system.util.Orientation
import nl.codingwithlinda.pagekeeper.design_system.util.rememberDeviceConfig
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListSideEffects
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.search.SearchViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

@Composable
fun SearchRoot(
    filter: BookFilter,
    onBack: () -> Unit,
    searchViewModel: SearchViewModel = koinViewModel(parameters = { parametersOf(filter) }),
    bookListViewModel: BookListViewModel = koinViewModel(qualifier = named("search"), parameters = { parametersOf(filter)}),
) {
    val state by searchViewModel.state.collectAsStateWithLifecycle()

    BookListSideEffects(bookListViewModel)

    val deviceConfig = rememberDeviceConfig()

    when(deviceConfig.orientation){
        Orientation.Portrait -> {
            SearchScaffold(
                query = state.query,
                onQueryChange = searchViewModel::onQueryChange,
                onBack = onBack
            ) {
                SearchContent(query = state.query, books = state.books)
            }
        }
        Orientation.Landscape -> {
            AppNavigation {
                SearchContent(query = state.query, books = state.books)
            }
        }
    }


}
