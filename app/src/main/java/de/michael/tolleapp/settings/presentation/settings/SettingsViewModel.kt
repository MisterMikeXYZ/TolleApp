package de.michael.tolleapp.settings.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.games.player.Player
import de.michael.tolleapp.games.player.PlayerRepository
import de.michael.tolleapp.games.presets.GamePresetRepository
import de.michael.tolleapp.settings.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.toMutableList

class SettingsViewModel (
    private val playerRepo: PlayerRepository,
    private val settingsRepo: SettingsRepository,
    private val presetRepo: GamePresetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    init {
        loadSettings()
        loadPlayers()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isDarkmode = settingsRepo.isDarkmode()
            )
        }
    }

    private fun loadPlayers() {
        viewModelScope.launch {
            playerRepo.getAllPlayers().collect { players ->
                _state.value = _state.value.copy(players = players)
            }
        }
    }

    fun toggleDarkMode() {
        val newValue = !_state.value.isDarkmode
        viewModelScope.launch {
            settingsRepo.changeDarkmode(newValue)
            _state.value = _state.value.copy(isDarkmode = newValue)
        }
    }

    private fun deletePlayer(player: Player) {
        viewModelScope.launch {
            playerRepo.deletePlayer(player)
            loadPlayers() // refresh after deletion
        }
    }

    fun deleteSelectedPlayers() {
        val players = _state.value.playersToDelete
        players.forEach { player -> deletePlayer(player) }
    }

    fun selectPlayer(player: Player) {
        val selectedPlayers = _state.value.playersToDelete.toMutableList()
        selectedPlayers.add(player)
        _state.value = _state.value.copy(playersToDelete = selectedPlayers)
    }

    fun deselectPlayer(player: Player) {
        val selectedPlayers = _state.value.playersToDelete.toMutableList()
        selectedPlayers.remove(player)
        _state.value = _state.value.copy(playersToDelete = selectedPlayers)

    }

    fun createTestPresets() {
        viewModelScope.launch {
            val players = playerRepo.createPlayers()
            if (players.isEmpty()) return@launch
            val playerMap = players.associateBy { it.name }

            val presets = listOf(
                "Test" to listOf("Michi", "Malte", "Nele"),
                "Test Extrem" to listOf("Michi", "Malte", "Nele", "Papa", "Mama", "Raphi"),
                "Viebegs" to listOf("Michi", "Papa", "Mama", "Raphi")
            )

            val games = listOf("skyjo", "schwimmen", "randomizer", "dart")

            games.forEach { gameType ->
                presets.forEach { (presetName, playerNames) ->
                    val playerIds = playerNames.mapNotNull { playerMap[it]?.id }
                    if (playerIds.isNotEmpty()) {
                        presetRepo.createPreset(gameType, presetName, playerIds)
                    }
                }
            }
        }
    }

    fun deleteTestPresets() {
        viewModelScope.launch { presetRepo.deleteTestPresets() }
    }
}
