package nl.codingwithlinda.pagekeeper.core.presentation

import kotlinx.coroutines.flow.Flow

interface MenuActionController {

    val actions: Flow<MenuAction>
    suspend fun onAction(action: MenuAction)

}