package nl.codingwithlinda.pagekeeper.core.presentation

import androidx.navigation3.runtime.NavKey

interface MenuAction {
    suspend fun execute()
    fun undo()
}

object ImportBookMenuAction : MenuAction {
    override suspend fun execute() {}
    override fun undo() {}
}

data class NavigationMenuAction(
    val destination: NavKey,
    val navigate: () -> Unit
): MenuAction {
    override suspend fun execute() {
      navigate()
    }
    override fun undo() {

    }
}
