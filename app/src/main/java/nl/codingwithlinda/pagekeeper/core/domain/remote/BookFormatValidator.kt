package nl.codingwithlinda.pagekeeper.core.domain.remote

interface BookFormatValidator {
    fun isSupportedFormat(uri: String): Boolean
}