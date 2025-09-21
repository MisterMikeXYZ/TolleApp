package de.michael.tolleapp.games.randomizer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.games.player.Player
import de.michael.tolleapp.games.player.PlayerRepository
import de.michael.tolleapp.games.presets.GamePresetRepository
import de.michael.tolleapp.games.skyjo.presentation.SkyjoState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RandomizerViewModel(
    private val playerRepo: PlayerRepository,
    private val presetRepo: GamePresetRepository,
    //private val randomizerRepo: RandomizerRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(RandomizerState())
    private val allPlayers = playerRepo.getAllPlayers()
    val state = combine(
        allPlayers,
        _state
    ) { players, state ->
        state.copy(
            playerNames = players.associate { it.id to it.name }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RandomizerState()
    )
    val presets = presetRepo.getPresets("randomizer")

    fun addPlayer(name: String, rowIndex: Int) = viewModelScope.launch {
        val added = playerRepo.addPlayer(name)
        selectPlayer(rowIndex, added.id)
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
    fun resetSelectedPlayers() {
        _state.update { state ->
            state.copy(
                selectedPlayerIds = listOf(null, null),
            )
        }
    }

    fun createPreset(gameType: String, name: String, playerIds: List<String>) {
        viewModelScope.launch {
            presetRepo.createPreset(gameType, name, playerIds)
        }
    }

    fun deletePreset(presetId: Long) {
        viewModelScope.launch {
            presetRepo.deletePreset(presetId)
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
    fun setRandomizerType(type: String) {
        _state.update { state ->
            state.copy(randomizerType = type)
        }
    }

    fun reset() {
        _state.update {state -> RandomizerState()}
    }
}