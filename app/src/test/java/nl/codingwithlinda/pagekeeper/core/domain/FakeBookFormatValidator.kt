package nl.codingwithlinda.pagekeeper.core.domain

import nl.codingwithlinda.pagekeeper.core.domain.remote.BookFormatValidator

class FakeBookFormatValidator(private val supported: Boolean) : BookFormatValidator {
    override fun isSupportedFormat(uri: String): Boolean = supported
}