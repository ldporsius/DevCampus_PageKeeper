package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import nl.codingwithlinda.pagekeeper.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    selectionCount: Int,
    onClear: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text("$selectionCount selected", style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onClear) {
                Icon(
                    painter = painterResource(R.drawable.cancel),
                    contentDescription = "Clear selection"
                )
            }
        },
        actions = {
            IconButton(onClick = onFavorite) {
                Icon(
                    painter = painterResource(R.drawable.menu_favorites_active),
                    contentDescription = "Mark as favorites"
                )
            }
            IconButton(onClick = onShare) {
                Icon(
                    painter = painterResource(R.drawable.share),
                    contentDescription = "Share selected"
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(R.drawable.delete),
                    contentDescription = "Delete selected"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = Color.Transparent
        )
    )
}