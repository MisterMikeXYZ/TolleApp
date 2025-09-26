package de.michael.tolleapp.games.romme.domain

data class RommeGame(
    val id: String,
    val createdAt: Long = System.currentTimeMillis(),
    val playerIds: List<String> = emptyList(), // List of playerIds
    val rounds: List<RommeRoundData> = emptyList(),
    val finished: Boolean = false,
)
