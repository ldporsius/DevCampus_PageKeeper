package nl.codingwithlinda.pagekeeper.core.presentation.design_system.components

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.core.navigation.ImportBookMenuAction
import nl.codingwithlinda.pagekeeper.core.navigation.MenuActionController
import nl.codingwithlinda.pagekeeper.core.navigation.NavItem
import nl.codingwithlinda.pagekeeper.core.navigation.NavigationMenuAction
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.util.DeviceType
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.util.Orientation
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.util.rememberDeviceConfig
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.core.navigation.BookListRoute
import nl.codingwithlinda.pagekeeper.core.navigation.FavoritesRoute
import nl.codingwithlinda.pagekeeper.core.navigation.FinishedRoute
import nl.codingwithlinda.pagekeeper.core.navigation.SearchRoute
import org.koin.compose.koinInject

@Composable
fun AppNavigation(
    selectedIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val controller = koinInject<MenuActionController>()
    val scope = rememberCoroutineScope()
    val onImportBook: () -> Unit = { scope.launch { controller.onAction(ImportBookMenuAction) } }


    val deviceConfig = rememberDeviceConfig()
    val useDrawer = deviceConfig.deviceType == DeviceType.Phone &&
            deviceConfig.orientation == Orientation.Portrait

    val navItems = remember() {
        listOf(
            NavItem("Library", R.drawable.menu_library_active, {
                scope.launch {
                    controller.onAction(NavigationMenuAction(BookListRoute))
                }
            }),
            NavItem("Favorites", R.drawable.menu_favorites_active, {
                scope.launch {
                    controller.onAction(NavigationMenuAction(FavoritesRoute))
                }
            }),
            NavItem("Finished", R.drawable.menu_finished_active, {
                scope.launch {
                    controller.onAction(NavigationMenuAction(FinishedRoute))
                }
            }),
        )
    }

    val onItemSelected = { index: Int ->
        navItems[index].action()
    }

    if (useDrawer) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        AppNavDrawer(
            onImportBook = onImportBook,
            items = navItems,
            selectedIndex = selectedIndex,
            onItemSelected = onItemSelected,
            drawerState = drawerState,
            mainContent = {
                    MainScaffold(
                        title = navItems[selectedIndex].label,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onSearch = {
                            scope.launch {
                                controller.onAction(NavigationMenuAction(SearchRoute(BookFilter.All)))
                            }
                        }
                    ) {
                        content()
                    }
                }

        )
    } else {
        AppNavRail(
            onMenuClick = {},
            onImportBook = onImportBook,
            items = navItems,
            selectedIndex = selectedIndex,
            onItemSelected = onItemSelected,
            content = {
                content()
            }
        )
    }
}