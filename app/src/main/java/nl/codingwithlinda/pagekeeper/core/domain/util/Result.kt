package nl.codingwithlinda.pagekeeper.core.domain.util

sealed interface Result<out D, out E : RootError> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Failure<out E : RootError>(val error: E) : Result<Nothing, E>
}

inline fun <T, E: RootError, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when(this) {
        is Result.Failure -> Result.Failure(error)
        is Result.Success -> Result.Success(map(this.data))
    }
}

inline fun <T, E: RootError> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Failure -> this
        is Result.Success -> {
            action(this.data)
            this
        }
    }
}

inline fun <T, E: RootError> Result<T, E>.onFailure(action: (E) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Failure -> {
            action(error)
            this
        }
        is Result.Success -> this
    }
}

fun <T, E: RootError> Result<T, E>.asEmptyResult(): EmptyResult<E> {
    return map {  }
}

typealias EmptyResult<E> = Result<Unit, E>