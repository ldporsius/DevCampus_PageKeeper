package nl.codingwithlinda.pagekeeper.core.presentation

import androidx.annotation.DrawableRes

data class NavItem(
    val label: String,
    @DrawableRes val iconRes: Int,
    val action: MenuAction
)