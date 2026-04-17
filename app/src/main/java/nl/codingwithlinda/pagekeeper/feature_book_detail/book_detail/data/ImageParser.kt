package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.data

import android.content.Context
import android.util.Base64
import androidx.core.net.toUri
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import java.io.File

class ImageParser (
    private val context: Context
) {

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


    private fun saveImageToStorage(
        href: String,
        imageMap: Map<String, ByteArray>,
        book: Book
    ): String? {
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

}