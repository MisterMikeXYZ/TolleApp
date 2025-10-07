package de.michael.tolleapp.games.randomizer.presentation

import de.michael.tolleapp.games.util.PausedGame
import de.michael.tolleapp.games.util.player.Player
import de.michael.tolleapp.games.util.presets.GamePresetWithPlayers

data class RandomizerState(
    // --- Random Names ---
    val allPlayers: List<Player> = emptyList(),
    val presets: List<GamePresetWithPlayers> = emptyList(),
    val selectedPlayers: List<Player?> = listOf(null, null),
    val selectedPlayerIds: List<String?> = listOf(null, null),

    //
    val randomNumber: Int = 0,
    val randomizerType: String = "Zufallsgenerator"
)