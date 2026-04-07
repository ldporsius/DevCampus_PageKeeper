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
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookParser
import nl.codingwithlinda.pagekeeper.core.domain.util.BookImportError
import nl.codingwithlinda.pagekeeper.core.domain.util.Result
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.StringReader
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory


private const val MAX_COVER_PX = 200

class FN2BookParser(
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
                    val descriptionSection = bytes.sectionUpToAndIncluding("</description>")
                        ?: return@withContext Result.Failure(BookImportError.BookImportOtherError)
                    val binarySection = bytes.sectionAfter("</body>")
                        ?: return@withContext Result.Failure(BookImportError.BookImportOtherError)

                    if (!isValidFb2(descriptionSection)) return@withContext Result.Failure(BookImportError.BookImportOtherError)
                    val (book, coverBytes) = parseContent(descriptionSection, binarySection)

                    fb2File = File(context.filesDir, "${book.ISBN}.fb2").also { it.writeBytes(bytes) }
                    val imgUrl = if (coverBytes != null) {
                        val f = File(context.filesDir, "${book.ISBN}.png").also { it.writeBytes(coverBytes) }
                        pngFile = f
                        f.toUri().toString()
                    } else ""

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
        val requiredMarkers = listOf("<FictionBook", "<title-info>", "<book-title>", "</description>")
        if (requiredMarkers.any { it !in descriptionSection }) return false
        return try {
            val xml = descriptionSection + "</FictionBook>"
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.byteInputStream())
            true
        } catch (e: Exception) {
            false
        }
    }

    // Returns the parsed Book (without imgUrl) and the compressed cover bytes, or null on failure.
    // Does not touch the filesystem.
    private suspend fun parseContent(descriptionSection: String, binarySection: String): Pair<Book, ByteArray?> {
        val metaData = docBuilder(
            inputStream = (descriptionSection + "</FictionBook>").byteInputStream(),
            bodyText = descriptionSection
        )

        var coverBytes: ByteArray? = null
        binarySection.split("</binary>").map { "$it</binary>" }.asSequence().forEach { string ->
            if ("<binary" !in string) return@forEach
            val (imgRef, imgData) = extractImageString(string)
            if (imgRef == 0) {
                coverBytes = parseImage(imgData)
                    ?.let { scaleCoverBitmap(it, MAX_COVER_PX) }
                    ?.let { bitmap ->
                        ByteArrayOutputStream().also { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }.toByteArray()
                    }
                return@forEach
            }
        }

        val book = Book(
            ISBN = metaData.isbn,
            title = metaData.title,
            author = metaData.authors.joinToString(", "),
            imgUrl = "",
            dateCreated = System.currentTimeMillis()
        )

        return book to coverBytes
    }

    // --- ByteArray section helpers ---

    /** Decodes bytes from the start up to and including [marker]. Returns null if not found. */
    private fun ByteArray.sectionUpToAndIncluding(marker: String): String? {
        val markerBytes = marker.toByteArray(Charsets.UTF_8)
        val pos = indexOf(markerBytes)
        if (pos == -1) return null
        return String(this, 0, pos + markerBytes.size, Charsets.UTF_8)
    }

    /** Decodes bytes from immediately after [marker] to the end. Returns null if not found. */
    private fun ByteArray.sectionAfter(marker: String): String? {
        val markerBytes = marker.toByteArray(Charsets.UTF_8)
        val pos = indexOf(markerBytes)
        if (pos == -1) return null
        val start = pos + markerBytes.size
        return String(this, start, size - start, Charsets.UTF_8)
    }

    private fun ByteArray.indexOf(pattern: ByteArray): Int {
        outer@ for (i in 0..size - pattern.size) {
            for (j in pattern.indices) {
                if (this[i + j] != pattern[j]) continue@outer
            }
            return i
        }
        return -1
    }

    // --- parsing helpers (unchanged) ---

    private fun extractImageString(xml: String): Pair<Int, ByteArray> {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val xpp = factory.newPullParser()

        xpp.setInput(StringReader(xml))
        var eventType = xpp.eventType
        var imgRef: Int = -1
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    println("start: ${xpp.name}, xpp.attributeCount: ${xpp.attributeCount}")
                    for (i in 0 until xpp.attributeCount) {
                        println("attribute: ${xpp.getAttributeName(i)} ${xpp.getAttributeValue(i)}")
                        if (xpp.getAttributeName(i) == "id") {
                            imgRef = xpp.getAttributeValue(i).filter { it.isDigit() }.toInt()
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    val imgData = Base64.decode(xpp.text, Base64.DEFAULT)
                    return imgRef to imgData
                }
            }
            eventType = xpp.next()
        }
        return imgRef to ByteArray(0)
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

    private fun docBuilder(inputStream: InputStream, bodyText: String = ""): Fb2Metadata {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(inputStream)

        fun getText(tag: String) = doc
            .getElementsByTagName(tag)
            .item(0)
            ?.textContent
            ?.trim()

        // Extract authors from title-info only (not document-info)
        val authors = mutableListOf<String>()
        val titleInfoNode = doc.getElementsByTagName("title-info").item(0)
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

        val title     = getText("book-title") ?: ""
        val genre     = getText("genre") ?: ""
        val lang      = getText("lang") ?: ""
        val year      = getText("year") ?: ""
        val publisher = getText("publisher") ?: ""

        val isbnPattern = Regex("""ISBN[:\s]*([\d\-X]+)""", RegexOption.IGNORE_CASE)
        val uuidPattern = Regex("""[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}""", RegexOption.IGNORE_CASE)

        // ISBN is not in FB2 metadata tags — extract from body text
        val hasIsbn = isbnPattern.find(bodyText)?.groupValues?.getOrNull(1)
        val uuid = uuidPattern.find(bodyText)?.groupValues?.getOrNull(1)
        val isbn = hasIsbn ?: uuid ?: UUID.randomUUID().toString()

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

data class Fb2Metadata(
    val authors: List<String>,
    val title: String,
    val isbn: String,
    val genre: String,
    val lang: String,
    val year: String,
    val publisher: String
)