package nl.codingwithlinda.pagekeeper.core.data.util

import nl.codingwithlinda.pagekeeper.core.domain.util.Logger

class SimpleLogger: Logger {
    private val TAG = "---SimpleLogger---"
    override fun log(message: String) {
        println("$TAG: $message")
    }
}