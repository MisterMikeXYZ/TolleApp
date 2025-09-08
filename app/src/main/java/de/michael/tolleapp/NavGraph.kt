import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.michael.tolleapp.Route
import de.michael.tolleapp.Route.SkyjoEnd
import de.michael.tolleapp.presentation.dart.DartStartScreen
import de.michael.tolleapp.presentation.dart.DartViewModel
import de.michael.tolleapp.presentation.skyjo.SkyjoEndScreen
import de.michael.tolleapp.presentation.skyjo.SkyjoGameScreen
import de.michael.tolleapp.presentation.skyjo.SkyjoStartScreen
import de.michael.tolleapp.presentation.skyjo.SkyjoViewModel
import de.michael.tolleapp.presentation.main.MainScreen
import de.michael.tolleapp.presentation.schwimmen.SchwimmenGameScreenCanvas
import de.michael.tolleapp.presentation.schwimmen.SchwimmenGameScreenCircle
import de.michael.tolleapp.presentation.schwimmen.SchwimmenStartScreen
import de.michael.tolleapp.presentation.schwimmen.SchwimmenViewModel
import de.michael.tolleapp.presentation.settings.SettingsScreen
import de.michael.tolleapp.presentation.settings.SettingsViewModel
import de.michael.tolleapp.presentation.settings.screens.PlayerDeleteScreen
import de.michael.tolleapp.presentation.statistics.StatScreen
import de.michael.tolleapp.presentation.statistics.StatViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    pad : PaddingValues,
) {
    // Get the ViewModel
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
            SkyjoStartScreen(
                navigateTo = { route ->
                    navController.navigate(route)
                },
                navigateBack = { navController.popBackStack() },
                viewModel = skyjoViewModel
            )
        }

        // Skyjo Game Screen
        composable<Route.SkyjoGame> {
            SkyjoGameScreen(
                navigateTo = { route ->
                    navController.navigate(route)
                },
                viewModel = skyjoViewModel,
            )
        }

        // Skyjo End Game Screen
        composable<SkyjoEnd> {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Skyjo") },
                    )
                }
            ) { innerPadding ->
                SkyjoEndScreen(
                    navigateTo = { route ->
                        navController.navigate(route)
                    },
                    modifier = Modifier.padding(innerPadding),
                    viewModel = skyjoViewModel,

                    )
            }
        }

        // Statistics Screen
        composable<Route.Statistics> {
            StatScreen(
                navigateTo = { route ->
                    navController.navigate(route)
                },
                //viewModel = statViewModel
            )
        }

        // Settings Screen
        composable <Route.Settings> {
             SettingsScreen(
                 viewModel = settingsViewModel,
                 navigateBack = { navController.popBackStack() },
                 navigateTo = { route ->
                     navController.navigate(route)
                 },
             )
        }

        // Player Delete Settings
        composable <Route.PlayerDeleteScreen> {
            PlayerDeleteScreen(
                viewModel = settingsViewModel,
                navigateBack = { navController.popBackStack() },
            )
        }

        // Schwimmen Startscreen
        composable<Route.Schwimmen> {
             SchwimmenStartScreen(
                 navigateTo = { route ->
                     navController.navigate(route)
                 },
                 navigateBack = { navController.popBackStack() },
                 viewModel = schwimmenViewModel
             )
        }

        // Schwimmen Game Screen with the boats
        composable<Route.SchwimmenGameScreenCanvas> {
             SchwimmenGameScreenCanvas(
                 viewModel = schwimmenViewModel,
                 navigateTo = { route ->
                     navController.navigate(route)
                 }
             )
         }

        // Schwimmen Game Screen with a big circle
        composable<Route.SchwimmenGameScreenCircle> {
            SchwimmenGameScreenCircle(
                viewModel = schwimmenViewModel,
                navigateTo = { route ->
                    navController.navigate(route)
                }
            )
        }

        // Dart Startscreen
        composable<Route.DartStartScreen> {
            DartStartScreen(
                viewModel = dartViewModel,
                navigateTo = { route ->
                    navController.navigate(route)
                },
                navigateBack = { navController.popBackStack() },
            )
        }
    }
}
