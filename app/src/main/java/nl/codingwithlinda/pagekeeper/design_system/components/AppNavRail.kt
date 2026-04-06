package nl.codingwithlinda.pagekeeper.design_system.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.core.presentation.MenuAction
import nl.codingwithlinda.pagekeeper.core.presentation.NavItem

@Composable
fun AppNavRail(
    onMenuClick: () -> Unit,
    onImportBook: () -> Unit,
    items: List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

        ) {
            NavigationRail(
                modifier = Modifier.width(80.dp),
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                windowInsets = WindowInsets(0),
                header = {
                    IconButton(
                        onClick = { onMenuClick() },
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.menu),
                            contentDescription = "Open menu"
                        )
                    }
                }
            ) {
                NavigationRailItem(
                    selected = false,
                    onClick = { onImportBook() },
                    icon = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                                .padding(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.import_book),
                                contentDescription = "Import book",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                )
                items.forEachIndexed { index, item ->
                    NavigationRailItem(
                        icon = {
                            if (item.showBackground) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(item.iconRes),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
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
                        label = { Text(item.label,
                                modifier = Modifier,
                            textAlign = TextAlign.Center

                        ) },
                        selected = index == selectedIndex,
                        onClick = { onItemSelected(index) }
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                content()
            }
        }
    }
}