package nl.codingwithlinda.pagekeeper.core.presentation.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResource(
        @StringRes val id: Int,
        vararg val args: Any? = emptyArray()
    ) : UiText
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.StringResource -> stringResource(id, *args.filterNotNull().toTypedArray())
}