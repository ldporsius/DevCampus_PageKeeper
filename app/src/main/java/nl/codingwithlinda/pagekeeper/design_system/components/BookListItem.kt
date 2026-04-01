package nl.codingwithlinda.pagekeeper.design_system.components

import androidx.compose.foundation.background
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
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.design_system.ui.theme.PageKeeperTheme

sealed interface BookListItemAction {
    data object FavouriteClick : BookListItemAction
    data object ReadingClick : BookListItemAction
    data object ShareClick : BookListItemAction
    data object DeleteClick : BookListItemAction
}

@Composable
fun BookListItem(
    book: Book,
    modifier: Modifier = Modifier,
    onAction: (BookListItemAction) -> Unit = {}
) {
    val iconTint = MaterialTheme.colorScheme.onSecondary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = book.imgUrl,
            contentDescription = book.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 80.dp, height = 110.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondary
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAction(BookListItemAction.FavouriteClick) }) {
                    Icon(
                        painter = painterResource(R.drawable.favorites),
                        contentDescription = "Favourite",
                        tint = iconTint
                    )
                }
                IconButton(onClick = { onAction(BookListItemAction.ReadingClick) }) {
                    Icon(
                        painter = painterResource(R.drawable.import_book),
                        contentDescription = "Currently reading",
                        tint = iconTint
                    )
                }
                IconButton(onClick = { onAction(BookListItemAction.ShareClick) }) {
                    Icon(
                        painter = painterResource(R.drawable.share),
                        contentDescription = "Share",
                        tint = iconTint
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { onAction(BookListItemAction.DeleteClick) }) {
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
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            BookListItem(
                book = Book(
                    ISBN = "9780195034929",
                    title = "The Adventures of Tom Sawyer",
                    author = "Mark Twain",
                    imgUrl = "https://covers.openlibrary.org/b/olid/OL7353617M-M.jpg",
                    dateCreated = "2026-04-01"
                )
            )
        }
    }
}