package nl.codingwithlinda.pagekeeper.feature_books.common.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.components.PrimaryButton
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.ui.theme.lora
import nl.codingwithlinda.pagekeeper.feature_books.common.presentation.BookUi
import nl.codingwithlinda.pagekeeper.feature_books.library.presentation.interaction.BookListItemAction

@Composable
fun LastOpenedBookBanner(
    book: BookUi,
    onClick: () -> Unit,
    onAction: (BookListItemAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = book.imgUrl.ifEmpty { null },
                contentDescription = book.title,
                contentScale = ContentScale.Fit,
                error = painterResource(R.drawable.book),
                placeholder = painterResource(R.drawable.book),
                modifier = Modifier
                    .size(width = 64.dp, height = 96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = lora,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { book.readingProgress },
                    modifier = Modifier.fillMaxWidth()
                )
                Row {
                    IconButton(onClick = { onAction(BookListItemAction.FavouriteClick(book.isbn)) }) {
                        Icon(
                            painter = painterResource(if (book.isFavorite) R.drawable.menu_favorites_active else R.drawable.menu_favorites_deactive),
                            contentDescription = "Favourite",
                            tint = if (book.isFavorite) MaterialTheme.colorScheme.primary else iconTint
                        )
                    }
                    IconButton(onClick = { onAction(BookListItemAction.FinishClick(book.isbn)) }) {
                        Icon(
                            painter = painterResource(if (book.isFinished) R.drawable.finished else R.drawable.finish),
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
                    IconButton(onClick = { onAction(BookListItemAction.DeleteClick(book.isbn)) }) {
                        Icon(
                            painter = painterResource(R.drawable.delete),
                            contentDescription = "Delete",
                            tint = iconTint
                        )
                    }
                }
            }

            PrimaryButton(
                iconRes = R.drawable.book_vector,
                text = stringResource(R.string.continue_reading_button),
                onClick = onClick
            )
        }
    }
}