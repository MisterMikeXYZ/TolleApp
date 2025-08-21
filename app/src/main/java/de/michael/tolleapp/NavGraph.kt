import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import de.michael.tolleapp.presentation.app1.SkyjoEndScreen
import de.michael.tolleapp.presentation.app1.SkyjoGameScreen
import de.michael.tolleapp.presentation.app1.SkyjoScreen
import de.michael.tolleapp.presentation.app1.SkyjoViewModel
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
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Skyjo") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                SkyjoScreen(
                    navigateTo = { route ->
                        navController.navigate(route)
                    },
                    modifier = Modifier.padding(innerPadding),
                    viewModel = skyjoViewModel
                )
            }
        }

        // Skyjo Game Screen
        composable<Route.SkyjoGame> {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Skyjo") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                SkyjoGameScreen(
                    navigateTo = { route ->
                        navController.navigate(route)
                    },
                    modifier = Modifier.padding(innerPadding),
                    viewModel = skyjoViewModel,

                )
            }
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
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("Statistik") },
                            navigationIcon = {
                                IconButton(onClick = { navController.navigate(Route.Main) }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    StatScreen(
                        navigateTo = { route ->
                            navController.navigate(route)
                        },
                        modifier = Modifier.padding(innerPadding),
                        viewModel = statViewModel
                    )
                }
            }
        }
    }
