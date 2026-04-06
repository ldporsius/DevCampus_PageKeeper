package nl.codingwithlinda.pagekeeper.design_system.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.codingwithlinda.pagekeeper.R
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
    var expanded by rememberSaveable { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = expanded,
                transitionSpec = {
                    val springSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessHigh,
                        visibilityThreshold = androidx.compose.ui.unit.IntOffset(1, 1)
                    )
                    val sizeSpring = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessHigh,
                        visibilityThreshold = androidx.compose.ui.unit.IntSize(1, 1)
                    )
                    if (targetState) {
                        (fadeIn(tween(70)) + slideInHorizontally(springSpec)) togetherWith
                        fadeOut(tween(50)) using SizeTransform { _, _ -> sizeSpring }
                    } else {
                        fadeIn(tween(50)) togetherWith
                        (fadeOut(tween(70)) + slideOutHorizontally(springSpec)) using SizeTransform { _, _ -> sizeSpring }
                    }
                },
                label = "NavRailExpand"
            ) { isExpanded ->
                if (isExpanded) {
                    PermanentDrawerSheet(
                        windowInsets = WindowInsets(0),
                        drawerContainerColor = MaterialTheme.colorScheme.background
                    ) {
                        Spacer(Modifier.height(64.dp))
                        IconButton(
                            onClick = { expanded = false },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.menu_arrow),
                                contentDescription = "Collapse menu"
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        NavigationDrawerItem(
                            icon = {
                                PrimaryButton(
                                    text = "Import book",
                                    iconRes = R.drawable.import_book,
                                    onClick = onImportBook
                                )
                            },
                            label = {},
                            selected = false,
                            onClick = {},
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
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
                                onClick = { onItemSelected(index) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }
                } else {
                    NavigationRail(
                        modifier = Modifier.width(80.dp),
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.primary,
                        windowInsets = WindowInsets(0),
                        header = {
                            IconButton(
                                onClick = { expanded = true },
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
                        Spacer(Modifier.height(32.dp))
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
                                label = {
                                    Text(
                                        item.label,
                                        textAlign = TextAlign.Center
                                    )
                                },
                                selected = index == selectedIndex,
                                onClick = { onItemSelected(index) }
                            )
                        }
                    }
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