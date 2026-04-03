package nl.codingwithlinda.pagekeeper.design_system.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    selectedIndex: Int,
    onLibrary: () -> Unit,
    onFavorites: () -> Unit,
    onFinished: () -> Unit,
    content: @Composable () -> Unit
) {
    val deviceConfig = rememberDeviceConfig()
    val useDrawer = deviceConfig.deviceType == DeviceType.Phone &&
            deviceConfig.orientation == Orientation.Portrait


    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { content ->
        content?.let {
            println("--- FILE PICKER CONTENT: $content")
        }
    }
    val onImportBook = {
        filePicker.launch("*/*")
    }
    val navItems = remember(onImportBook, onLibrary, onFavorites, onFinished) {
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
        AppNavDrawer(
            onImportBook = onImportBook,
            items = navItems,
            selectedIndex = selectedIndex,
            onItemSelected = onItemSelected,
            drawerState = drawerState,
            content = content
        )
    } else {
        AppNavRail(
            onMenuClick = {},
            onImportBook = onImportBook,
            items = navItems,
            selectedIndex = selectedIndex,
            onItemSelected = onItemSelected,
            content = content
        )
    }
}