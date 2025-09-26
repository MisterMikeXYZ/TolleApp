package de.michael.tolleapp.games.util.startScreen

import de.michael.tolleapp.games.util.PausedGame
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.games.util.player.Player
import de.michael.tolleapp.games.util.presets.GamePresetWithPlayers

open class StartState(
    val gameType: GameType,
    open val pausedGames: List<PausedGame> = emptyList(),
    open val presets: List<GamePresetWithPlayers> = emptyList(),
    open val allPlayers: List<Player> = emptyList(),
    open val selectedPlayers: List<Player?> = listOf(null, null),
)
