package de.michael.tolleapp.games.wizard.presentation

import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.games.util.PausedGame
import de.michael.tolleapp.games.util.player.Player
import de.michael.tolleapp.games.util.presets.GamePresetWithPlayers
import de.michael.tolleapp.games.util.startScreen.StartState
import de.michael.tolleapp.games.wizard.domain.WizardRoundData

data class WizardState(
    val allPlayers: List<Player> = emptyList(),
    val presets: List<GamePresetWithPlayers> = emptyList(),
    val pausedGames: List<PausedGame> = emptyList(),

    val selectedPlayers: List<Player?> = listOf(null, null, null),

    val currentGameId: String? = null,
    val roundsToPlay: Int = 20,
    val finished: Boolean = false,

    val rounds: List<WizardRoundData> = emptyList(),
)

fun WizardState.toStartState(): StartState {
    return StartState(
        gameType = GameType.WIZARD,
        pausedGames = this.pausedGames,
        presets = this.presets,
        allPlayers = this.allPlayers,
        selectedPlayers = this.selectedPlayers
    )
}