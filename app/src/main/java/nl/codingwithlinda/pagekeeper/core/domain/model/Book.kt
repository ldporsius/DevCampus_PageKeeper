package nl.codingwithlinda.pagekeeper.core.domain.model

data class Book(
    val ISBN: String,
    val title: String,
    val author: String,
    val imgUrl: String,
    val dateCreated: Long,
    val isFavorite: Boolean = false,
    val isFinished: Boolean = false,
    val currentSection: Int = 0,
    val currentElementId: Int = 0,
    val lastOpenedDate: Long = 0L,
)
