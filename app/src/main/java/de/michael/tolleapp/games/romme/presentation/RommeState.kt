package de.michael.tolleapp.games.romme.presentation

import de.michael.tolleapp.games.romme.domain.RommeRoundData
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.games.util.PausedGame
import de.michael.tolleapp.games.util.player.Player
import de.michael.tolleapp.games.util.presets.GamePresetWithPlayers
import de.michael.tolleapp.games.util.startScreen.StartState

data class RommeState(
    override val allPlayers: List<Player> = emptyList(),
    override val presets: List<GamePresetWithPlayers> = emptyList(),
    override val pausedGames: List<PausedGame> = emptyList(),

    override val selectedPlayers: List<Player?> = listOf(null, null, null),

    val currentGameId: String? = null,
    val finished: Boolean = false,

    val rounds: List<RommeRoundData> = emptyList(),
): StartState(gameType = GameType.ROMME)