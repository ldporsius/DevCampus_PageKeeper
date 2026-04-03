package nl.codingwithlinda.pagekeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation3.runtime.rememberNavBackStack
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.core.presentation.DefaultMenuActionController
import nl.codingwithlinda.pagekeeper.core.presentation.MenuActionController
import nl.codingwithlinda.pagekeeper.core.presentation.NavigationMenuAction
import nl.codingwithlinda.pagekeeper.core.presentation.ObserveAsEvents
import nl.codingwithlinda.pagekeeper.design_system.components.AppNavigation
import nl.codingwithlinda.pagekeeper.design_system.ui.theme.PageKeeperTheme
import nl.codingwithlinda.pagekeeper.navigation.BookListRoute
import nl.codingwithlinda.pagekeeper.navigation.MainNav
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PageKeeperTheme {
                val scope = rememberCoroutineScope()
                val backstack = rememberNavBackStack(BookListRoute)
                val controller = koinInject<MenuActionController>()

                ObserveAsEvents(controller.actions) {
                    scope.launch {
                        it.undo()
                        it.execute()
                    }
                }
                AppNavigation(
                    selectedIndex = 0,
                    onLibrary = {
                        scope.launch {
                            controller.onAction(NavigationMenuAction(
                                destination = BookListRoute,
                                navigate = {
                                    backstack.retainAll { it is BookListRoute }
                                }
                            ))
                        }
                    },
                    onFavorites = { /* TODO: navigate to favorites */ },
                    onFinished = { /* TODO: navigate to finished */ }
                ) {
                    MainNav(
                        backStack = backstack,
                       )
                }
            }
        }
    }
}
