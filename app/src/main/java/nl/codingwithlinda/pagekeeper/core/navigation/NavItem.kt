package nl.codingwithlinda.pagekeeper.core.navigation

import androidx.annotation.DrawableRes

data class NavItem(
    val label: String,
    @DrawableRes val iconRes: Int,
    val action: () -> Unit,
    val showBackground: Boolean = false
)