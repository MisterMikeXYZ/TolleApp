package de.michael.tolleapp.games.util.startScreen

import de.michael.tolleapp.games.util.PausedGame
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.games.util.player.Player
import de.michael.tolleapp.games.util.presets.GamePresetWithPlayers

data class StartGameState(
    val gameType: GameType,
    val pausedGames: List<PausedGame> = emptyList(),
    val presets: List<GamePresetWithPlayers> = emptyList(),
    val allPlayers: List<Player> = emptyList(),
    val selectedPlayers: List<Player?> = listOf(null, null),
)
