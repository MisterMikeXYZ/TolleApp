package de.michael.tolleapp.presentation.app1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.data.Player
import de.michael.tolleapp.data.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

class SkyjoViewModel(private val repository: PlayerRepository) : ViewModel() {
    private val _state = MutableStateFlow(SkyjoState())
    val state: StateFlow<SkyjoState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getPlayers().collect { players ->
                _state.update { it.copy(players = players) }
            }
        }
    }

    fun addPlayer(name: String, rowIndex: Int) = viewModelScope.launch {
        val newPlayer = Player(name = name)
        val added = repository.addPlayer(newPlayer)
        if (added) {
            selectPlayer(rowIndex, newPlayer.id)
        }
    }

    fun selectPlayer(rowIndex: Int, playerId: String) {
        _state.update { state ->
            val updated = state.selectedPlayerIds.toMutableList()
            // prevent duplicates
            if (updated.contains(playerId)) return
            // ensure list is big enough
            while (updated.size <= rowIndex) {
                updated.add(null)
            }
            updated[rowIndex] = playerId
            // auto add empty row if last row got a player
            if (rowIndex == updated.lastIndex) {
                updated.add(null)
            }
            state.copy(selectedPlayerIds = updated)
        }
    }

    fun removePlayer(rowIndex: Int) {
        _state.update { state ->
            val updated = state.selectedPlayerIds.toMutableList()
            if (rowIndex < updated.size) {
                updated.removeAt(rowIndex)
            }
            // keep at least 2 slots
            while (updated.size < 2) {
                updated.add(null)
            }
            state.copy(selectedPlayerIds = updated)
        }
    }
}
