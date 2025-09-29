package de.michael.tolleapp.games.skyjo.presentation

import de.michael.tolleapp.games.skyjo.domain.SkyjoRoundData
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.games.util.PausedGame
import de.michael.tolleapp.games.util.player.Player
import de.michael.tolleapp.games.util.presets.GamePresetWithPlayers
import de.michael.tolleapp.games.util.startScreen.StartState
import de.michael.tolleapp.games.util.table.SortDirection
import de.michael.tolleapp.games.wizard.domain.WizardRoundData

data class SkyjoState(


    //val perPlayerRounds: Map<String, List<Int>> = emptyMap(),
    val totalPoints: Map<String, Int> = emptyMap(),
    val visibleRoundRows: Int = 5,
    val winnerId: List<String?> = listOf(null),
    val loserId: List<String?> = listOf(null),
    val ranking: List<String> = emptyList(),
    val currentDealerId: String? = null,


    val lastKeyboardPage: Int = 0,

    val allPlayers: List<Player> = emptyList(),
    val presets: List<GamePresetWithPlayers> = emptyList(),
    val pausedGames: List<PausedGame> = emptyList(),
    val selectedPlayers: List<Player?> = listOf(null, null),
    val selectedPlayerIds: List<String?> = listOf(null, null),

    val rounds: List<SkyjoRoundData> = emptyList(),
    val endPoints: Int = 100,

    val currentGameId: String = "",
    val isGameEnded: Boolean = false,

    val sortDirection: SortDirection = SortDirection.ASCENDING,
)

fun SkyjoState.toStartState(): StartState {
    return StartState(
        gameType = GameType.SKYJO,
        pausedGames = this.pausedGames,
        presets = this.presets,
        allPlayers = this.allPlayers,
        selectedPlayers = this.selectedPlayers,
    )
}