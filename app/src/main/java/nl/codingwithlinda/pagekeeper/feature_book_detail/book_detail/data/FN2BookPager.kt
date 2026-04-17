package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.util.fastFilterNotNull
import androidx.compose.ui.util.fastJoinToString
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import nl.codingwithlinda.pagekeeper.core.data.util.sectionBetween
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.util.Result
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookPager
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookParseError
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.FormattedLine
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Page
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.TextSpan
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

private val json = Json { ignoreUnknownKeys = true }

@OptIn(ExperimentalSerializationApi::class)
class FN2BookPager(
    private val context: Context
): BookPager {

    private val pRegex = Regex("<p>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
    private val imageRegex = Regex("""<image[^>]+\w+:href="([^"]+)"""")


    val pageBuilder = PageBuilder()

    override suspend fun writePages(uri: String, book: Book): Result<Unit, BookParseError> {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri.toUri())?.use { stream ->
                    val bytes = runInterruptible { stream.readBytes() }
                    val body = bytes.sectionBetween("<body>", "</body>") ?: ""

                    val hasTitles = body.contains("<title>")

                    //println("--- FN2 BOOK PAGER HAS TITLES --- $hasTitles")

                    runInterruptible {
                        val sections = body.split("<section>")

                        sections.onEachIndexed { index, string ->
                            val res = parseSection(Section(index), string)
                            val file = File(context.filesDir, "${book.ISBN}_$index.json")
                            file.outputStream().use {
                                json.encodeToStream<List<Section>>(res, it)
                            }
                            pageBuilder.clear()
                        }
                    }
                }
                return@withContext Result.Success(Unit)

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext Result.Failure(BookParseError.GeneralBookParseError)
            }finally {
                withContext(NonCancellable) { pageBuilder.clear() }
            }
        }
        return Result.Failure(BookParseError.GeneralBookParseError)
    }


    private tailrec fun parseSection(section: Section, body: String): List<Section>{

        //val tagSection = body.split("</section>").fastFilterNotNull()

        if (body.isEmpty()) {
            println("--- base case reached, sections in builder: ${pageBuilder.sections.size}")
            return pageBuilder.sections.map { it.value }
        }
        //println("--- FN2 BOOK PAGER FOUND SECTION --- length = ${tagSection.length}")

        val nextSection = body
        var matchFound: Boolean

        var paragraphCount = 0
        var  paragraphMatch = pRegex.find(nextSection, 0)
        matchFound = paragraphMatch != null
        while ( matchFound && paragraphCount < 100) {
            paragraphMatch = paragraphMatch?.next()

            matchFound = paragraphMatch != null
            val paragraph = paragraphMatch?.value ?: break

            pageBuilder.addElementToSection(section, Paragraph(paragraph))
            paragraphCount++
        }

//        println("--- FN2 BOOK PAGER FOUND PARAGRAPHS --- $paragraphCount")
//        println("--- FN2 BOOK PAGER HAS SECTIONS --- ${pageBuilder.sections.size}")
        println("--- FN2 BOOK PAGER LAST ELEMENT --- ${pageBuilder.sections.values.lastOrNull()?.elements?.lastOrNull()?.toPlainText()}")

        val continuation =  body.substringAfter(pageBuilder.sections.values.lastOrNull()?.elements?.lastOrNull()
            ?.toPlainText() ?: "")

        println("--- FN2 BOOK PAGER CONTINUATION --- ${continuation.take(150)}")

        val startNewSection = continuation.startsWith("<section>")
        val newSection = if (startNewSection) Section(section.id + 1) else section
        return pageBuilder.sections.map { it.value }

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

    /* private suspend fun buildPages(sections: List<String>, imageMap: Map<String, ByteArray>, book: Book): List<Page> = buildList {
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
     }*/

    override suspend fun readPages(book: Book): Result<List<Section>, BookParseError> {
        var sections = mutableListOf<Section>()
        return withContext(Dispatchers.IO) {
            try {
                val files = sectionFiles(book)
                if (files.isEmpty()) return@withContext Result.Failure(BookParseError.NoPagesFound)
                runInterruptible {
                    ensureActive()
                    files.onEachIndexed { index, file ->
                        file.inputStream().use {
                            json.decodeFromStream<List<Section>>(it) .also {
                                sections.addAll(it)
                            }
                        }
                    }
                }
                return@withContext Result.Success(sections)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext Result.Failure(BookParseError.GeneralBookParseError)
            }
        }
    }

    override suspend fun loadPages(book: Book, sectionIndex: Int): Result<List<Section>, BookParseError> = withContext(Dispatchers.IO) {
        val pagesRes= readPages(book)

        when(pagesRes){
            is Result.Failure-> {
                when(pagesRes.error){

                    BookParseError.NoPagesFound -> {
                        val uri = Uri.fromFile(File(context.filesDir, "${book.ISBN}.fb2")).toString()
                        writePages(uri, book)
                        loadPages(book)
                    }
                    else ->{
                        return@withContext Result.Failure(BookParseError.GeneralBookParseError)
                    }
                }
            }
            is Result.Success -> {
                return@withContext pagesRes
            }
        }
    }

    fun sectionFiles(book: Book): List<File>{
        return context.filesDir.listFiles()?.filter { it.name.startsWith("${book.ISBN}_") } ?: emptyList()
    }
    private fun pagesFile(book: Book): File{
        return File(context.filesDir, "${book.ISBN}.json")
    }

}