package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.design_system.ui.theme.PageKeeperTheme
import nl.codingwithlinda.pagekeeper.design_system.ui.theme.lora
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction

@Composable
fun BookListItem(
    book: BookUi,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onAction: (BookListItemAction) -> Unit
) {
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .background(MaterialTheme.colorScheme.background)
            .combinedClickable(onClick = onClick, onLongClick = { onAction(BookListItemAction.MultiSelectLongPress) })
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                val icon = if(book.isFavorite) R.drawable.menu_favorites_active else R.drawable.menu_favorites_deactive
                IconButton(onClick = { onAction(BookListItemAction.FavouriteClick(book.isbn)) }) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = "Favourite",
                        tint = if (book.isFavorite) MaterialTheme.colorScheme.primary else iconTint
                    )
                }
                val fIcon = if(book.isFinished) R.drawable.finished else R.drawable.finish
                IconButton(onClick = { onAction(BookListItemAction.FinishClick(book.isbn)) }) {
                    Icon(
                        painter = painterResource(fIcon),
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

@Preview(showBackground = true)
@Composable
private fun PreviewBookListItem() {
    PageKeeperTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            BookListItem(
                book = BookUi(
                    isbn = "9780195034929",
                    title = "The Adventures of Tom Sawyer",
                    author = "Mark Twain",
                    imgUrl = "https://covers.openlibrary.org/b/olid/OL7353617M-M.jpg",
                    formattedDate = "Apr 1, 2026"
                ),
                onAction = {}
            )
        }
    }
}