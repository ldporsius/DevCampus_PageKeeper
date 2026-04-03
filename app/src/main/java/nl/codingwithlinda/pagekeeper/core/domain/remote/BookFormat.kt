package nl.codingwithlinda.pagekeeper.core.domain.remote

enum class BookFormat(
    val extension: String,
    val mimeTypes: List<String>
) {
    FB2(
        extension = "fb2",
        mimeTypes = listOf(
            "application/x-fictionbook+xml",
            "application/x-fictionbook",
        )
    );

    companion object {
        val supportedExtensions: Set<String>
            get() = entries.map { it.extension }.toSet()

        val allMimeTypes: Array<String>
            get() = entries.flatMap { it.mimeTypes }.toTypedArray()
    }
}