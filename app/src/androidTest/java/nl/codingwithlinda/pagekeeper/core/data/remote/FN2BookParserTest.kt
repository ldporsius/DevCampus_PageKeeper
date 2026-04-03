package nl.codingwithlinda.pagekeeper.core.data.remote

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FN2BookParserTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val parser = FN2BookParser(context)

    @Test
    fun singleAuthor_parsesCorrectly(): Unit = runBlocking {
        val book = parser.fromAssets("book_single_author.fb2")

        assertNotNull(book)
        assertEquals("Pride and Pixels", book!!.title)
        assertEquals("Jane Austen", book.author)
        assertEquals("978-0-000000-01-1", book.ISBN)
    }

    @Test
    fun multipleAuthors_joinedWithComma(): Unit = runBlocking {
        val book = parser.fromAssets("book_multiple_authors.fb2")

        assertNotNull(book)
        assertEquals("Engines of Thought", book!!.title)
        assertEquals("Ada Lovelace, Charles Babbage", book.author)
        assertEquals("978-0-000000-02-8", book.ISBN)
    }

    @Test
    fun noIsbn_returnsEmptyIsbnString(): Unit = runBlocking {
        val book = parser.fromAssets("book_no_isbn.fb2")

        assertNotNull(book)
        assertEquals("The Null Hypothesis", book!!.title)
        assertEquals("", book.ISBN)
    }

    @Test
    fun minimalMetadata_parsesWithoutCrash(): Unit = runBlocking {
        val book = parser.fromAssets("book_minimal_metadata.fb2")

        assertNotNull(book)
        assertEquals("Unknown Title", book!!.title)
        assertEquals("", book.ISBN)
    }

    @Test
    fun originalAssetBook_parsesCorrectly(): Unit = runBlocking {
        val book = parser.fromAssets()

        assertNotNull(book)
        assertEquals("A Second Chance for the Cowboy: Walker Ranch Book 2", book!!.title)
        assertEquals("Tess Thornton", book.author)
    }
}
