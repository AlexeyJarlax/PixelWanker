package com.pavlovalexey.pavlovAlexeySandbox.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.start.StartScreen
import androidx.annotation.Keep
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist.AppDetailScreen

@Keep
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        startDestination = "start_screen",
        modifier         = modifier
    ) {
        composable("start_screen") {
            StartScreen(
                onAppClick = { packageName ->
                    navController.navigate("app_detail/$packageName")
                }
            )
        }
        composable(
            route = "app_detail/{packageName}",
            arguments = listOf(
                androidx.navigation.navArgument("packageName") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->
            AppDetailScreen(
                navController = navController,
                backStackEntry = backStackEntry
            )
        }
    }
}
