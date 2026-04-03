package nl.codingwithlinda.pagekeeper.core.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext

class DefaultMenuActionController: MenuActionController {

    private val _actions = Channel<MenuAction>()
    override val actions: Flow<MenuAction>
        get() = _actions.receiveAsFlow()

    override suspend fun onAction(action: MenuAction) {
        withContext(Dispatchers.Main.immediate) {
            _actions.send(action)
        }
    }

}