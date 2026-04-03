package nl.codingwithlinda.pagekeeper.core.presentation

sealed interface MenuAction {
    class ImportBookAction(private val onExecute: () -> Unit) : MenuAction {
        override fun execute() = onExecute()
    }
    class LibraryAction(private val onExecute: () -> Unit) : MenuAction {
        override fun execute() = onExecute()
    }
    class FavoritesAction(private val onExecute: () -> Unit) : MenuAction {
        override fun execute() = onExecute()
    }
    class FinishedAction(private val onExecute: () -> Unit) : MenuAction {
        override fun execute() = onExecute()
    }

    fun execute()
}