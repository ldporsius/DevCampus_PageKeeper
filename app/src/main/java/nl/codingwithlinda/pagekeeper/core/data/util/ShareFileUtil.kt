package nl.codingwithlinda.pagekeeper.core.data.util

import java.io.File

fun File.toShareFile(cacheDir: File, title: String): File {
    val safeName = title
        .replace(Regex("[^a-zA-Z0-9 ._-]"), "")
        .trim()
        .replace(" ", "_")
        .take(64)
        .ifEmpty { "book" }
    val sharingDir = File(cacheDir, "sharing").also { it.mkdirs() }
    val dest = File(sharingDir, "$safeName.fb2")
    if (exists()) copyTo(dest, overwrite = true)
    return dest
}