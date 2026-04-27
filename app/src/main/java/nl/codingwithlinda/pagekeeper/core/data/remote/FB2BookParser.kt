package nl.codingwithlinda.pagekeeper.core.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.core.net.toUri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import nl.codingwithlinda.pagekeeper.core.data.util.scaleCoverBitmapTo
import nl.codingwithlinda.pagekeeper.core.data.util.sectionAfter
import nl.codingwithlinda.pagekeeper.core.data.util.sectionBetween
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookParser
import nl.codingwithlinda.pagekeeper.core.domain.util.BookImportError
import nl.codingwithlinda.pagekeeper.core.domain.util.Result
import java.io.File
import java.io.InputStream
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory


private const val MAX_COVER_PX = 200

class FB2BookParser(
    private val context: Context
) : BookParser {

    override suspend fun fetch(uri: String): Result<Book, BookImportError> {
        println("FN2BookParser.fetch: $uri")
        return withContext(Dispatchers.IO) {
            var fb2File: File? = null
            var pngFile: File? = null
            try {
                context.contentResolver.openInputStream(uri.toUri())?.use { stream ->
                    val bytes = runInterruptible { stream.readBytes() }

                    // Decode only the two sections we actually need — never the body text.
                    // For a 100 MB file this keeps peak String memory in the low MBs.
                    val descriptionSection = bytes.sectionBetween("<description>", "</description>")?.let{ "<description>$it</description>" }
                        ?: return@withContext Result.Failure(BookImportError.BookImportOtherError)
                    val binarySection = bytes.sectionAfter("</body>")
                        ?: return@withContext Result.Failure(BookImportError.BookImportOtherError)

                    if (!isValidFb2(descriptionSection)) return@withContext Result.Failure(BookImportError.BookImportOtherError)

                    val bodySection = bytes.sectionBetween("<body>", "</body>") ?: ""
                    val book  = extractBookMetaData(descriptionSection)

                    runInterruptible {
                        fb2File = File(context.filesDir, "${book.ISBN}.fb2").also { it.writeBytes(bytes) }
                    }
                    val imgUrl = withContext(Dispatchers.Default) {
                        val coverPage = extractCoverPageInfo(descriptionSection)
                        println("coverPage: $coverPage")
                        val coverResult = extractCoverImage(binarySection, coverPage)
                        println("coverResult: $coverResult")
                        when (coverResult) {
                            is Result.Failure -> ""
                            is Result.Success -> {
                                if (coverResult.data.data != null) {
                                    val f = File(context.filesDir, "${book.ISBN}.png")
                                    val os = f.outputStream()
                                    os.use {
                                        coverResult.data.data.compress(
                                            Bitmap.CompressFormat.PNG,
                                            100,
                                            it
                                        )
                                    }

                                    pngFile = f
                                    f.toUri().toString()
                                } else ""
                            }

                        }
                    }

                    Result.Success(book.copy(imgUrl = imgUrl))
                } ?: Result.Failure(BookImportError.BookImportOtherError)
            } catch (e: CancellationException) {
                fb2File?.delete()
                pngFile?.delete()
                throw e
            } catch (e: Exception) {
                fb2File?.delete()
                pngFile?.delete()
                e.printStackTrace()
                Result.Failure(BookImportError.BookImportOtherError)
            }
        }
    }

    // Validates the description section (already stripped of body text).
    // </body> presence is guaranteed by sectionAfter() returning non-null in fetch().
    private fun isValidFb2(descriptionSection: String): Boolean {
        val requiredMarkers = listOf("<author", "<title-info>", "<book-title>",)
        return (requiredMarkers.all { it in descriptionSection })
    }

    private suspend fun extractBookMetaData(descriptionSection: String): Book = withContext(
        Dispatchers.Default){
        val metaData = extractMetadata(
            descriptionSection = descriptionSection
           )

        val book = Book(
            ISBN = metaData.isbn,
            title = metaData.title,
            author = metaData.authors.joinToString(", "),
            imgUrl = "",
            dateCreated = System.currentTimeMillis()
        )

        return@withContext book
    }

    private suspend fun extractCoverPageInfo(section: String): String{

        val hrefRegex = Regex("""<coverpage[\s\S]*?\w+:href="([^"]+)"""")
        val imageRef = hrefRegex.find(section)?.groupValues?.get(1)

        return imageRef ?: "_0"
    }

    private suspend fun extractCoverImage(binarySection: String, givenCoverPage: String = "_0"): Result<BookCover, BookImportError> = withContext(Dispatchers.Default){

        val imageSequence = binarySection.split("</binary>")
            .filter { it.contains("<binary") }
            .map { "$it</binary>" }.asSequence()

        imageSequence.take(2).onEach {
            println("imageSequence: $it")
        }

        val cover: BookCover? = try {
            imageSequence.map {
                extractImageString(it)
            }.firstOrNull { (id, data) ->
                id.filter { it.isDigit() } in givenCoverPage.orEmpty()
            }?.let { (id, data) ->
                val scaled = parseImage(data)?.let { scaleCoverBitmap(it, MAX_COVER_PX) }
                BookCover(id, scaled)
            }
        }catch (e: CancellationException){
            throw e
        }
        catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (cover == null) return@withContext Result.Failure(BookImportError.BookImportOtherError)

        Result.Success(cover)

    }

    private fun extractImageString(xml: String): Pair<String, ByteArray> {
        val binaryTagStart = xml.indexOf("<binary")
        if (binaryTagStart == -1) return "" to ByteArray(0)

        val binaryTag = xml.substring(binaryTagStart)
        val idMatch = Regex("""id\s*=\s*["']([^"']*)["']""").find(binaryTag)
        val imgId = idMatch?.groupValues?.getOrNull(1) ?: ""

        val contentStart = xml.indexOf('>', binaryTagStart) + 1
        val contentEnd = xml.lastIndexOf("</binary>")
        if (contentStart <= binaryTagStart || contentEnd <= contentStart) return imgId to ByteArray(0)

        val base64Text = xml.substring(contentStart, contentEnd).trim()
        if (base64Text.isEmpty()) return imgId to ByteArray(0)

        return try {
            imgId to Base64.decode(base64Text, Base64.DEFAULT)
        } catch (e: Exception) {
            imgId to ByteArray(0)
        }
    }

    private fun parseImage(binary: ByteArray): Bitmap? {
        return try {
            BitmapFactory.decodeByteArray(binary, 0, binary.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun scaleCoverBitmap(original: Bitmap, maxPx: Int): Bitmap =
        scaleCoverBitmapTo(original, maxPx)

    private fun deterministicUuid(
        title: String, author: String, publisher: String, year: String, lang: String
    ): String {
        val name = "$title|$author|$publisher|$year|$lang".lowercase().trim()
        return UUID.nameUUIDFromBytes(name.toByteArray()).toString()
    }



    private fun extractMetadata(descriptionSection: String): Fb2Metadata {
        val doc = DocBuilder(descriptionSection)
        val isbnBare = ""

        // Extract authors from title-info only (not document-info)
        val authors = mutableListOf<String>()
        val titleInfoNode = doc.getElements("title-info").item(0)
        if (titleInfoNode is org.w3c.dom.Element) {
            val authorNodes = titleInfoNode.getElementsByTagName("author")
            for (i in 0 until authorNodes.length) {
                val authorEl = authorNodes.item(i) as? org.w3c.dom.Element ?: continue
                val first = authorEl.getElementsByTagName("first-name").item(0)?.textContent?.trim() ?: ""
                val last = authorEl.getElementsByTagName("last-name").item(0)?.textContent?.trim() ?: ""
                val fullName = "$first $last".trim()
                if (fullName.isNotEmpty()) authors.add(fullName)
            }
        }

        with(doc) {
            val title = getText("book-title") ?: ""
            val genre = getText("genre") ?: ""
            val lang = getText("lang") ?: ""
            val year = getText("year") ?: ""
            val publisher = getText("publisher") ?: ""
            val isbn = deterministicUuid(title, authors.joinToString(","), publisher, year, lang)
                //?: getText("id")?.takeIf { it.isNotBlank() }



            return Fb2Metadata(
                authors = authors,
                title = title,
                isbn = isbn,
                genre = genre,
                lang = lang,
                year = year,
                publisher = publisher
            )
        }
    }

    private fun extractISBN(bodyText: String): String? {

        // Priority 1: explicit ISBN prefix in body text
        val isbnWithPrefix = Regex("""ISBN[:\s-]*([\d][\d\-X]*)""", RegexOption.IGNORE_CASE)
            .find(bodyText)?.groupValues?.getOrNull(1)
        // Priority 2: bare ISBN-13 (starts with 978/979) in body text, no prefix needed
        val isbnBare = isbnWithPrefix
            ?: Regex("""\b(97[89](?:[-\s]?\d){10})\b""").find(bodyText)?.groupValues?.getOrNull(1)

        return isbnBare
    }
}
class DocBuilder(bodyText: String = "") {
    val inputStream: InputStream = bodyText.byteInputStream()
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val doc = builder.parse(inputStream)

    fun getText(tag: String) = doc
        .getElementsByTagName(tag)
        .item(0)
        ?.textContent
        ?.trim()

    fun getElements(tag: String) = doc
        .getElementsByTagName(tag)
}

data class Fb2Metadata(
    val authors: List<String>,
    val title: String,
    val isbn: String,
    val genre: String,
    val lang: String,
    val year: String,
    val publisher: String
)

data class BookCover(
    val id: String = "",
    val data: Bitmap? = null
)