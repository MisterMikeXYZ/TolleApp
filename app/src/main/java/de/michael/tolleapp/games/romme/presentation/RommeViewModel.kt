package de.michael.tolleapp.games.romme.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.games.romme.data.RommeRepository
import de.michael.tolleapp.games.romme.domain.RommeRoundData
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.games.util.player.PlayerRepository
import de.michael.tolleapp.games.util.presets.GamePresetRepository
import de.michael.tolleapp.games.util.startScreen.StartAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class RommeViewModel(
    private val playerRepo: PlayerRepository,
    private val rommeRepo: RommeRepository,
    private val presetRepo: GamePresetRepository
): ViewModel() {

    private val _allPlayers = playerRepo.getAllPlayers()
    private val _presets = presetRepo.getPresets(GameType.ROMME.toString())
    private val _pausedGames = rommeRepo.getPausedGames()
        .getOrThrow()

    private val _selectedPlayerIds = MutableStateFlow<List<String?>>(
        listOf(null, null)
    )

    private val _state = MutableStateFlow(RommeState())

    val state = combine(
        _allPlayers,
        _presets,
        _selectedPlayerIds,
        _pausedGames,
        _state
    ) { players, presets, selectedPlayerIds, pausedGames, state ->
        state.copy(
            allPlayers = players,
            presets = presets,
            pausedGames = pausedGames,
            selectedPlayers = selectedPlayerIds.map { selectedPlayerId ->
                players.find { it.id == selectedPlayerId }
            }
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5_000),
            initialValue = RommeState()
        )

    fun onStartAction(action: StartAction) {
        when (action) {
            is StartAction.CreatePlayer -> viewModelScope.launch {
                val player = playerRepo.addPlayer(action.name)
                selectPlayer(action.index, player.id)
            }
            is StartAction.SelectPlayer -> selectPlayer(action.index, action.playerId)
            is StartAction.UnselectPlayer -> unselectPlayer(action.index)
            StartAction.ResetSelectedPlayers -> _selectedPlayerIds.update { List(2) { null } }

            is StartAction.CreatePreset -> viewModelScope.launch {
                presetRepo.createPreset(GameType.ROMME.toString(), action.presetName, action.playerIds)
            }
            is StartAction.SelectPreset -> viewModelScope.launch {
                val players = state.value.presets.first { it.preset.id == action.presetId }.players
                _selectedPlayerIds.update { List(2) { null } }
                players.map { it.playerId }.forEachIndexed { index, playerId ->
                    selectPlayer(index, playerId)
                }
            }
            is StartAction.DeletePreset -> viewModelScope.launch {
                presetRepo.deletePreset(action.presetId)
            }

            StartAction.StartGame -> {
                val gameId = UUID.randomUUID().toString()
                _selectedPlayerIds.update { it.filterNotNull() }
                val emptyScoreList = state.value.selectedPlayers.filterNotNull().associate { it.id to null }
                _state.update { state ->
                    state.copy(
                        currentGameId = gameId,
                        rounds = listOf(
                            RommeRoundData(
                                roundNumber = 1,
                                roundScores = emptyScoreList,
                                finalScores = emptyScoreList,
                            )
                        )
                    )
                }
                viewModelScope.launch {
                    rommeRepo.createGame(gameId)
                    state.value.selectedPlayers.filterNotNull().forEach { player ->
                        rommeRepo.addPlayerToGame(gameId, player.id)
                    }
                    rommeRepo.upsertRound(
                        gameId,
                        state.value.rounds.first()
                    )
                }
            }
            is StartAction.ResumeGame -> {
                viewModelScope.launch {
                    val pausedGame = rommeRepo.getGame(action.gameId)
                        .getOrThrow()
                        .first()
                        ?: throw IllegalStateException("Could not find game #${action.gameId}")
                    _selectedPlayerIds.update { pausedGame.playerIds }
                    _state.update { state -> state.copy(
                        currentGameId = pausedGame.id,
                        rounds = pausedGame.rounds,
                    ) }
                }
            }
            is StartAction.DeleteGame -> {
                viewModelScope.launch {
                    rommeRepo.deleteGame(action.gameId)
                }
            }

            else -> throw NotImplementedError("Action '$action' not implemented in WizardViewModel")
        }
    }

    fun onAction(action: RommeAction) {
        when (action) {
            is RommeAction.OnRoundScoreChange -> {
                _state.update { state ->
                    val currentRound = state.rounds.lastOrNull() ?: RommeRoundData(roundNumber = 1)

                    val updatedRoundScores = currentRound.roundScores.toMutableMap()
                    val updatedFinalScores = currentRound.finalScores.toMutableMap().apply {
                        this[action.playerId] = this[action.playerId]
                            ?.minus(currentRound.roundScores[action.playerId] ?: 0)
                            ?.plus(action.newValue)
                            ?: action.newValue
                    }
                    updatedRoundScores[action.playerId] = action.newValue

                    val updatedRound = currentRound.copy(
                        roundScores = updatedRoundScores,
                        finalScores = updatedFinalScores,
                    )

                    return@update state.copy(
                        rounds = state.rounds.dropLast(1) + updatedRound
                    )
                }
                val gameId = state.value.currentGameId ?: return
                viewModelScope.launch {
                    rommeRepo.upsertRound(
                        gameId,
                        state.value.rounds.last()
                    )
                }
            }
            RommeAction.FinishRound -> {
                _state.update { state ->
                    val currentRound = state.rounds.last()
                    val newRound = RommeRoundData(
                        roundNumber = currentRound.roundNumber + 1,
                        roundScores = currentRound.roundScores.mapValues { null },
                        finalScores = currentRound.finalScores
                    )
                    state.copy(
                        rounds = state.rounds + newRound,
                    )
                }
                val gameId = state.value.currentGameId ?: return
                viewModelScope.launch {
                    rommeRepo.upsertRound(
                        gameId,
                        state.value.rounds.last()
                    )
                }
            }
            RommeAction.OnGameFinished -> {
                val gameId = state.value.currentGameId
                    ?: throw IllegalStateException("Game ID is null, cannot finish game")
                viewModelScope.launch {
                    rommeRepo.finishGame(gameId)
                }
            }
            RommeAction.DeleteGame -> {
                val gameId = state.value.currentGameId
                    ?: throw IllegalStateException("Game ID is null, cannot delete game")
                viewModelScope.launch {
                    rommeRepo.deleteGame(gameId)
                }
            }

            else -> throw NotImplementedError("Action '$action' not implemented in RommeViewModel")
        }
    }

    private fun selectPlayer(index: Int, playerId: String) {
        _selectedPlayerIds.update { selectedPlayerIds ->
            val newValue = selectedPlayerIds.toMutableList().apply {
                this[index] = playerId
                if (index == this.lastIndex) this.add(null) // auto add empty row if last row got a player
            }
            return@update newValue
        }

    }

    private fun unselectPlayer(index: Int) {
        _selectedPlayerIds.update { selectedPlayerIds ->
            val updated = selectedPlayerIds.toMutableList()
            if (index < updated.size) {
                updated.removeAt(index)
            }
            if (updated.last() != null) {
                updated.add(null)
            }
            return@update updated
        }
    }
}