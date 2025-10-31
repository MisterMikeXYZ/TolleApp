package de.michael.tolleapp.games.skyjo.domain

data class SkyjoRoundData (
    val roundNumber: Int,
    val dealerId: String?,
    val scores: Map<String, Int> = emptyMap(),
)


// List(Map<String, Int>) -> Map<String, Sum(List<Map.values>)>
//