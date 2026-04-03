package nl.codingwithlinda.pagekeeper.core.presentation

class DefaultMenuActionController: MenuActionController {
    override fun onAction(action: MenuAction) {
        action.execute()
    }
}