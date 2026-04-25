package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import nl.codingwithlinda.pagekeeper.core.data.util.sectionBetween
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Citation
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Epigraph
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.PageElement
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Paragraph
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Section
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Title
import nl.codingwithlinda.pagekeeper.core.domain.util.Result
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookParseError
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.LazyBookPager
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

private val json = Json { ignoreUnknownKeys = true }

private val pRegex              = Regex("<p>(.*?)</p>",                     RegexOption.DOT_MATCHES_ALL)
private val titleRegex          = Regex("<title>(.*?)</title>",             RegexOption.DOT_MATCHES_ALL)
private val citeRegex           = Regex("<cite[^>]*?>(.*?)</cite>",         RegexOption.DOT_MATCHES_ALL)
private val epigraphRegex       = Regex("<epigraph[^>]*?>(.*?)</epigraph>", RegexOption.DOT_MATCHES_ALL)
private val firstStrongPRegex   = Regex(
    """\s*(?:<empty-line\s*/>\s*)*<p[^>]*?>\s*<strong[^>]*?>(.*?)</strong>\s*</p>""".trimIndent(),
    option = (RegexOption.MULTILINE)
)

private val elementParsers: List<Pair<Regex, (MatchResult) -> PageElement>> = listOf(
    titleRegex        to { m -> Title(pRegex.replace(m.groupValues[1], "$1").trim()) },
    firstStrongPRegex to { m -> Title(m.groupValues[1].trim()) },
    citeRegex         to { m -> Citation(pRegex.replace(m.groupValues[1], "$1").trim()) },
    epigraphRegex     to { m -> Epigraph(pRegex.replace(m.groupValues[1], "$1").trim()) },
    pRegex            to { m -> Paragraph(m.value) }
)

internal fun parseElements(body: String): List<PageElement> {
    val candidates = elementParsers
        .flatMap { (regex, factory) -> regex.findAll(body).map { Triple(it.range.first, it.range.last, factory(it)) } }
        .sortedBy { it.first }
    val result = mutableListOf<Triple<Int, Int, PageElement>>()
    for (candidate in candidates) {
        val containedByExisting = result.any { existing -> candidate.first >= existing.first && candidate.second <= existing.second }
        if (!containedByExisting) result += candidate
    }
    return result.map { it.third }
}

internal suspend fun findTopLevelSections(body: String): List<String> = withContext(Dispatchers.Default) {
    val result = mutableListOf<String>()
    val open = "<section>"
    val close = "</section>"
    var pos = 0
    while (pos < body.length) {
        val start = body.indexOf(open, pos)
        if (start == -1) break
        var depth = 1
        var search = start + open.length
        while (depth > 0 && search < body.length) {
            val nextOpen = body.indexOf(open, search)
            val nextClose = body.indexOf(close, search)
            if (nextClose == -1) break
            if (nextOpen != -1 && nextOpen < nextClose) {
                depth++
                search = nextOpen + open.length
            } else {
                depth--
                search = nextClose + close.length
            }
        }
        if (depth == 0) {
            result += body.substring(start, search)
            pos = search
        } else break
    }
    return@withContext result
}

@OptIn(ExperimentalSerializationApi::class)
class FN2BookPager(
    private val context: Context
): LazyBookPager {
    private val imageRegex = Regex("""<image[^>]+\w+:href="([^"]+)"""")


    override suspend fun writePages(uri: String, book: Book): Result<Unit, BookParseError> {
        return withContext(Dispatchers.IO) {
            try {
                val uri = Uri.fromFile(File(context.filesDir, "${book.ISBN}.fb2")).toString()
                context.contentResolver.openInputStream(uri.toUri())?.use { stream ->
                    val bytes = runInterruptible { stream.readBytes() }
                    val body = bytes.sectionBetween("<body>", "</body>") ?: ""

                    val hasTitles = body.contains("<title>")
                    println("--- FN2 BOOK PAGER HAS TITLES --- $hasTitles")

                    val topLevelSections = findTopLevelSections(body)

                    println("--- FN2 BOOK PAGER FOUND SECTIONS --- ${topLevelSections.size}")

                    topLevelSections.forEachIndexed { index, html ->
                        println("--- FN2 BOOK PAGER PARSING SECTION --- $index")

                        ensureActive()
                        val inner = html.removePrefix("<section>").removeSuffix("</section>")
                        val section = parseSection(index, inner)
                        val file = File(context.filesDir, "${book.ISBN}_$index.json")
                        file.outputStream().use {
                            json.encodeToStream<List<Section>>(listOf(section), it)
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
            }
        }
    }


    internal suspend fun parseSection(sectionId: Int, body: String): Section {
        val nestedSections = findTopLevelSections(body)
        val elements: List<PageElement> = if (nestedSections.isEmpty()) {
            parseElements(body)
        } else {
            nestedSections.mapIndexed { index, html ->
                val inner = html.removePrefix("<section>").removeSuffix("</section>")
                parseSection(sectionId * 100 + index, inner)
            }
        }
        return Section(sectionId, elements)
    }


    override suspend fun readPages(book: Book): Result<List<Section>, BookParseError> {
        var sections = mutableListOf<Section>()
        return withContext(Dispatchers.IO) {
            try {
                val files = sectionFiles(book)
                if (files.isEmpty()) return@withContext Result.Failure(BookParseError.NoPagesFound)
                runInterruptible {
                    files.onEachIndexed { index, file ->
                        ensureActive()
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

    override suspend fun hasPages(book: Book): Boolean = withContext(Dispatchers.IO) {
        sectionFiles(book).isNotEmpty()
    }

    override suspend fun loadPages(book: Book, sectionIndex: Int): Result<List<Section>, BookParseError> = withContext(Dispatchers.IO) {
       try {
           val pagesRes= sectionFiles(book).getOrNull(sectionIndex).let { file ->
               if (file == null) return@let Result.Failure(BookParseError.NoPagesFound)
               val list = file.inputStream().use {
                   ensureActive()
                   json.decodeFromStream<List<Section>>(it)
               }
               Result.Success(list)
           }

           return@withContext pagesRes


       }catch (e: CancellationException){
           throw e
       }
        catch (e: Exception) {
            e.printStackTrace()
            return@withContext Result.Failure(BookParseError.GeneralBookParseError)
        }

    }

    override suspend fun loadChapter(book: Book, sectionIndex: Int): Flow<Section> {
        return flow {
            sectionFiles(book).getOrNull(sectionIndex).let { file ->
                if (file == null) return@flow
                file.inputStream().use {
                    json.decodeFromStream<List<Section>>(it) .forEach { section ->
                        emit(section)
                    }
                }
            }
        }
    }

    private fun sectionFiles(book: Book): List<File> {
        return context.filesDir.listFiles()
            ?.filter { it.name.startsWith("${book.ISBN}_") }
            ?.sortedBy { it.name.removePrefix("${book.ISBN}_").removeSuffix(".json").toIntOrNull() ?: 0 }
            ?: emptyList()
    }


}