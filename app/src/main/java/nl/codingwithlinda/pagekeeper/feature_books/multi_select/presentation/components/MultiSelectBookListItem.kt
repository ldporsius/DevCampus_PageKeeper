package nl.codingwithlinda.pagekeeper.feature_books.multi_select.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.ui.theme.lora
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction

@Composable
fun MultiSelectBookListItem(
    book: BookUi,
    isSelected: Boolean,
    onToggle: (String) -> Unit,
    onAction: (BookListItemAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable { onToggle(book.isbn) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(
                if (isSelected) R.drawable.checkbox_checked else R.drawable.checkbox
            ),
            contentDescription = if (isSelected) "Selected" else "Not selected",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterVertically)
        )

        AsyncImage(
            model = book.imgUrl.ifEmpty { null },
            contentDescription = book.title,
            contentScale = ContentScale.Fit,
            error = painterResource(R.drawable.book),
            placeholder = painterResource(R.drawable.book),
            modifier = Modifier
                .size(width = 96.dp, height = 172.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = MaterialTheme.colorScheme.surfaceVariant)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = lora,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val favIcon = if (book.isFavorite) R.drawable.menu_favorites_active else R.drawable.menu_favorites_deactive
                IconButton(onClick = { onAction(BookListItemAction.FavouriteClick(book.isbn)) }) {
                    Icon(
                        painter = painterResource(favIcon),
                        contentDescription = "Favourite",
                        tint = if (book.isFavorite) MaterialTheme.colorScheme.primary else iconTint
                    )
                }
                val finishIcon = if (book.isFinished) R.drawable.finished else R.drawable.finish
                IconButton(onClick = { onAction(BookListItemAction.FinishClick(book.isbn)) }) {
                    Icon(
                        painter = painterResource(finishIcon),
                        contentDescription = "Mark as finished",
                        tint = iconTint
                    )
                }
                IconButton(onClick = { onAction(BookListItemAction.ShareClick(book.isbn)) }) {
                    Icon(
                        painter = painterResource(R.drawable.share),
                        contentDescription = "Share",
                        tint = iconTint
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { onAction(BookListItemAction.DeleteClick(book.isbn)) }) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = "Delete",
                        tint = iconTint
                    )
                }
            }
        }
    }
}
