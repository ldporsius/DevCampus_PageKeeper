package nl.codingwithlinda.pagekeeper.core.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.codingwithlinda.pagekeeper.core.data.util.scaleCoverBitmapTo
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookParser
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import java.io.StringReader
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.uuid.Uuid


private const val MAX_COVER_PX = 200

class FN2BookParser(
    private val context: Context
): BookParser {

    override suspend fun fetch(uri: String): Book? {
        println("FN2BookParser.fetch: $uri")
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri.toUri())?.use { stream ->
                    val bytes = stream.readBytes()
                    val content = bytes.decodeToString()
                    if (!isValidFb2(content)) return@withContext null
                    val book = parseContent(content) ?: return@withContext null
                    File(context.filesDir, "${book.ISBN}.fb2").writeBytes(bytes)
                    book
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // Validates that content has the same structural markers our parsing logic requires,
    // as established by the assets dummy book.fb2.
    private fun isValidFb2(content: String): Boolean {
        val requiredMarkers = listOf("<FictionBook", "<title-info>", "<book-title>", "</body>")
        if (requiredMarkers.any { it !in content }) return false
        if (content.split("</body>").size < 2) return false
        return try {
            val descriptionXml = content.split("</body>")[0] + "</body></FictionBook>"
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(descriptionXml.byteInputStream())
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun parseContent(content: String): Book? {
        val split = content.split("</body>")
        val body = split[0] + "</body></FictionBook>"
        val metaData = docBuilder(inputStream = body.byteInputStream(), bodyText = split[0])

        var coverSaved = false
        split[1].split("</binary>").map { "$it</binary>" }.asSequence().forEach { string ->
            if ("<binary" !in string) return@forEach

            val (imgRef, imgData) = extractImageString(string)
            if (imgRef == 0) {
                val saved = parseImage(imgData)
                    ?.let { scaleCoverBitmap(it, MAX_COVER_PX) }
                    ?.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        File(context.filesDir, "${metaData.isbn}.png").outputStream()
                    ) ?: false
                coverSaved = saved
                return@forEach
            }
        }

        val imgUrl = if (coverSaved) {
            File(context.filesDir, "${metaData.isbn}.png").toUri().toString()
        } else ""

        return Book(
            ISBN = metaData.isbn,
            title = metaData.title,
            author = metaData.authors.joinToString(", "),
            imgUrl = imgUrl,
            dateCreated = System.currentTimeMillis()
        )
    }

    private fun extractImageString(xml: String): Pair<Int,ByteArray>{
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val xpp = factory.newPullParser()

        xpp.setInput(StringReader(xml))
        var eventType = xpp.eventType
        var imgRef: Int = -1
        while (eventType != XmlPullParser.END_DOCUMENT) {

            when (eventType) {
                XmlPullParser.START_TAG-> {
                    println("start: ${xpp.name}, xpp.attributeCount: ${xpp.attributeCount}")
                    for (i in 0 until xpp.attributeCount){
                        println("attribute: ${xpp.getAttributeName(i)} ${xpp.getAttributeValue(i)}")
                        if (xpp.getAttributeName(i) == "id"){
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

    private fun parseImage(binary: ByteArray) : Bitmap?{
        return try {
            BitmapFactory.decodeByteArray(binary, 0, binary.size)
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
    }

    private fun scaleCoverBitmap(original: Bitmap, maxPx: Int): Bitmap =
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
                val last  = authorEl.getElementsByTagName("last-name").item(0)?.textContent?.trim() ?: ""
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
        val hasIsbn = isbnPattern
            .find(bodyText)?.groupValues?.getOrNull(1)

        val uuid = uuidPattern
            .find(bodyText)?.groupValues?.getOrNull(1)

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
