package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.util

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import nl.codingwithlinda.pagekeeper.core.domain.util.Logger
import nl.codingwithlinda.pagekeeper.core.domain.util.Result
import nl.codingwithlinda.pagekeeper.core.domain.util.RootError
import nl.codingwithlinda.pagekeeper.core.domain.util.onFailure
import nl.codingwithlinda.pagekeeper.core.domain.util.onSuccess
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.BookParseError

class Paginator<KEY, ITEM>(
    private val initialKey: KEY?=null,
    private val onLoadUpdated: (Boolean) -> Unit,
    private val onRequest: suspend (nextKey: KEY) -> Result<List<ITEM>, BookParseError>,
    private val getNextKey: suspend (List<ITEM>) -> KEY,
    private val onError: suspend (BookParseError?) -> Unit,
    private val onSuccess: suspend (items: List<ITEM>, newKey: KEY) -> Unit,
    private val logger: Logger,
) {
    private var currentKey : KEY? = initialKey
    private var isMakingRequest = false
    private var lastRequestKey: KEY? = null

    fun setInitialKey(key: KEY){
        currentKey = key
        lastRequestKey = null
    }
    suspend fun loadNextItems() {
        if (isMakingRequest) {
            logger.log("--- PAGINATOR EARLY RETURN --- isMakingRequest")
            return
        }

        if (currentKey == null) {
            return
        }

        if (currentKey != null && lastRequestKey == currentKey) {
            logger.log("--- PAGINATOR EARLY RETURN --- currentKey == lastRequestKey. currentKey = $currentKey, lastRequestKey = $lastRequestKey")
            return
        }


        onLoadUpdated(true)

        currentKey?.let {key->
            try {
                isMakingRequest = true
                onRequest(key)
                    .onSuccess { items ->
                        currentKey = getNextKey(items)
                        lastRequestKey = currentKey
                        onSuccess(items, key)
                    }
                    .onFailure { error ->
                        onError(error)
                    }

            } catch (e: Exception) {
                e.printStackTrace()
                currentCoroutineContext().ensureActive()
                onError(BookParseError.GeneralBookParseError)
            } finally {
                onLoadUpdated(false)
                isMakingRequest = false
            }
        }
    }

    fun reset(){
        currentKey = initialKey
        lastRequestKey = null
    }
}