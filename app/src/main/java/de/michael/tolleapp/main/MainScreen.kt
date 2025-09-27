package de.michael.tolleapp.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.R
import de.michael.tolleapp.Route
import de.michael.tolleapp.main.components.GameCard
import de.michael.tolleapp.main.util.gameNavigationDataList
import org.koin.compose.viewmodel.koinViewModel

/**
 * MainScreen is the entry point of the app.
 * It displays a simple button to navigate to the [SkyjoScreen][de.michael.tolleapp.skyjo.presentation.SkyjoStartScreen].
 * The MainViewModel is injected using Koin. []
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel(),
    navigateTo : (Route) -> Unit,
) {
    BackHandler {  }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = stringResource(R.string.app_name),
                    color = MaterialTheme.colorScheme.onSurface
                ) },
                navigationIcon = {
                    // App Icon
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_round),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(48.dp)
                    )
                },
                actions = {
                    IconButton(
                        onClick = { navigateTo(Route.Before.Settings) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier.clip(
                    shape = MaterialTheme.shapes.extraLarge.copy(
                        topStart = CornerSize(0.dp),
                        topEnd = CornerSize(0.dp),
                    )
                )
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = spacedBy(8.dp),
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier
                .padding(8.dp)
                .padding(innerPadding)
        ) {
            items(gameNavigationDataList) {
                GameCard(
                    title = it.name,
                    onClick = { navigateTo(it.gameRoute) },
                )
            }
        }
    }
}
