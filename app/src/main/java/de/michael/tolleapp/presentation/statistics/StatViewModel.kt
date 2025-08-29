package de.michael.tolleapp.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.data.skyjo.player.SkyjoStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StatViewModel(
    private val repository: SkyjoStatsRepository
) : ViewModel() {
    private val _state: MutableStateFlow<StatState> = MutableStateFlow(StatState())
    val state: StateFlow<StatState> = _state.asStateFlow()

    fun getPlayers() {
        viewModelScope.launch {
            repository.getPlayers().collect { players ->
                // players here are Player objects
                val names = players.associate { it.id to it.name }

                // fetch SkyjoStats for all players
                val statsList = repository.getStatsForPlayers(players.map { it.id })

                _state.update { it.copy(players = statsList, playerNames = names) }
            }
        }
    }


    fun resetAllGameStats() {
        viewModelScope.launch {
            repository.resetAllGameStats()
        }
    }
}
