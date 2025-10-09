package de.michael.tolleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.michael.tolleapp.settings.presentation.SettingsState
import de.michael.tolleapp.settings.presentation.SettingsViewModel
import de.michael.tolleapp.ui.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val settingsState by settingsViewModel.state.collectAsState(initial = SettingsState())

            AppTheme(darkTheme = settingsState.isDarkmode || isSystemInDarkTheme()) {
                NavGraph()
            }
        }
    }
}
