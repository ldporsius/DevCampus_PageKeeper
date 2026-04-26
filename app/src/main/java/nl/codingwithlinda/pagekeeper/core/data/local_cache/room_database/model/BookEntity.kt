package nl.codingwithlinda.pagekeeper.core.data.local_cache.room_database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import nl.codingwithlinda.pagekeeper.core.domain.model.Book

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey
    val isbn: String,
    val title: String,
    val author: String,
    val imgUrl: String,
    val dateCreated: Long,
    val isFavorite: Boolean = false,
    val isFinished: Boolean = false,
    val currentSection: Int = 0,
)

fun BookEntity.toDomain(): Book = Book(
    ISBN = isbn,
    title = title,
    author = author,
    imgUrl = imgUrl,
    dateCreated = dateCreated,
    isFavorite = isFavorite,
    isFinished = isFinished,
    currentSection = currentSection,
)

fun Book.toEntity(): BookEntity = BookEntity(
    isbn = ISBN,
    title = title,
    author = author,
    imgUrl = imgUrl,
    dateCreated = dateCreated,
    isFavorite = isFavorite,
    isFinished = isFinished,
    currentSection = currentSection,
)