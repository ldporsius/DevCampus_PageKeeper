package nl.codingwithlinda.pagekeeper.feature_books.common.presentation.form_factors

import androidx.compose.runtime.Composable
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.util.DeviceType
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.util.rememberDeviceConfig
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

@Composable
fun BooksRoot(
    onImportBook: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    bookListViewModel: BookListViewModel = koinViewModel(qualifier = named("all")),
    libraryViewModel: LibraryViewModel = koinViewModel()
) {
    val deviceConfig = rememberDeviceConfig()
    if (deviceConfig.deviceType == DeviceType.Tablet) {
        BooksTabletLayout(
            onImportBook = onImportBook,
            onNavigateToDetail = onNavigateToDetail,
            bookListViewModel = bookListViewModel,
            libraryViewModel = libraryViewModel
        )
    } else {
        BooksPhoneLayout(
            onImportBook = onImportBook,
            onNavigateToDetail = onNavigateToDetail,
            bookListViewModel = bookListViewModel,
            libraryViewModel = libraryViewModel
        )
    }
}