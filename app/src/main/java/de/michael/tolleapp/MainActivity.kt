package de.michael.tolleapp

import NavGraph
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.michael.tolleapp.di.appModule
import de.michael.tolleapp.presentation.settings.SettingsState
import de.michael.tolleapp.presentation.settings.SettingsViewModel
import de.michael.tolleapp.ui.theme.TolleAppTheme
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.context.startKoin
import androidx.compose.runtime.getValue

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
            TolleAppTheme(darkTheme = settingsState.isDarkmode) {
                Scaffold (modifier = Modifier.statusBarsPadding()){ pad ->
                    NavGraph(pad = pad)
                }
            }
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TolleAppTheme {
        Greeting("Android")
    }
}