package de.michael.tolleapp.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.data.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StatViewModel(
    private val repository: PlayerRepository
) : ViewModel() {
    private val _state : MutableStateFlow<StatState> = MutableStateFlow(StatState())
    val state: StateFlow<StatState> = _state.asStateFlow()

    fun getPlayers() {
        viewModelScope.launch {
            repository.getPlayers().collect { players ->
                _state.update { it.copy(players = players) }
            }
        }
    }

    fun resetAllGameStats () {
        viewModelScope.launch {
            repository.resetAllGameStats()
        }
    }
}
