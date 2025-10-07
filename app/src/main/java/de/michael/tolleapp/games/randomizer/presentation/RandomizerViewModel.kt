package de.michael.tolleapp.games.randomizer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.games.skyjo.presentation.SkyjoState
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.games.util.player.PlayerRepository
import de.michael.tolleapp.games.util.presets.GamePresetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RandomizerViewModel(
    private val playerRepo: PlayerRepository,
    private val presetRepo: GamePresetRepository,
    //private val randomizerRepo: RandomizerRepository,
) : ViewModel() {
    private val _allPlayers = playerRepo.getAllPlayers()
    private val _presets = presetRepo.getPresets(GameType.RANDOMIZER.toString())
    private val _selectedPlayerIds = MutableStateFlow<List<String?>>(listOf(null, null))
    private val _state = MutableStateFlow(RandomizerState())

    val state = combine(
        _allPlayers,
        _presets,
        _selectedPlayerIds,
        _state
    ) { players, presets, selectedPlayerIds, state ->
        state.copy(
            allPlayers = players,
            presets = presets,
            selectedPlayers = selectedPlayerIds.map { selectedPlayerId ->
                players.find { it.id == selectedPlayerId }
            },
            selectedPlayerIds = selectedPlayerIds,

            )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5_000),
        initialValue = RandomizerState()
    )



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