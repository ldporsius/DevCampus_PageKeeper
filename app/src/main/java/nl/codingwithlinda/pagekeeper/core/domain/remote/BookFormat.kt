package nl.codingwithlinda.pagekeeper.core.domain.remote

enum class BookFormat(
    val extension: String,
    val mimeTypes: List<String>,
    val isSupported: Boolean
) {
    FB2(
        extension = "fb2",
        mimeTypes = listOf(
            "application/x-fictionbook+xml",
            "application/x-fictionbook",
        ),
        isSupported = true
    ),
    EPUB(
        extension = "epub",
        mimeTypes = listOf(
        "application/epub+zip",
        "application/x-epub"
    ),
        isSupported = false
    );

    companion object {
        val supportedExtensions: Set<String>
            get() = entries.filter { it.isSupported }.map { it.extension }.toSet()

        val allMimeTypes: Array<String>
            get() = entries.flatMap { it.mimeTypes }.toTypedArray()
    }
}