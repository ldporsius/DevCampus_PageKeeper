package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import nl.codingwithlinda.pagekeeper.core.data.util.sectionBetween
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Citation
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Epigraph
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.PageElement
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Paragraph
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Section
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.Title
import nl.codingwithlinda.pagekeeper.core.domain.util.Result
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookPager
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookParseError
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.InnerSection
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.cancellation.CancellationException


private val imageRegex = Regex("""<image[^>]+\w+:href="([^"]+)"""")


@Serializable
private data class BookMeta(val totalElements: Int)

private val json = Json { ignoreUnknownKeys = true }

private val pRegex              = Regex("<p>(.*?)</p>",                     RegexOption.DOT_MATCHES_ALL)
private val titleRegex          = Regex("<title>(.*?)</title>",             RegexOption.DOT_MATCHES_ALL)
private val citeRegex           = Regex("<cite[^>]*?>(.*?)</cite>",         RegexOption.DOT_MATCHES_ALL)
private val epigraphRegex       = Regex("<epigraph[^>]*?>(.*?)</epigraph>", RegexOption.DOT_MATCHES_ALL)
private val firstStrongPRegex   = Regex(
    """\s*(?:<empty-line\s*/>\s*)*<p[^>]*?>\s*<strong[^>]*?>(.*?)</strong>\s*</p>""".trimIndent(),
    options = setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
)

private val elementParsers: List<Pair<Regex, (MatchResult) -> PageElement>> = listOf(
    titleRegex        to { m -> Title(text = pRegex.replace(m.groupValues[1], "$1").trim()) },
    firstStrongPRegex to { m -> Title(text = m.groupValues[1].trim()) },
    citeRegex         to { m -> Citation(text = pRegex.replace(m.groupValues[1], "$1").trim()) },
    epigraphRegex     to { m -> Epigraph(text = pRegex.replace(m.groupValues[1], "$1").trim()) },
    pRegex            to { m -> Paragraph(text = m.value) }
)

private fun PageElement.withId(id: Int): PageElement = when (this) {
    is Paragraph -> copy(id = id)
    is Title     -> copy(id = id)
    is Citation  -> copy(id = id)
    is Epigraph  -> copy(id = id)
    is InnerSection -> copy(id = id)
    is Section   -> copy(id = id)
}

internal suspend fun parseInnerSection(
    sectionId: Int,
    body: String,
    idCounter: AtomicInteger,
): InnerSection {
    val nestedSections = findTopLevelSections(body)
    val elements: List<PageElement> = if (nestedSections.isEmpty()) {
        parseElements(body, idCounter)
    } else {
        nestedSections.mapIndexed { index, html ->
            val inner = html.removePrefix("<section>").removeSuffix("</section>")
            parseInnerSection(sectionId * 100 + index, inner, idCounter)
        }
    }
    return InnerSection(id = sectionId, elements = elements)
}

internal suspend fun parseSection(
    sectionId: Int,
    body: String,
    idCounter: AtomicInteger = AtomicInteger(0),
): Section {
    val nestedSections = findTopLevelSections(body)
    val elements: List<PageElement> = if (nestedSections.isEmpty()) {
        parseElements(body, idCounter)
    } else {
        nestedSections.mapIndexed { index, html ->
            val inner = html.removePrefix("<section>").removeSuffix("</section>")
            parseInnerSection(sectionId * 100 + index, inner, idCounter)
        }
    }
    return Section(id = sectionId, elements = elements)
}

internal fun parseElements(body: String, idCounter: AtomicInteger = AtomicInteger(0)): List<PageElement> {
    val candidates = elementParsers
        .flatMap { (regex, factory) -> regex.findAll(body).map { Triple(it.range.first, it.range.last, factory(it)) } }
        .sortedBy { it.first }
    val result = mutableListOf<Triple<Int, Int, PageElement>>()
    for (candidate in candidates) {
        val containedByExisting = result.any { existing -> candidate.first >= existing.first && candidate.second <= existing.second }
        if (!containedByExisting) result += candidate
    }
    return result.map { it.third.withId(idCounter.getAndIncrement()) }
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
class FB2BookPager(
    private val context: Context
): BookPager {

    override suspend fun writePages(uri: String, isbn: String, onProgress: suspend (written: Int, total: Int) -> Unit): Result<Unit, BookParseError> {
        return withContext(Dispatchers.IO) {
            try {
                val uri = Uri.fromFile(File(context.filesDir, "$isbn.fb2")).toString()
                context.contentResolver.openInputStream(uri.toUri())?.use { stream ->
                    val bytes = runInterruptible { stream.readBytes() }
                    val body = bytes.sectionBetween("<body>", "</body>") ?: ""

                    val topLevelSections = findTopLevelSections(body)

                    println("--- FN2 BOOK PAGER FOUND SECTIONS --- ${topLevelSections.size}")

                    val idCounter = AtomicInteger(0)

                    topLevelSections.forEachIndexed { index, html ->
                        println("--- FN2 BOOK PAGER PARSING SECTION --- $index")

                        ensureActive()
                        val inner = html.removePrefix("<section>").removeSuffix("</section>")
                        val section = parseSection(index, inner, idCounter)
                        val file = File(context.filesDir, "${isbn}_$index.json")
                        file.outputStream().use {
                            json.encodeToStream<List<Section>>(listOf(section), it)
                        }
                        onProgress(index + 1, topLevelSections.size)
                    }

                    val metaFile = File(context.filesDir, "${isbn}.meta.json")
                    metaFile.outputStream().use {
                        json.encodeToStream(BookMeta(idCounter.get()), it)
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


    override suspend fun hasPages(isbn: String): Boolean = withContext(Dispatchers.IO) {
        sectionFiles(isbn).isNotEmpty()
    }

    override suspend fun hasElementMeta(isbn: String): Boolean = withContext(Dispatchers.IO) {
        File(context.filesDir, "${isbn}.meta.json").exists()
    }

    override suspend fun countPages(isbn: String): Int = withContext(Dispatchers.IO) {
        sectionFiles(isbn).size
    }

    override suspend fun countElements(isbn: String): Int = withContext(Dispatchers.IO) {
        val metaFile = File(context.filesDir, "${isbn}.meta.json")
        metaFile.inputStream().use { json.decodeFromStream<BookMeta>(it) }.totalElements
    }

    override suspend fun loadSections(isbn: String, sectionIndex: Int): Result<List<Section>, BookParseError> = withContext(Dispatchers.IO) {
       try {
           val pagesRes= sectionFiles(isbn).getOrNull(sectionIndex).let { file ->
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

    override suspend fun loadSection(isbn: String, sectionIndex: Int): Flow<Section> {
        return flow {
            sectionFiles(isbn).getOrNull(sectionIndex).let { file ->
                if (file == null) return@flow
                file.inputStream().use {
                    json.decodeFromStream<List<Section>>(it) .forEach { section ->
                        emit(section)
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun sectionFiles(isbn: String): List<File> {
        return context.filesDir.listFiles()
            ?.filter { it.name.startsWith("${isbn}_") }
            ?.sortedBy { it.name.removePrefix("${isbn}_").removeSuffix(".json").toIntOrNull() ?: 0 }
            ?: emptyList()
    }


}