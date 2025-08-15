package de.michael.tolleapp

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.michael.tolleapp.presentation.app1.SkyjoScreen
import de.michael.tolleapp.presentation.main.MainScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    pad : PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Route.Main,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        modifier = Modifier.padding(pad)
    ) {
        // Main Screen
        composable<Route.Main> {
            MainScreen(
                navigateTo = { route ->
                    navController.navigate(route)
                }
            )
        }

        // Skyjo Screen
        composable<Route.Skyjo> {
            SkyjoScreen(
                navigateTo = { route ->
                    navController.navigate(route)
                }
            )
        }
    }
}