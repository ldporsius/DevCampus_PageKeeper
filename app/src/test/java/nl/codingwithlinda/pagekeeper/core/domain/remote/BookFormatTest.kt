package nl.codingwithlinda.pagekeeper.core.domain.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookFormatTest {

    @Test
    fun supportedExtensions_containsFb2() {
        assertTrue(BookFormat.supportedExtensions.contains("fb2"))
    }

    @Test
    fun supportedExtensions_doesNotContainPdf() {
        assertFalse(BookFormat.supportedExtensions.contains("pdf"))
    }

    @Test
    fun supportedExtensions_doesNotContainEpub() {
        assertFalse(BookFormat.supportedExtensions.contains("epub"))
    }

    @Test
    fun allMimeTypes_containsFb2MimeTypes() {
        val mimeTypes = BookFormat.allMimeTypes.toList()
        assertTrue(mimeTypes.contains("application/x-fictionbook+xml"))
        assertTrue(mimeTypes.contains("application/x-fictionbook"))
    }

    @Test
    fun fb2_extensionValue_isCorrect() {
        assertEquals("fb2", BookFormat.FB2.extension)
    }

}