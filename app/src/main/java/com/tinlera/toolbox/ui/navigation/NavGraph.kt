package com.tinlera.toolbox.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tinlera.toolbox.ui.screens.AboutScreen
import com.tinlera.toolbox.ui.screens.HomeScreen
import com.tinlera.toolbox.ui.screens.ToolsScreen
import com.tinlera.toolbox.ui.screens.TweaksScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) { HomeScreen() }
        composable(Screen.Tools.route) { ToolsScreen() }
        composable(Screen.Tweaks.route) { TweaksScreen() }
        composable(Screen.About.route) { AboutScreen() }
    }
}
