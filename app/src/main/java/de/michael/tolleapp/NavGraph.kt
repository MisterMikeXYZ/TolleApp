import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.michael.tolleapp.Route
import de.michael.tolleapp.games.dart.presentation.DartGameScreen
import de.michael.tolleapp.games.dart.presentation.DartStartScreen
import de.michael.tolleapp.games.dart.presentation.DartViewModel
import de.michael.tolleapp.games.schwimmen.presentation.SchwimmenGameScreenCanvas
import de.michael.tolleapp.games.schwimmen.presentation.SchwimmenGameScreenCircle
import de.michael.tolleapp.games.schwimmen.presentation.SchwimmenStartScreen
import de.michael.tolleapp.games.schwimmen.presentation.SchwimmenViewModel
import de.michael.tolleapp.games.skyjo.presentation.SkyjoEndScreen
import de.michael.tolleapp.games.skyjo.presentation.SkyjoGameScreen
import de.michael.tolleapp.games.skyjo.presentation.SkyjoStartScreen
import de.michael.tolleapp.games.skyjo.presentation.SkyjoViewModel
import de.michael.tolleapp.main.MainScreen
import de.michael.tolleapp.settings.presentation.settings.SettingsScreen
import de.michael.tolleapp.settings.presentation.settings.SettingsViewModel
import de.michael.tolleapp.settings.presentation.settings.screens.PlayerDeleteScreen
import de.michael.tolleapp.statistics.StatScreen
import de.michael.tolleapp.statistics.StatViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
) {
    val skyjoViewModel = koinViewModel<SkyjoViewModel>()
    val statViewModel = koinViewModel<StatViewModel>()
    val schwimmenViewModel = koinViewModel<SchwimmenViewModel>()
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val dartViewModel = koinViewModel<DartViewModel>()

    NavHost(
        navController = navController,
        startDestination = Route.Main,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        // --- Main ---
        composable<Route.Main> {
            MainScreen(
                navigateTo = { route ->
                    navController.navigate(route)
                }
            )
        }

        // --- Skyjo ---
        composable<Route.Skyjo> {
            SkyjoStartScreen(
                navigateTo = { route ->
                    navController.navigate(route)
                },
                navigateBack = { navController.popBackStack() },
                viewModel = skyjoViewModel
            )
        }
        composable<Route.SkyjoGame> {
            SkyjoGameScreen(
                navigateTo = { route ->
                    navController.navigate(route)
                },
                viewModel = skyjoViewModel,
            )
        }
        composable<Route.SkyjoEnd> {
            SkyjoEndScreen(
                navigateTo = { route ->
                    navController.navigate(route)
                },
                viewModel = skyjoViewModel,
            )
        }

        // --- Statistics ---
        composable<Route.Statistics> {
            StatScreen(
                navigateTo = { route ->
                    navController.navigate(route)
                },
                viewModel = statViewModel
            )
        }

        // --- Settings ---
        composable <Route.Settings> {
             SettingsScreen(
                 viewModel = settingsViewModel,
                 navigateBack = { navController.popBackStack() },
                 navigateTo = { route ->
                     navController.navigate(route)
                 },
             )
        }
        composable <Route.PlayerDeleteScreen> {
            PlayerDeleteScreen(
                viewModel = settingsViewModel,
                navigateBack = { navController.popBackStack() },
            )
        }

        // --- Schwimmen ---
        composable<Route.Schwimmen> {
             SchwimmenStartScreen(
                 navigateTo = { route ->
                     navController.navigate(route)
                 },
                 navigateBack = { navController.popBackStack() },
                 viewModel = schwimmenViewModel
             )
        }
        composable<Route.SchwimmenGameScreenCanvas> {
             SchwimmenGameScreenCanvas(
                 viewModel = schwimmenViewModel,
                 navigateTo = { route ->
                     navController.navigate(route)
                 }
             )
         }
        composable<Route.SchwimmenGameScreenCircle> {
            SchwimmenGameScreenCircle(
                viewModel = schwimmenViewModel,
                navigateTo = { route ->
                    navController.navigate(route)
                }
            )
        }

        // --- Dart ---
        composable<Route.DartStartScreen> {
            DartStartScreen(
                viewModel = dartViewModel,
                navigateTo = { route ->
                    navController.navigate(route)
                },
                navigateBack = { navController.popBackStack() },
            )
        }
        composable<Route.DartGameScreen> {
            DartGameScreen(
                viewModel = dartViewModel,
                navigateTo = { route ->
                    navController.navigate(route) }
            )
        }
    }
}
