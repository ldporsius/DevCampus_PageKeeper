package nl.codingwithlinda.pagekeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation3.runtime.rememberNavBackStack
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookFormat
import nl.codingwithlinda.pagekeeper.core.presentation.MenuActionController
import nl.codingwithlinda.pagekeeper.core.presentation.NavigationMenuAction
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.design_system.components.AppNavigation
import nl.codingwithlinda.pagekeeper.design_system.ui.theme.PageKeeperTheme
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.LibraryViewModel
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.LibraryAction
import nl.codingwithlinda.pagekeeper.navigation.BookListRoute
import nl.codingwithlinda.pagekeeper.navigation.FavoritesRoute
import nl.codingwithlinda.pagekeeper.navigation.FinishedRoute
import nl.codingwithlinda.pagekeeper.navigation.MainNav
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val scope = rememberCoroutineScope()
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

                val backstack = rememberNavBackStack(BookListRoute)
                val controller = koinInject<MenuActionController>()

                ObserveAsEvents(controller.actions) {
                    scope.launch {
                        it.undo()
                        it.execute()
                    }
                }
                val selectedIndex = when (backstack.lastOrNull()) {
                    is FavoritesRoute -> 1
                    is FinishedRoute -> 2
                    else -> 0
                }

                AppNavigation(
                    selectedIndex = selectedIndex,
                    onLibrary = {
                        scope.launch {
                            controller.onAction(NavigationMenuAction(
                                destination = BookListRoute,
                                navigate = {
                                    backstack.add(BookListRoute)
                                    backstack.retainAll { it is BookListRoute }
                                }
                            ))
                        }
                    },
                    onFavorites = {
                        scope.launch {
                            controller.onAction(NavigationMenuAction(
                                destination = FavoritesRoute,
                                navigate = {
                                    backstack.add(FavoritesRoute)
                                    backstack.retainAll { it is FavoritesRoute }
                                }
                            ))
                        }
                    },
                    onFinished = {
                        scope.launch {
                            controller.onAction(NavigationMenuAction(
                                destination = FinishedRoute,
                                navigate = {
                                    backstack.add(FinishedRoute)
                                    backstack.retainAll { it is FinishedRoute }
                                }
                            ))
                        }
                    },
                    onImportBook = onImportBook
                ) {
                    MainNav(
                        backStack = backstack,
                        onImportBook = onImportBook
                    )
                }
            }
        }
    }
}
