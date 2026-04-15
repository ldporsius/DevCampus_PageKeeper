package nl.codingwithlinda.pagekeeper.core.navigation

import kotlinx.coroutines.flow.Flow

interface MenuActionController {

    val actions: Flow<MenuAction>
    suspend fun onAction(action: MenuAction)

}