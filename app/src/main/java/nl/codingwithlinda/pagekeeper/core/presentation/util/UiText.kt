package nl.codingwithlinda.pagekeeper.core.presentation.util

import androidx.annotation.StringRes

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResource(
        @StringRes val id: Int,
        vararg args: Any? = emptyArray()
    ) : UiText
}