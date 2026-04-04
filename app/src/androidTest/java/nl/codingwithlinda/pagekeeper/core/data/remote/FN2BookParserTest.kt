package nl.codingwithlinda.pagekeeper.core.data.remote

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.runBlocking
import nl.codingwithlinda.pagekeeper.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File

class FN2BookParserTest {

    private val appCtx: Context = ApplicationProvider.getApplicationContext()
    private val parser = FN2BookParser(appCtx)

    /** Copies an asset to filesDir and returns its content:// URI so fetch() can open it. */
    private fun assetUri(filename: String): String {
        val file = File(appCtx.filesDir, filename)
        file.parentFile?.mkdirs()
        // Use the instrumentation context to access assets bundled in the test APK
        InstrumentationRegistry.getInstrumentation().context.assets
            .open(filename).use { it.copyTo(file.outputStream()) }
        return Uri.fromFile(file).toString()
    }

    @Test
    fun singleAuthor_parsesCorrectly(): Unit = runBlocking {
        val book = parser.fetch(assetUri("book_single_author.fb2"))

        assertNotNull(book)
        assertEquals("Pride and Pixels", book!!.title)
        assertEquals("Jane Austen", book.author)
        assertEquals("978-0-000000-01-1", book.ISBN)
    }

    @Test
    fun multipleAuthors_joinedWithComma(): Unit = runBlocking {
        val book = parser.fetch(assetUri("book_multiple_authors.fb2"))

        assertNotNull(book)
        assertEquals("Engines of Thought", book!!.title)
        assertEquals("Ada Lovelace, Charles Babbage", book.author)
        assertEquals("978-0-000000-02-8", book.ISBN)
    }

    @Test
    fun noIsbn_returnsRandomIsbnString(): Unit = runBlocking {
        val book = parser.fetch(assetUri("book_no_isbn.fb2"))

        assertNotNull(book)
        assertEquals("The Null Hypothesis", book!!.title)
        assertThat(book.ISBN.length).isEqualTo(36)
    }

    @Test
    fun minimalMetadata_parsesWithoutCrash(): Unit = runBlocking {
        val book = parser.fetch(assetUri("book_minimal_metadata.fb2"))

        assertNotNull(book)
        assertEquals("Unknown Title", book!!.title)
        assertEquals("", book.ISBN)
    }

    @Test
    fun noCoverImage_returnsDummyFrontCover(): Unit = runBlocking {
        val book = parser.fetch(assetUri("book_single_author.fb2"))

        assertNotNull(book)
        val expectedImgUrl = ""
        assertEquals(expectedImgUrl, book!!.imgUrl)
    }

    @Test
    fun originalBook_parsesCorrectly(): Unit = runBlocking {
        val book = parser.fetch(assetUri("book.fb2"))

        assertNotNull(book)
        assertEquals("A Second Chance for the Cowboy: Walker Ranch Book 2", book!!.title)
        assertEquals("Tess Thornton", book.author)
    }
}
