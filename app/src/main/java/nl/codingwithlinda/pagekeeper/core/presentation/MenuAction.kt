package nl.codingwithlinda.pagekeeper.core.presentation

import nl.codingwithlinda.pagekeeper.navigation.Destination

interface MenuAction

object ImportBookMenuAction : MenuAction

data class NavigationMenuAction(
    val destination: Destination,
): MenuAction
