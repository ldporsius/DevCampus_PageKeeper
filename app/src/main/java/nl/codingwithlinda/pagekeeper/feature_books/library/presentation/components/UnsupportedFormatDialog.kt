package nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import nl.codingwithlinda.pagekeeper.core.domain.remote.BookFormat

@Composable
fun UnsupportedFormatDialog(onDismiss: () -> Unit) {
    val supported = BookFormat.entries.filter { it.isSupported }.joinToString { ".${it.extension}" }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unsupported file format") },
        text = { Text("This file format is not supported. Supported formats: $supported.") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}