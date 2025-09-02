package de.michael.tolleapp.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.data.schwimmen.stats.SchwimmenStats
import de.michael.tolleapp.data.schwimmen.stats.SchwimmenStatsRepository
import de.michael.tolleapp.data.skyjo.stats.SkyjoStats
import de.michael.tolleapp.data.skyjo.stats.SkyjoStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StatViewModel(
    private val skyjoRepo: SkyjoStatsRepository,
    private val schwimmenRepo: SchwimmenStatsRepository,
) : ViewModel() {

    private val _allPlayers = skyjoRepo.getPlayers()

    private val _skyjoStats = skyjoRepo.getAllStats()
    private val _schwimmenStats = schwimmenRepo.getAllStats()

    private val _state = MutableStateFlow(StatState())

    val state = combine(
        _allPlayers,       // Flow<List<Player>>
        _skyjoStats,       // Flow<List<SkyjoStats>>
        _schwimmenStats,   // Flow<List<SchwimmenStats>>
        _state             // MutableStateFlow for selectedGame
    ) { allPlayers, skyjoList, schwimmenList, state ->
        val playersSkyjoFull = allPlayers.map { player ->
            skyjoList.find { it.playerId == player.id } ?: SkyjoStats(playerId = player.id)
        }
        val playersSchwimmenFull = allPlayers.map { player ->
            schwimmenList.find { it.playerId == player.id } ?: SchwimmenStats(playerId = player.id)
        }

        state.copy(
            playersSkyjo = playersSkyjoFull,
            playersSchwimmen = playersSchwimmenFull,
            playerNames = allPlayers.associate { it.id to it.name }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = StatState()
    )

    fun resetCurrentGameStats() {
        viewModelScope.launch {
            when (_state.value.selectedGame) {
                GameType.SKYJO -> skyjoRepo.resetAllGameStats()
                GameType.SCHWIMMEN -> schwimmenRepo.resetAllGameStats()
            }
        }
    }

    fun selectGame(game: GameType) {
        _state.value = _state.value.copy(selectedGame = game)
    }
}