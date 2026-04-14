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
import com.tinlera.toolbox.ui.screens.tools.*
import com.tinlera.toolbox.ui.screens.remote.*

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
        composable(Screen.Tools.route) { ToolsScreen(onNavigate = { navController.navigate(it) }) }
        composable(Screen.Remote.route) {
            RemoteConnectScreen(
                onBack = { navController.popBackStack() },
                onConnected = { navController.navigate("remote/dashboard") }
            )
        }
        composable(Screen.Tweaks.route) { TweaksScreen() }
        composable(Screen.About.route) { AboutScreen() }

        // Tool detail screens
        composable("tool/debloat") { DebloatScreen(onBack = { navController.popBackStack() }) }
        composable("tool/art") { ArtScreen(onBack = { navController.popBackStack() }) }
        composable("tool/dns") { DnsScreen(onBack = { navController.popBackStack() }) }
        composable("tool/buildprop") { BuildPropScreen(onBack = { navController.popBackStack() }) }
        composable("tool/intents") { IntentsScreen(onBack = { navController.popBackStack() }) }
        composable("tool/modules") { ModulesScreen(onBack = { navController.popBackStack() }) }

        // Remote control screens
        composable("remote/dashboard") {
            RemoteDashboardScreen(onBack = { navController.popBackStack() })
        }
    }
}
