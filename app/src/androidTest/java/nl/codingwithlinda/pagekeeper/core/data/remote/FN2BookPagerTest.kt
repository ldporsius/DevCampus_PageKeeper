package nl.codingwithlinda.pagekeeper.core.data.remote

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isNotEmpty
import kotlinx.coroutines.runBlocking
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Page
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data.FN2BookPager
import org.junit.Test
import java.io.File

class FN2BookPagerTest {

    private val appCtx: Context = ApplicationProvider.getApplicationContext()
    private val pager = FN2BookPager(appCtx)

    private val fakeBook = Book(
        ISBN = "978-1-957082-04-2",
        title = "A Second Chance for the Cowboy: Walker Ranch Book 2",
        author = "Tess Thornton",
        imgUrl = "",
        dateCreated = 0L
    )

    private fun assetUri(filename: String): String {
        val file = File(appCtx.filesDir, filename)
        file.parentFile?.mkdirs()
        InstrumentationRegistry.getInstrumentation().context.assets
            .open(filename).use { it.copyTo(file.outputStream()) }
        return Uri.fromFile(file).toString()
    }

    @Test
    fun writePages_withValidFb2_completesWithoutException(): Unit = runBlocking {
        val uri = assetUri("book.fb2")
        pager.writePages(uri, fakeBook)
        // reaching here means no exception propagated
    }

    @Test
    fun writePages_withInvalidUri_doesNotThrow(): Unit = runBlocking {
        // FN2BookPager catches internal exceptions; should not propagate to caller
        pager.writePages("file:///nonexistent/book.fb2", fakeBook)
    }

    @Test
    fun writeThenRead_returnsPersistedPages(): Unit = runBlocking {
        val uri = assetUri("book.fb2")
        pager.writePages(uri, fakeBook)

        val pages = pager.readPages(fakeBook)
        assertThat(pages).isNotEmpty()
    }

    @Test
    fun writeThenRead_containsTextAndImagePages(): Unit = runBlocking {
        val uri = assetUri("book.fb2")
        pager.writePages(uri, fakeBook)

        val pages = pager.readPages(fakeBook)
        assertThat(pages.filterIsInstance<Page.ImagePage>()).isNotEmpty()
        assertThat(pages.filterIsInstance<Page.TextPage>()).isNotEmpty()
    }

    @Test
    fun readPages_beforeWrite_returnsEmptyList(): Unit = runBlocking {
        // delete any cached file from a previous test run
        File(appCtx.filesDir, "${fakeBook.ISBN}.json").delete()

        val pages = pager.readPages(fakeBook)
        assertThat(pages).isEmpty()
    }
}