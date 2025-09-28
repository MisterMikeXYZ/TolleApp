package de.michael.tolleapp.games.skyjo.domain

data class SkyjoGame (
    val id: String,
    val createdAt: Long = System.currentTimeMillis(),
    val playerIds: List<String> = emptyList(),
    val rounds: List<SkyjoRoundData> = emptyList(),
    val finished: Boolean = false,
    val dealerId: String? = null,
    val endPoints: Int = 100,
)