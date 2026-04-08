package nl.codingwithlinda.pagekeeper.core.data.util

// --- ByteArray section helpers ---

/** Decodes bytes from the start up to and including [marker]. Returns null if not found. */
fun ByteArray.sectionUpToAndIncluding(marker: String): String? {
    val markerBytes = marker.toByteArray(Charsets.UTF_8)
    val pos = indexOf(markerBytes)
    if (pos == -1) return null
    return String(this, 0, pos + markerBytes.size, Charsets.UTF_8)
}

/** Decodes bytes from immediately after [marker] to the end. Returns null if not found. */
fun ByteArray.sectionAfter(marker: String): String? {
    val markerBytes = marker.toByteArray(Charsets.UTF_8)
    val pos = indexOf(markerBytes)
    if (pos == -1) return null
    val start = pos + markerBytes.size
    return String(this, start, size - start, Charsets.UTF_8)
}

fun ByteArray.indexOf(pattern: ByteArray, fromIndex: Int = 0): Int {
    outer@ for (i in fromIndex..size - pattern.size) {
        for (j in pattern.indices) {
            if (this[i + j] != pattern[j]) continue@outer
        }
        return i
    }
    return -1
}

/** Decodes bytes between [start] (exclusive) and [end] (exclusive). Returns null if either marker is missing. */
fun ByteArray.sectionBetween(start: String, end: String): String? {
    val startBytes = start.toByteArray(Charsets.UTF_8)
    val contentStart = indexOf(startBytes).takeIf { it != -1 }?.plus(startBytes.size) ?: return null
    val endBytes = end.toByteArray(Charsets.UTF_8)
    val endPos = indexOf(endBytes, contentStart).takeIf { it != -1 } ?: return null
    return String(this, contentStart, endPos - contentStart, Charsets.UTF_8)
}