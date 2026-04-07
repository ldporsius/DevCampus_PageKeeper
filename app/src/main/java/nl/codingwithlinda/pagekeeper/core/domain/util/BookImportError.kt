package nl.codingwithlinda.pagekeeper.core.domain.util

sealed interface BookImportError : RootError {
    data object BookIsDuplicate : BookImportError
    data object BookImportOtherError : BookImportError
}