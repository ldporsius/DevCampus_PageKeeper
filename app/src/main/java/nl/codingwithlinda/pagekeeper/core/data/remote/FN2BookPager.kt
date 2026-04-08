package nl.codingwithlinda.pagekeeper.core.data.remote

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import nl.codingwithlinda.pagekeeper.core.data.util.sectionAfter
import nl.codingwithlinda.pagekeeper.core.data.util.sectionBetween
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookPager
import nl.codingwithlinda.pagekeeper.core.domain.remote.FormattedLine
import nl.codingwithlinda.pagekeeper.core.domain.remote.Page
import nl.codingwithlinda.pagekeeper.core.domain.remote.TextSpan
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

private val json = Json { ignoreUnknownKeys = true }

@OptIn(ExperimentalSerializationApi::class)
class FN2BookPager(
    private val context: Context
): BookPager {

    private val pRegex = Regex("<p>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
    private val imageRegex = Regex("""<image[^>]+\w+:href="([^"]+)"""")
    private val spanRegex = Regex(
        """<emphasis>(.*?)</emphasis>|<a\s[^>]*?\w+:href="([^"]*)"[^>]*?>(.*?)</a>""",
        RegexOption.DOT_MATCHES_ALL
    )
    private val tagStripRegex = Regex("<[^>]+>")

    override suspend fun writePages(uri: String, book: Book) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri.toUri())?.use { stream ->
                    val bytes = runInterruptible { stream.readBytes() }
                    val body = bytes.sectionBetween("<body>", "</body>") ?: ""
                    val sections = body.split("</section>").filter { it.isNotBlank() }

                    val binarySection = bytes.sectionAfter("</body>") ?: ""
                    val imageMap = extractBinaries(binarySection)

                    val pages = buildPages(sections, imageMap, book)
                    val file = pagesFile(book)
                    runInterruptible { file.outputStream().use { json.encodeToStream(pages, it) } }
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
            if (match.groups[1] != null) {
                spans += TextSpan(match.groupValues[1], emphasis = true)
            } else {
                val url = match.groupValues[2]
                val text = tagStripRegex.replace(match.groupValues[3], "")
                if (text.isNotBlank()) spans += TextSpan(text, url = url)
            }
            cursor = match.range.last + 1
        }
        if (cursor < content.length) {
            spans += TextSpan(content.substring(cursor))
        }
        return FormattedLine(spans)
    }

    private fun extractBinaries(binarySection: String): Map<String, ByteArray> {
        return binarySection.split("</binary>")
            .filter { it.contains("<binary") }
            .associate { chunk ->
                val xml = "$chunk</binary>"
                val binaryTagStart = xml.indexOf("<binary")
                val idMatch = Regex("""id\s*=\s*["']([^"']*)["']""").find(xml.substring(binaryTagStart))
                val id = idMatch?.groupValues?.getOrNull(1) ?: ""
                val contentStart = xml.indexOf('>', binaryTagStart) + 1
                val contentEnd = xml.lastIndexOf("</binary>")
                val base64Text = if (contentStart > binaryTagStart && contentEnd > contentStart)
                    xml.substring(contentStart, contentEnd).trim()
                else ""
                val imageBytes = try {
                    if (base64Text.isNotEmpty()) Base64.decode(base64Text, Base64.DEFAULT)
                    else ByteArray(0)
                } catch (e: Exception) { ByteArray(0) }
                id to imageBytes
            }
    }

    private fun saveImageToStorage(href: String, imageMap: Map<String, ByteArray>, book: Book): String? {
        val id = href.trimStart('#')
        val bytes = imageMap[id]?.takeIf { it.isNotEmpty() } ?: return null
        val safeId = id.replace(Regex("[^a-zA-Z0-9_.-]"), "_")
        val file = File(context.filesDir, "${book.ISBN}_img_$safeId")
        return try {
            file.writeBytes(bytes)
            file.toUri().toString()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun buildPages(sections: List<String>, imageMap: Map<String, ByteArray>, book: Book): List<Page> = buildList {
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
                    val fileUri = saveImageToStorage(imageHref, imageMap, book)
                    add(Page.ImagePage(fileUri ?: imageHref))
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
                runInterruptible { file.inputStream().use { json.decodeFromStream<List<Page>>(it) } }
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