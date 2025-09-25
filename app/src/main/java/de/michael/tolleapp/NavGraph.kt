import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import de.michael.tolleapp.Route
import de.michael.tolleapp.games.dart.presentation.DartGameScreen
import de.michael.tolleapp.games.dart.presentation.DartStartScreen
import de.michael.tolleapp.games.dart.presentation.DartViewModel
import de.michael.tolleapp.games.randomizer.presentation.RandomizerScreen
import de.michael.tolleapp.games.randomizer.presentation.RandomizerViewModel
import de.michael.tolleapp.games.schwimmen.presentation.SchwimmenGameScreenCanvas
import de.michael.tolleapp.games.schwimmen.presentation.SchwimmenGameScreenCircle
import de.michael.tolleapp.games.schwimmen.presentation.SchwimmenStartScreen
import de.michael.tolleapp.games.schwimmen.presentation.SchwimmenViewModel
import de.michael.tolleapp.games.skyjo.presentation.SkyjoEndScreen
import de.michael.tolleapp.games.skyjo.presentation.SkyjoGameScreen
import de.michael.tolleapp.games.skyjo.presentation.SkyjoStartScreen
import de.michael.tolleapp.games.skyjo.presentation.SkyjoViewModel
import de.michael.tolleapp.games.util.startScreen.StartAction
import de.michael.tolleapp.games.util.startScreen.StartGameScreen
import de.michael.tolleapp.games.wizard.presentation.WizardAction
import de.michael.tolleapp.games.wizard.presentation.WizardEndScreen
import de.michael.tolleapp.games.wizard.presentation.WizardGameScreen
import de.michael.tolleapp.games.wizard.presentation.WizardViewModel
import de.michael.tolleapp.games.wizard.presentation.toStartState
import de.michael.tolleapp.main.MainScreen
import de.michael.tolleapp.settings.presentation.SettingsScreen
import de.michael.tolleapp.settings.presentation.SettingsViewModel
import de.michael.tolleapp.settings.presentation.screens.PlayerDeleteScreen
import de.michael.tolleapp.statistics.StatScreen
import de.michael.tolleapp.statistics.StatViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
) {
    val statViewModel = koinViewModel<StatViewModel>()
    val settingsViewModel = koinViewModel<SettingsViewModel>()

    NavHost(
        navController = navController,
        startDestination = Route.BeforeNav,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        navigation<Route.BeforeNav>(
            startDestination = Route.Before.StartScreen,
        ) {
            // --- Start aka Main ---
            composable<Route.Before.StartScreen> {
                MainScreen(
                    navigateTo = { route ->
                        navController.navigate(route)
                    }
                )
            }

            // --- Statistics ---
            composable<Route.Before.Statistics> {
                StatScreen(
                    navigateTo = { route ->
                        navController.navigate(route)
                    },
                    viewModel = statViewModel
                )
            }

            // --- Settings ---
            composable <Route.Before.Settings> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    navigateBack = { navController.popBackStack() },
                    navigateTo = { route ->
                        navController.navigate(route)
                    },
                )
            }
            composable <Route.Before.PlayerDeleteScreen> {
                PlayerDeleteScreen(
                    viewModel = settingsViewModel,
                    navigateBack = { navController.popBackStack() },
                )
            }
        }
        navigation<Route.SkyjoNav>(
            startDestination = Route.Skyjo.Start
        ) {
            composable<Route.Skyjo.Start> {
                val viewModel = it.sharedViewModel<SkyjoViewModel>(navController)
                SkyjoStartScreen(
                    navigateToGame = { navController.navigate(Route.Skyjo.Game) },
                    navigateToMainMenu = { navController.navigateWithPop<Route.SkyjoNav>(Route.BeforeNav) },
                    viewModel = viewModel
                )
            }
            composable<Route.Skyjo.Game> {
                val viewModel = it.sharedViewModel<SkyjoViewModel>(navController)
                SkyjoGameScreen(
                    viewModel = viewModel,
                    navigateToMainMenu = { navController.navigateWithPop<Route.SkyjoNav>(Route.BeforeNav) },
                    navigateToEnd = { navController.navigate(Route.Skyjo.End) },
                )
            }
            composable<Route.Skyjo.End> {
                val viewModel = it.sharedViewModel<SkyjoViewModel>(navController)
                SkyjoEndScreen(
                    navigateToGameScreen = { navController.navigate(Route.Skyjo.Game) },
                    navigateToMainMenu = { navController.navigateWithPop<Route.SkyjoNav>(Route.BeforeNav) },
                    viewModel = viewModel,
                )
            }
        }
        navigation<Route.SchwimmenNav>(
            startDestination = Route.Schwimmen.Start
        ) {
            composable<Route.Schwimmen.Start> {
                val viewModel = it.sharedViewModel<SchwimmenViewModel>(navController)
                SchwimmenStartScreen(
                    navigateToGame = { canvas -> navController.navigate(Route.Schwimmen.Game(canvas)) },
                    navigateBack = { navController.navigateWithPop<Route.SchwimmenNav>(Route.BeforeNav) },
                    viewModel = viewModel
                )
            }
            composable<Route.Schwimmen.Game> {
                val canvas = it.toRoute<Route.Schwimmen.Game>().canvas
                val viewModel = it.sharedViewModel<SchwimmenViewModel>(navController)

                if (canvas) SchwimmenGameScreenCanvas(
                    viewModel = viewModel,
                    navigateToMainMenu = { navController.navigateWithPop<Route.SchwimmenNav>(Route.BeforeNav) },
                ) else SchwimmenGameScreenCircle(
                    viewModel = viewModel,
                    navigateToMainMenu = { navController.navigateWithPop<Route.SchwimmenNav>(Route.BeforeNav) },
                )
            }
        }
        navigation<Route.DartNav>(
            startDestination = Route.Dart.Start
        ) {
            composable<Route.Dart.Start> {
                val viewModel = it.sharedViewModel<DartViewModel>(navController)
                DartStartScreen(
                    viewModel = viewModel,
                    navigateToGameScreen = { navController.navigate(Route.Dart.Game) },
                    navigateToMainMenu = { navController.navigateWithPop<Route.DartNav>(Route.BeforeNav) },
                )
            }
            composable<Route.Dart.Game> {
                val viewModel = it.sharedViewModel<DartViewModel>(navController)
                DartGameScreen(
                    viewModel = viewModel,
                    navigateToMainMenu = { navController.navigateWithPop<Route.DartNav>(Route.BeforeNav) }
                )
            }
        }
        navigation<Route.RandomizerNav>(
            startDestination = Route.Randomizer.Start,
        ) {
            composable<Route.Randomizer.Start> {
                val viewModel = it.sharedViewModel<RandomizerViewModel>(navController)
                RandomizerScreen(
                    viewModel = viewModel,
                    navigateToMainMenu = { navController.navigateWithPop<Route.RandomizerNav>(Route.BeforeNav) }
                )
            }
        }
        navigation<Route.WizardNav>(
            startDestination = Route.Wizard.Start
        ) {
            composable<Route.Wizard.Start> {
                val viewModel = it.sharedViewModel<WizardViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()
                StartGameScreen(
                    minPlayers = 3,
                    maxPlayers = 6,
                    state = state.toStartState(),
                    onAction = { action ->
                        when (action) {
                            StartAction.NavigateToMainMenu -> navController.navigateWithPop<Route.WizardNav>(Route.BeforeNav)
                            StartAction.NavigateToGame -> navController.navigate(Route.Wizard.Game)
                            else -> viewModel.onStartAction(action)
                        }
                    }
                )
            }
            composable<Route.Wizard.Game> {
                val viewModel = it.sharedViewModel<WizardViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()
                WizardGameScreen(
                    state = state,
                    onAction = { action ->
                        when (action) {
                            WizardAction.NavigateToMainMenu -> navController.navigateWithPop<Route.WizardNav>(Route.BeforeNav)
                            WizardAction.OnGameFinished -> {
                                navController.navigate(Route.Wizard.End)
                            }
                            else -> viewModel.onAction(action)
                        }
                    }
                )
            }
            composable<Route.Wizard.End> {
                val viewModel = it.sharedViewModel<WizardViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()
                WizardEndScreen(
                    state = state,
                    navigateToMainMenu = { navController.navigateWithPop<Route.WizardNav>(Route.BeforeNav) },
                )
            }
        }
    }
}

inline fun <reified T: Route> NavController.navigateWithPop(route: Route) {
    this.navigate(route) {
        popUpTo(T::class) {
            inclusive = true
        }
    }
}

@Composable
inline fun <reified T: ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return koinViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return koinViewModel(viewModelStoreOwner = parentEntry)
}