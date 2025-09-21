package de.michael.tolleapp.games.randomizer.presentation

data class RandomizerState(
    val playerNames: Map<String, String> = emptyMap(),
    val selectedPlayerIds: List<String?> = listOf(null, null),
    val randomNumber: Int = 0,
    val randomizerType: String = "Zufallsgenerator"
)