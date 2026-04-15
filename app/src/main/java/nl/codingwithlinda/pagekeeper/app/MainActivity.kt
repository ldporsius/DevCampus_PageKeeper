package nl.codingwithlinda.pagekeeper.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookFormat
import nl.codingwithlinda.pagekeeper.core.navigation.MainNav
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.ui.theme.PageKeeperTheme
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

       var isLoadingBooks = true
        installSplashScreen()
            .setKeepOnScreenCondition {
                isLoadingBooks
            }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val libraryViewModel: LibraryViewModel = koinViewModel()
            val bookListViewModel: BookListViewModel = koinViewModel(qualifier = named("all"))

            bookListViewModel.state.collectAsStateWithLifecycle().value.let { state ->
                isLoadingBooks = state.isLoading
            }

            val filePicker =
                rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { content ->
                    content?.let {
                        libraryViewModel.onAction(LibraryAction.OnImportBookClick(it.toString()))
                    }
                }
            val onImportBook = {
                filePicker.launch(BookFormat.Companion.allMimeTypes)
            }
            PageKeeperTheme {
                MainNav(onImportBook = onImportBook)
            }
        }
    }
}