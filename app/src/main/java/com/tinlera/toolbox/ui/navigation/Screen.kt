package com.tinlera.toolbox.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object Tools : Screen("tools", "Araçlar", Icons.Filled.Build)
    data object Tweaks : Screen("tweaks", "Tweaks", Icons.Filled.Tune)
    data object About : Screen("about", "Hakkında", Icons.Filled.Info)

    companion object {
        val items = listOf(Home, Tools, Tweaks, About)
    }
}
