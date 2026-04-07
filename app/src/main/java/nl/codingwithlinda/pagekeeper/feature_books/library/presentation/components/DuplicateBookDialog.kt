package nl.codingwithlinda.pagekeeper.feature_books.library.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import nl.codingwithlinda.pagekeeper.core.domain.model.Book

@Composable
fun DuplicateBookDialog(
    existing: Book,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Book already in library") },
        text = { Text("\"${existing.title}\" by ${existing.author} is already in your library. Replace it?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Replace")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}