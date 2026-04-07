package nl.codingwithlinda.pagekeeper.core.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isLessThanOrEqualTo
import assertk.assertions.isGreaterThanOrEqualTo
import kotlinx.coroutines.runBlocking
import nl.codingwithlinda.pagekeeper.core.domain.util.Result
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream
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
        val result = parser.fetch(assetUri("book_single_author.fb2"))

        assertThat(result).isInstanceOf(Result.Success::class)
        val book = (result as Result.Success).data
        assertEquals("Pride and Pixels", book.title)
        assertEquals("Jane Austen", book.author)
        assertEquals("978-0-000000-01-1", book.ISBN)
    }

    @Test
    fun multipleAuthors_joinedWithComma(): Unit = runBlocking {
        val result = parser.fetch(assetUri("book_multiple_authors.fb2"))

        assertThat(result).isInstanceOf(Result.Success::class)
        val book = (result as Result.Success).data
        assertEquals("Engines of Thought", book.title)
        assertEquals("Ada Lovelace, Charles Babbage", book.author)
        assertEquals("978-0-000000-02-8", book.ISBN)
    }

    @Test
    fun noIsbn_usesDocumentId(): Unit = runBlocking {
        val result = parser.fetch(assetUri("book_no_isbn.fb2"))

        assertThat(result).isInstanceOf(Result.Success::class)
        val book = (result as Result.Success).data
        assertEquals("The Null Hypothesis", book.title)
        assertEquals("cccccccc-0003-0003-0003-cccccccccccc", book.ISBN)
    }

    @Test
    fun minimalMetadata_usesDocumentId(): Unit = runBlocking {
        val result = parser.fetch(assetUri("book_minimal_metadata.fb2"))

        assertThat(result).isInstanceOf(Result.Success::class)
        val book = (result as Result.Success).data
        assertEquals("Unknown Title", book.title)
        assertEquals("dddddddd-0004-0004-0004-dddddddddddd", book.ISBN)
    }

    @Test
    fun noCoverImage_returnsDummyFrontCover(): Unit = runBlocking {
        val result = parser.fetch(assetUri("book_single_author.fb2"))

        assertThat(result).isInstanceOf(Result.Success::class)
        val book = (result as Result.Success).data
        assertEquals("", book.imgUrl)
    }

    @Test
    fun originalBook_parsesCorrectly(): Unit = runBlocking {
        val result = parser.fetch(assetUri("book.fb2"))

        assertThat(result).isInstanceOf(Result.Success::class)
        val book = (result as Result.Success).data
        assertEquals("A Second Chance for the Cowboy: Walker Ranch Book 2", book.title)
        assertEquals("Tess Thornton", book.author)
    }

    @Test
    fun scaleCoverBitmap_debugSwap_scalesDownToAtMost200px() = runBlocking {
        val result = parser.fetch(assetUri("book.fb2"))
        if (result !is Result.Success) return@runBlocking
        val book = result.data

        val scaled: Bitmap = appCtx.contentResolver?.openInputStream(book.imgUrl.toUri()).use {
            BitmapFactory.decodeStream(it)
        }

        val maxDim = maxOf(scaled.width, scaled.height)
        assertThat(maxDim).isLessThanOrEqualTo(200)
        assertThat(maxDim).isGreaterThanOrEqualTo(195) // close to 200, not a tiny thumbnail

        // Verify it round-trips as a valid PNG
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
        val decoded = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
        assertThat(maxOf(decoded.width, decoded.height)).isEqualTo(maxDim)
    }
}