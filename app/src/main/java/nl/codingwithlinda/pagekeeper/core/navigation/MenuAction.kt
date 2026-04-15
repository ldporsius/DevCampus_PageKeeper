package nl.codingwithlinda.pagekeeper.core.navigation

interface MenuAction

object ImportBookMenuAction : MenuAction

data class NavigationMenuAction(
    val destination: Destination,
): MenuAction
