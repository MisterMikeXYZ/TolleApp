package de.michael.tolleapp.main

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.R
import de.michael.tolleapp.Route
import de.michael.tolleapp.main.components.GameCard
import de.michael.tolleapp.main.util.gameNavigationDataList
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel(),
    navigateTo : (Route) -> Unit,
) {
    val content = LocalContext.current

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
                    var counter by remember { mutableIntStateOf(0) }
                    LaunchedEffect(counter) {
                        if (counter == 1) {
                            delay(3000)
                            counter = 0
                        }
                    }
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_round),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(48.dp)
                            .clickable(true) {
                                if (counter++ >= 20) {
                                    counter = 0
                                    Toast
                                        .makeText(
                                            content,
                                            "Such dir nen Leben, du Opfer",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                            }
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
