package nl.codingwithlinda.pagekeeper.design_system.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.core.presentation.NavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavDrawer(
    onImportBook: () -> Unit,
    items: List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    drawerState: DrawerState,
    mainContent: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(64.dp))
                IconButton(
                    onClick = {
                        scope.launch { drawerState.close() }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.menu_arrow),
                        contentDescription = "Close menu"
                    )
                }
                Spacer(Modifier.height(16.dp))
                NavigationDrawerItem(
                    icon = {
                        PrimaryButton(
                            text = "Import book",
                            iconRes = R.drawable.import_book,
                            onClick = {
                                onImportBook()
                                scope.launch { drawerState.close() }
                            }
                        )

                    },
                    label = {  },
                    selected = false,
                    onClick = {

                    }
                )
                Spacer(Modifier.height(64.dp))
                items.forEachIndexed { index, item ->
                    NavigationDrawerItem(
                        icon = {
                            if (item.showBackground) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                ) {
                                    Icon(
                                        painter = painterResource(item.iconRes),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    painter = painterResource(item.iconRes),
                                    contentDescription = null
                                )
                            }
                        },
                        label = { Text(item.label) },
                        selected = index == selectedIndex,
                        onClick = {
                            onItemSelected(index)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        mainContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    title: String,
    onMenuClick: () -> Unit,
    onSearch: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            painter = painterResource(R.drawable.menu),
                            contentDescription = "Open menu"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearch) {
                        Icon(
                            painter = painterResource(R.drawable.search),
                            contentDescription = "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) { content() }
    }
}

@Composable
fun SearchScaffold(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "Back"
                    )
                }
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Search books...") },
                    singleLine = true,
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    painter = painterResource(R.drawable.cancel),
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }
            content()
        }
    }
}