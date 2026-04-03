package nl.codingwithlinda.pagekeeper.core.data.remote

import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import android.provider.OpenableColumns
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class ContentResolverBookFormatValidatorTest {

    private fun validatorWithDisplayName(displayName: String): ContentResolverBookFormatValidator {
        val cursor = MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME)).apply {
            addRow(arrayOf(displayName))
        }
        val mockResolver = mockk<ContentResolver> {
            every { query(any(), any(), null, null, null) } returns cursor
        }
        val mockContext = mockk<Context> {
            every { contentResolver } returns mockResolver
        }
        return ContentResolverBookFormatValidator(mockContext)
    }

    @Test
    fun fb2File_isSupportedFormat() {
        assertThat(validatorWithDisplayName("my_book.fb2").isSupportedFormat("content://test/book")).isTrue()
    }

    @Test
    fun fb2File_uppercaseExtension_isSupportedFormat() {
        assertThat(validatorWithDisplayName("MY_BOOK.FB2").isSupportedFormat("content://test/book")).isTrue()
    }

    @Test
    fun pdfFile_isNotSupportedFormat() {
        assertThat(validatorWithDisplayName("document.pdf").isSupportedFormat("content://test/book")).isFalse()
    }

    @Test
    fun epubFile_isNotSupportedFormat() {
        assertThat(validatorWithDisplayName("novel.epub").isSupportedFormat("content://test/book")).isFalse()
    }

    @Test
    fun fileWithNoExtension_isNotSupportedFormat() {
        assertThat(validatorWithDisplayName("bookfile").isSupportedFormat("content://test/book")).isFalse()
    }
}