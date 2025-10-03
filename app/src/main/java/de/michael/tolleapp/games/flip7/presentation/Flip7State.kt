package de.michael.tolleapp.games.flip7.presentation

import de.michael.tolleapp.games.flip7.domain.Flip7RoundData
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.games.util.PausedGame
import de.michael.tolleapp.games.util.player.Player
import de.michael.tolleapp.games.util.presets.GamePresetWithPlayers
import de.michael.tolleapp.games.util.startScreen.StartState

data class Flip7State(

    val allPlayers: List<Player> = emptyList(),
    val presets: List<GamePresetWithPlayers> = emptyList(),
    val pausedGames: List<PausedGame> = emptyList(),
    val selectedPlayers: List<Player?> = listOf(null, null, null),
    val selectedPlayerIds: List<String?> = listOf(null, null, null),


    val gameId: String = "",
    val isFinished: Boolean = false,
    val currentDealerId: String? = null,
    val rounds: List<Flip7RoundData> = emptyList(),
    val totalPoints: Map<String, Int> = emptyMap(),
    val visibleRoundRows: Int = 5,
    val winners: List<Player> = emptyList(),

    val lastKeyboardPage: Int = 0,
    val sortDirection: de.michael.tolleapp.games.util.table.SortDirection = de.michael.tolleapp.games.util.table.SortDirection.ASCENDING,
)

fun Flip7State.toStartState(): StartState {
    return StartState(
        gameType = GameType.FLIP7,
        pausedGames = this.pausedGames,
        presets = this.presets,
        allPlayers = this.allPlayers,
        selectedPlayers = this.selectedPlayers,
    )
}