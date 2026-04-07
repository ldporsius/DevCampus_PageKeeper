package nl.codingwithlinda.pagekeeper.design_system.components

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.core.presentation.ImportBookMenuAction
import nl.codingwithlinda.pagekeeper.core.presentation.MenuActionController
import nl.codingwithlinda.pagekeeper.core.presentation.NavItem
import nl.codingwithlinda.pagekeeper.core.presentation.NavigationMenuAction
import nl.codingwithlinda.pagekeeper.design_system.util.DeviceType
import nl.codingwithlinda.pagekeeper.design_system.util.Orientation
import nl.codingwithlinda.pagekeeper.design_system.util.rememberDeviceConfig
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookFilter
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookListViewModel
import nl.codingwithlinda.pagekeeper.navigation.BookListRoute
import nl.codingwithlinda.pagekeeper.navigation.FavoritesRoute
import nl.codingwithlinda.pagekeeper.navigation.FinishedRoute
import nl.codingwithlinda.pagekeeper.navigation.SearchRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

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