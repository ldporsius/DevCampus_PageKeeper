package nl.codingwithlinda.pagekeeper.core.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.codingwithlinda.pagekeeper.core.domain.model.Book
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookParser
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.StringReader


class FN2BookParser(
    private val context: Context
): BookParser {

    suspend fun fromAssets(): Book? {
        return withContext(Dispatchers.IO) {
            context.assets.open("book.fb2").use { stream ->
                val size = stream.available()
                val buffer = ByteArray(size)
                stream.read(buffer)

                val split = buffer.decodeToString().split("</body>")
                parse(split[0] + "</body>")

                val images = split[1].split("</binary>").map {
                    "$it</binary>"
                }

                images.asSequence().onEach{ string ->
                    val (imgRef, imgData) = extractImageString(string)

                    if (imgRef == 0) {
                        val bm = parseImage(imgData)

                        bm?.compress(
                            Bitmap.CompressFormat.PNG,
                            100,
                            File(context.filesDir, "img$imgRef.png").outputStream()
                        )
                    }
                }

                val imgUrl = File(context.filesDir, "img0.png").toUri().toString()

                Book(
                    ISBN = "1235",
                    title = "TODO()",
                    author = "TODO()",
                    imgUrl = imgUrl,
                    dateCreated = System.currentTimeMillis()
                )
            }
        }
    }
    override suspend fun fetch(uri: String): Book? {
        return try {
            context.contentResolver?.openInputStream(uri.toUri())?.use {
                val size = it.read()
                val outputArray = ByteArray(size)
                it.read(outputArray)

                val file = File(context.filesDir, "book.xml")
                file.writeText(outputArray.decodeToString())

                fromAssets()
            }
        }catch (e: Exception){
            e.printStackTrace()
            fromAssets()
        }
    }

    fun parse(data: String){
        val factory = XmlPullParserFactory.newInstance()
        factory.setNamespaceAware(true)
        val xpp = factory.newPullParser()

        xpp.setInput(StringReader(data))
        var eventType = xpp.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when(eventType){
                XmlPullParser.START_DOCUMENT -> println("Start document")
                XmlPullParser.START_TAG -> {
                    println("Start tag: " + xpp.name + " " + xpp.attributeCount)
                    for (i in 0 until xpp.attributeCount){
                        println("Attribute: " + xpp.getAttributeName(i) + " " + xpp.getAttributeValue(i))
                    }
                }
                XmlPullParser.END_TAG -> println("End tag " + xpp.name )
                XmlPullParser.PROCESSING_INSTRUCTION -> println("PI: " + xpp.name + " " + xpp.text)
                XmlPullParser.TEXT -> println("Text: " + xpp.text)

            }
            eventType = xpp.nextToken()
        }
        println("End document")
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
}