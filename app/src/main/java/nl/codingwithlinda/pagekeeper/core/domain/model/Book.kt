package nl.codingwithlinda.pagekeeper.core.domain.model

data class Book(
    val ISBN: String,
    val title: String,
    val author: String,
    val imgUrl: String,
    val dateCreated: Long
)
