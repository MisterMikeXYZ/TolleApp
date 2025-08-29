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
import de.michael.tolleapp.presentation.skyjo.SkyjoEndScreen
import de.michael.tolleapp.presentation.skyjo.SkyjoGameScreen
import de.michael.tolleapp.presentation.skyjo.SkyjoScreen
import de.michael.tolleapp.presentation.skyjo.SkyjoViewModel
import de.michael.tolleapp.presentation.main.MainScreen
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
                    viewModel = statViewModel
                )
            }
        }
    }
