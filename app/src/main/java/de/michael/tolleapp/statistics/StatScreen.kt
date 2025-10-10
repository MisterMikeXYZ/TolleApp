@file:Suppress("UNCHECKED_CAST")

package de.michael.tolleapp.statistics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.games.schwimmen.data.stats.SchwimmenStats
import de.michael.tolleapp.games.util.CustomTopBar
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.statistics.gameStats.DartStats
import de.michael.tolleapp.statistics.gameStats.Flip7Stats
import de.michael.tolleapp.statistics.gameStats.SkyjoStats
import de.michael.tolleapp.statistics.screens.DartCompareTable
import de.michael.tolleapp.statistics.screens.PlayerStatsList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun StatScreen(
    viewModel: StatViewModel = koinViewModel(),
    navigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    // Build a (possibly empty) list so UI always renders.
    val players: List<*> = when (state.selectedGame) {
        GameType.SKYJO -> state.playerNames.keys.map { id ->
            state.playersSkyjo.find { it.playerId == id } ?: SkyjoStats(playerId = id)
        }.sortedByDescending { it.roundsPlayed + it.totalGames }

        GameType.SCHWIMMEN -> state.playerNames.keys.map { id ->
            state.playersSchwimmen.find { it.playerId == id } ?: SchwimmenStats(playerId = id)
        }.sortedByDescending { it.roundsPlayedSchwimmen + it.totalGamesPlayedSchwimmen }

        GameType.FLIP7 -> state.playerNames.keys.map { id ->
            state.playersFlip7.find { it.playerId == id } ?: Flip7Stats(playerId = id)
        }.sortedByDescending { it.roundsPlayed + it.totalGames }

        GameType.DART -> state.playerNames.keys.map { id ->
            state.playersDart.find { it.playerId == id } ?: DartStats(playerId = id)
        }.sortedByDescending { it.roundsPlayed + it.gamesPlayed }

        else -> emptyList<Any>()
    }

    // Drawer state + coroutine scope for open/close.
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Dart-only states that live across drawer toggles.
    var showCompare by remember { mutableStateOf(false) }
    val selectedPlayerIds = remember { mutableStateSetOf<String>() }
    var playersCollapsed by remember { mutableStateOf(false) } // hideable players section

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.widthIn(min = 300.dp, max = 360.dp)
            ) {
                // Drawer header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Einstellungen", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { scope.launch { drawerState.close() } }) {
                        Icon(Icons.Default.Close, contentDescription = "Schließen")
                    }
                }
                Divider()

                // --- Game selector (always visible in drawer) ---
                DrawerSectionTitle("Spiel auswählen")
                GameSelector(
                    selected = state.selectedGame,
                    onSelect = { g ->
                        // Reset Dart-specific selections when switching games.
                        if (g != GameType.DART) {
                            showCompare = false
                            selectedPlayerIds.clear()
                            playersCollapsed = false
                        }
                        viewModel.selectGame(g)
                    }
                )
                Spacer(Modifier.height(12.dp))

                // --- Dart-only controls in drawer ---
                if (state.selectedGame == GameType.DART) {
                    DrawerSectionTitle("Ansicht")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
                        Button(onClick = { showCompare = false }, enabled = showCompare) { Text("Übersicht") }
                        Button(onClick = { showCompare = true }, enabled = !showCompare) { Text("Vergleich") }
                    }

                    if (showCompare) {
                        Spacer(Modifier.height(16.dp))
                        DrawerSectionHeader(
                            title = "Spieler",
                            right = {
                                TextButton(onClick = { playersCollapsed = !playersCollapsed }) {
                                    Text(if (playersCollapsed) "Einblenden" else "Ausblenden")
                                }
                            }
                        )
                        val dartPlayers = (players as? List<DartStats>).orEmpty()
                        if (dartPlayers.isEmpty()) {
                            Text("Keine Spieler vorhanden", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        } else {
                            AnimatedVisibility(visible = !playersCollapsed) {
                                PlayerSelector(
                                    allPlayers = dartPlayers,
                                    playerNames = state.playerNames,
                                    selected = selectedPlayerIds
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                CustomTopBar(
                    title = "Statistik",
                    navigationIcon = {
                        IconButton(onClick = { navigateBack() }) {
                            Icon(Icons.Default.Home, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Open drawer
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menü")
                        }

                        // Reset with two-tap confirm
                        var resetPressed by remember { mutableStateOf(false) }
                        LaunchedEffect(resetPressed) {
                            if (resetPressed) {
                                delay(2000)
                                resetPressed = false
                            }
                        }
                        IconButton(
                            onClick = {
                                if (!resetPressed) resetPressed = true
                                else {
                                    viewModel.resetCurrentGameStats()
                                    resetPressed = false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (!resetPressed) Icons.Default.Delete else Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = if (!resetPressed) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            // MAIN CONTENT
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                when (state.selectedGame) {
                    GameType.SKYJO -> {
                        SectionCard(title = "Skyjo", modifier = Modifier.weight(1f)) {
                            val list = players as List<SkyjoStats>
                            if (list.isEmpty()) Text("Keine Spieler vorhanden")
                            else Column(Modifier.verticalScroll(rememberScrollState())) {
                                de.michael.tolleapp.statistics.screens.SkyjoStatsTable(list, state.playerNames)
                            }
                        }
                    }
                    GameType.SCHWIMMEN -> {
                        SectionCard(title = "Schwimmen", modifier = Modifier.weight(1f)) {
                            val list = players as List<SchwimmenStats>
                            if (list.isEmpty()) Text("Keine Spieler vorhanden")
                            else Column(Modifier.verticalScroll(rememberScrollState())) {
                                de.michael.tolleapp.statistics.screens.SchwimmenStatsTable(list, state.playerNames)
                            }
                        }
                    }
                    GameType.FLIP7 -> {
                        SectionCard(title = "Flip7", modifier = Modifier.weight(1f)) {
                            val list = players as List<Flip7Stats>
                            if (list.isEmpty()) Text("Keine Spieler vorhanden")
                            else Column(Modifier.verticalScroll(rememberScrollState())) {
                                de.michael.tolleapp.statistics.screens.Flip7StatsTable(list, state.playerNames)
                            }
                        }
                    }
                    GameType.DART -> {
                        val dartPlayers = (players as? List<DartStats>).orEmpty()
                        if (!showCompare) {
                            SectionCard(title = "Spieler-Übersicht", modifier = Modifier.weight(1f)) {
                                if (dartPlayers.isEmpty()) {
                                    Text("Keine Spieler vorhanden")
                                } else {
                                    PlayerStatsList(
                                        players = dartPlayers,
                                        playerNames = state.playerNames,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        } else {
                            val chosen = dartPlayers.filter { it.playerId in selectedPlayerIds }
                            SectionCard(title = "Vergleich", modifier = Modifier.weight(1f)) {
                                if (dartPlayers.isEmpty()) {
                                    Text("Keine Spieler vorhanden")
                                } else if (chosen.isEmpty()) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Spieler im Menü auswählen, um zu vergleichen")
                                    }
                                } else {
                                    Box(Modifier.fillMaxSize()) {
                                        DartCompareTable(players = chosen, playerNames = state.playerNames)
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        SectionCard(title = "Hinweis", modifier = Modifier.weight(1f)) {
                            Text("Öffne das Menü oben rechts und wähle ein Spiel.")
                        }
                    }
                }
            }
        }
    }
}

/* ---------------------- Drawer helpers & shared components ---------------------- */

@Composable
private fun DrawerSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun DrawerSectionHeader(
    title: String,
    right: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        if (right != null) Row(content = right)
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (actions != null) Row(content = actions)
            }
            Divider()
            Column(Modifier.fillMaxWidth().padding(12.dp), content = content)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GameSelector(
    selected: GameType?,
    onSelect: (GameType) -> Unit
) {
    val allGames = listOf(GameType.SKYJO, GameType.SCHWIMMEN, GameType.FLIP7, GameType.DART)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        allGames.forEach { g ->
            val isSelected = g == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(g) },
                label = { Text(gameLabel(g)) }
            )
        }
    }
}

private fun gameLabel(g: GameType): String = when (g) {
    GameType.SKYJO -> "Skyjo"
    GameType.SCHWIMMEN -> "Schwimmen"
    GameType.FLIP7 -> "Flip7"
    GameType.DART -> "Dart"
    else -> g.name
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerSelector(
    allPlayers: List<DartStats>,
    playerNames: Map<String, String>,
    selected: MutableSet<String>,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Quick actions
        AssistChip(
            onClick = {
                if (selected.size == allPlayers.size) selected.clear()
                else {
                    selected.clear()
                    selected.addAll(allPlayers.map { it.playerId })
                }
            },
            label = { Text(if (selected.size == allPlayers.size) "Keine" else "Alle") }
        )

        allPlayers.forEach { p ->
            val isSelected = p.playerId in selected
            FilterChip(
                selected = isSelected,
                onClick = {
                    if (isSelected) selected.remove(p.playerId) else selected.add(p.playerId)
                },
                label = { Text(playerNames[p.playerId] ?: "?", maxLines = 1) }
            )
        }
    }
}
