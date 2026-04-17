package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain

import nl.codingwithlinda.pagekeeper.core.domain.util.RootError

sealed interface BookParseError: RootError {
    object GeneralBookParseError : BookParseError
    object OOM : BookParseError
    object NoPagesFound : BookParseError

}