package de.michael.tolleapp.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.data.skyjo.player.SkyjoStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StatViewModel(
    private val repository: SkyjoStatsRepository
) : ViewModel() {

    private val _allPlayers = repository
        .getPlayers()

    private val _statsList = repository
        .getAllStats()

    private val _state = MutableStateFlow(StatState())

    val state = combine(
        _state,
        _allPlayers,
        _statsList,
    ) { state, allPlayers, statsList ->
        state.copy(
            players = statsList,
            playerNames = allPlayers.associate { it.id to it.name }
        )
    }
        .stateIn(
            viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
            initialValue = StatState()
        )


    fun resetAllGameStats() {
        viewModelScope.launch {
            repository.resetAllGameStats()
        }
    }
}
