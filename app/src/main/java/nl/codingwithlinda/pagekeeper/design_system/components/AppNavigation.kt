package nl.codingwithlinda.pagekeeper.design_system.components

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.core.presentation.ImportBookMenuAction
import nl.codingwithlinda.pagekeeper.core.presentation.MenuActionController
import nl.codingwithlinda.pagekeeper.core.presentation.NavItem
import nl.codingwithlinda.pagekeeper.design_system.util.DeviceType
import nl.codingwithlinda.pagekeeper.design_system.util.Orientation
import nl.codingwithlinda.pagekeeper.design_system.util.rememberDeviceConfig
import nl.codingwithlinda.pagekeeper.feature_books.search.width_compact.SearchRoot
import org.koin.compose.koinInject

@Composable
fun AppNavigation(
    selectedIndex: Int,
    onLibrary: () -> Unit,
    onFavorites: () -> Unit,
    onFinished: () -> Unit,
    onSearch: () -> Unit,
    content: @Composable () -> Unit
) {
    val controller = koinInject<MenuActionController>()
    val scope = rememberCoroutineScope()
    val onImportBook: () -> Unit = { scope.launch { controller.onAction(ImportBookMenuAction) } }

    val deviceConfig = rememberDeviceConfig()
    val useDrawer = deviceConfig.deviceType == DeviceType.Phone &&
            deviceConfig.orientation == Orientation.Portrait

    val navItems = remember(onLibrary, onFavorites, onFinished) {
        listOf(
            NavItem("Library", R.drawable.menu_library_active, onLibrary),
            NavItem("Favorites", R.drawable.menu_favorites_active, onFavorites),
            NavItem("Finished", R.drawable.menu_finished_active, onFinished),
        )
    }

    val onItemSelected = { index: Int ->
        navItems[index].action()
    }

    if (useDrawer) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
       // var showSearch by remember { mutableStateOf(false) }

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
                        onSearch = { onSearch() }
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
            content = { content() }
        )
    }
}