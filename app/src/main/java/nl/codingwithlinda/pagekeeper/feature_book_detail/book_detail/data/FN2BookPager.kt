package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import nl.codingwithlinda.pagekeeper.core.data.util.sectionBetween
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.util.Result
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookPager
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookParseError
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

private val json = Json { ignoreUnknownKeys = true }

internal val sectionRegex = Regex("<section>(.*?)</section>", RegexOption.DOT_MATCHES_ALL)

@OptIn(ExperimentalSerializationApi::class)
class FN2BookPager(
    private val context: Context
): BookPager {
    private val pRegex = Regex("<p>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
    private val imageRegex = Regex("""<image[^>]+\w+:href="([^"]+)"""")

    val pageBuilder = PageBuilder()


    suspend fun bookInfo(uri: String) {
        context.contentResolver.openInputStream(uri.toUri())?.use { stream ->
            val bytes = runInterruptible { stream.readBytes() }
            val body = bytes.sectionBetween("<body>", "</body>") ?: ""
        }
    }
    override suspend fun writePages(uri: String, book: Book): Result<Unit, BookParseError> {
        withContext(Dispatchers.IO) {
            try {
                val uri = Uri.fromFile(File(context.filesDir, "${book.ISBN}.fb2")).toString()
                context.contentResolver.openInputStream(uri.toUri())?.use { stream ->
                    val bytes = runInterruptible { stream.readBytes() }
                    val body = bytes.sectionBetween("<body>", "</body>") ?: ""


                    val hasTitles = body.contains("<title>")

                    println("--- FN2 BOOK PAGER HAS TITLES --- $hasTitles")

                    runInterruptible {
                        val sections = sectionRegex.findAll(body)

                        println("--- FN2 BOOK PAGER FOUND SECTIONS --- ${sections.firstOrNull()?.value}")

                        sections.forEachIndexed { index, string ->
                            println("--- FN2 BOOK PAGER PARSING SECTION --- $index")

                            val res = parseSection(Section(index), string.value)
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
            }
            catch (oom: OutOfMemoryError){
                return@withContext Result.Failure(BookParseError.OOM)
            }
            catch (e: Exception) {
                e.printStackTrace()
                return@withContext Result.Failure(BookParseError.GeneralBookParseError)
            }finally {
                withContext(NonCancellable) { pageBuilder.clear() }
            }
        }
        return Result.Failure(BookParseError.GeneralBookParseError)
    }


    private fun parseSection(section: Section, body: String): List<Section>{

        if (body.isEmpty()) {
            println("--- base case reached, sections in builder: ${pageBuilder.sections.size}")
            return pageBuilder.sections.map { it.value }
        }

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
                return@withContext pagesRes

               /* when(pagesRes.error){

                    BookParseError.NoPagesFound -> {
                        val uri = Uri.fromFile(File(context.filesDir, "${book.ISBN}.fb2")).toString()
                        //writePages(uri, book)
                        return@withContext Result.Success(emptyList())
                    }
                    else ->{
                        return@withContext Result.Failure(BookParseError.GeneralBookParseError)
                    }
                }*/
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