package nl.codingwithlinda.pagekeeper.core.data.remote

import android.content.Context
import android.provider.OpenableColumns
import androidx.core.net.toUri
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookFormat
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookFormatValidator

class ContentResolverBookFormatValidator(
    private val context: Context
) : BookFormatValidator {

    override fun isSupportedFormat(uri: String): Boolean {
        val displayName = context.contentResolver.query(
            uri.toUri(),
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else null
        } ?: return false

        val extension = displayName.substringAfterLast('.', missingDelimiterValue = "")
        return extension.lowercase() in BookFormat.supportedExtensions
    }
}