package de.michael.tolleapp

import NavGraph
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import de.michael.tolleapp.di.appModule
import de.michael.tolleapp.settings.presentation.SettingsState
import de.michael.tolleapp.settings.presentation.SettingsViewModel
import de.michael.tolleapp.ui.theme.AppTheme
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        startKoin {
            androidContext(this@MainActivity)
            modules(appModule)
        }
        setContent {
            val settingsViewModel: SettingsViewModel = getViewModel()
            val settingsState by settingsViewModel.state.collectAsState(initial = SettingsState())
            AppTheme(darkTheme = settingsState.isDarkmode || isSystemInDarkTheme()) { NavGraph() }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}