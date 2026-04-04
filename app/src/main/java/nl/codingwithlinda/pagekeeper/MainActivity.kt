package nl.codingwithlinda.pagekeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookFormat
import nl.codingwithlinda.pagekeeper.design_system.ui.theme.PageKeeperTheme
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction
import nl.codingwithlinda.pagekeeper.navigation.MainNav
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val libraryViewModel: LibraryViewModel = koinViewModel()
            val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { content ->
                content?.let {
                    libraryViewModel.onAction(LibraryAction.OnImportBookClick(it.toString()))
                }
            }
            val onImportBook = {
                filePicker.launch(BookFormat.allMimeTypes)
            }
            PageKeeperTheme {
                MainNav(onImportBook = onImportBook)
            }
        }
    }
}
