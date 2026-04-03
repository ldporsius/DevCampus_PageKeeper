package nl.codingwithlinda.pagekeeper.feature_books.common.presentation

import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BookUi(
    val isbn: String,
    val title: String,
    val author: String,
    val imgUrl: String,
    val formattedDate: String,
    val isFavorite: Boolean = false,
    val isFinished: Boolean = false
)

fun Book.toBookUi(): BookUi {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return BookUi(
        isbn = ISBN,
        title = title,
        author = author,
        imgUrl = imgUrl,
        formattedDate = formatter.format(Date(dateCreated)),
        isFavorite = isFavorite,
        isFinished = isFinished
    )
}