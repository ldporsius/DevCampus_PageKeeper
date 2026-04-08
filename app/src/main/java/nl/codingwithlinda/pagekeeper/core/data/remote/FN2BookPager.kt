package nl.codingwithlinda.pagekeeper.core.data.remote

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.codingwithlinda.pagekeeper.core.data.util.sectionBetween
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookPager
import nl.codingwithlinda.pagekeeper.core.domain.remote.FormattedLine
import nl.codingwithlinda.pagekeeper.core.domain.remote.Page
import nl.codingwithlinda.pagekeeper.core.domain.remote.TextSpan
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

private val json = Json { ignoreUnknownKeys = true }

class FN2BookPager(
    private val context: Context
): BookPager {

    private val pRegex = Regex("<p>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
    private val imageRegex = Regex("""xlink:href="([^"]+)"""")
    private val spanRegex = Regex("<emphasis>(.*?)</emphasis>", RegexOption.DOT_MATCHES_ALL)

    override suspend fun writePages(uri: String, book: Book) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri.toUri())?.use { stream ->
                    val bytes = runInterruptible { stream.readBytes() }
                    val body = bytes.sectionBetween("<body>", "</body>") ?: ""
                    val sections = body.split("</section>").filter { it.isNotBlank() }

                    val pages = buildPages(sections)
                    val file = pagesFile(book)
                    runInterruptible { file.writeText(json.encodeToString(pages)) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun parseSpans(content: String): FormattedLine {
        val spans = mutableListOf<TextSpan>()
        var cursor = 0
        spanRegex.findAll(content).forEach { match ->
            if (match.range.first > cursor) {
                spans += TextSpan(content.substring(cursor, match.range.first))
            }
            spans += TextSpan(match.groupValues[1], emphasis = true)
            cursor = match.range.last + 1
        }
        if (cursor < content.length) {
            spans += TextSpan(content.substring(cursor))
        }
        return FormattedLine(spans)
    }

    private suspend fun buildPages(sections: List<String>): List<Page> = buildList {
        val textBuffer = mutableListOf<FormattedLine>()

        fun flushText() {
            if (textBuffer.isNotEmpty()) {
                add(Page.TextPage(textBuffer.toList()))
                textBuffer.clear()
            }
        }

        sections.forEach { section ->
            currentCoroutineContext().ensureActive()
            pRegex.findAll(section).forEach { match ->
                val content = match.groupValues[1]
                val imageHref = imageRegex.find(content)?.groupValues?.get(1)
                if (imageHref != null) {
                    flushText()
                    add(Page.ImagePage(imageHref))
                } else {
                    textBuffer += parseSpans(content)
                    if (textBuffer.size >= 100) flushText()
                }
            }
        }
        flushText()
    }

    override suspend fun readPages(book: Book): List<Page> {
        return withContext(Dispatchers.IO) {
            try {
                val file = pagesFile(book)
                if (!file.exists()) return@withContext emptyList()
                val text = runInterruptible { file.readText() }
                json.decodeFromString<List<Page>>(text)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    override suspend fun loadPages(book: Book): List<Page> {
        var pages = readPages(book)
        if (pages.isEmpty()) {
            val uri = Uri.fromFile(File(context.filesDir, "${book.ISBN}.fb2")).toString()
            writePages(uri, book)
            pages = readPages(book)
        }
        return pages
    }

    private fun pagesFile(book: Book): File =
        File(context.filesDir, "${book.ISBN}.json")
}