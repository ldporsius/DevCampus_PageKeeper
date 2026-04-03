package nl.codingwithlinda.pagekeeper.design_system.components

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.core.presentation.DefaultMenuActionController
import nl.codingwithlinda.pagekeeper.core.presentation.MenuAction
import nl.codingwithlinda.pagekeeper.core.presentation.NavItem
import nl.codingwithlinda.pagekeeper.design_system.util.DeviceType
import nl.codingwithlinda.pagekeeper.design_system.util.Orientation
import nl.codingwithlinda.pagekeeper.design_system.util.rememberDeviceConfig

@Composable
fun AppNavigation(
    onImportBook: () -> Unit,
    onLibrary: () -> Unit,
    onFavorites: () -> Unit,
    onFinished: () -> Unit,
    content: @Composable () -> Unit
) {
    val deviceConfig = rememberDeviceConfig()
    val useDrawer = deviceConfig.deviceType == DeviceType.Phone &&
            deviceConfig.orientation == Orientation.Portrait

    val controller = remember { DefaultMenuActionController() }
    var selectedIndex by rememberSaveable { mutableIntStateOf(1) }

    val navItems = remember(onImportBook, onLibrary, onFavorites, onFinished) {
        listOf(
            NavItem("", R.drawable.import_book, MenuAction.ImportBookAction(onImportBook), showBackground = true),
            NavItem("Library", R.drawable.menu_library_active, MenuAction.LibraryAction(onLibrary)),
            NavItem("Favorites", R.drawable.menu_favorites_active, MenuAction.FavoritesAction(onFavorites)),
            NavItem("Finished", R.drawable.menu_finished_active, MenuAction.FinishedAction(onFinished)),
        )
    }

    val onItemSelected = { index: Int ->
        selectedIndex = index
        controller.onAction(navItems[index].action)
    }

    if (useDrawer) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        AppNavDrawer(
            items = navItems,
            selectedIndex = selectedIndex,
            onItemSelected = onItemSelected,
            drawerState = drawerState,
            content = content
        )
    } else {
        AppNavRail(
            onMenuClick = {},
            items = navItems,
            selectedIndex = selectedIndex,
            onItemSelected = {
                selectedIndex = navItems.indexOfFirst { item -> item.action == it }
                controller.onAction(it)
            },
            content = content
        )
    }
}