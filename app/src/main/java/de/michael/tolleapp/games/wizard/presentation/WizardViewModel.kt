package de.michael.tolleapp.games.wizard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.games.util.player.PlayerRepository
import de.michael.tolleapp.games.util.presets.GamePresetRepository
import de.michael.tolleapp.games.util.startScreen.StartAction
import de.michael.tolleapp.games.wizard.domain.WizardRoundData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

class WizardViewModel(
    private val playerRepo: PlayerRepository,
//    private val gameRepo: , FIXME
    private val presetRepo: GamePresetRepository
): ViewModel() {

    private val _allPlayers = playerRepo.getAllPlayers()
    private val _presets = presetRepo.getPresets(GameType.WIZARD.toString())

    private val _selectedPlayerIds = MutableStateFlow<List<String?>>(
        listOf(null, null, null)
    )

    private val _state = MutableStateFlow(WizardState())

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
            }
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5_000),
            initialValue = WizardState()
        )

    fun onStartAction(action: StartAction) {
        when (action) {
            is StartAction.CreatePlayer -> viewModelScope.launch {
                val player = playerRepo.addPlayer(action.name)
                selectPlayer(action.index, player.id)
            }
            is StartAction.SelectPlayer -> selectPlayer(action.index, action.playerId)
            is StartAction.UnselectPlayer -> unselectPlayer(action.index)
            StartAction.ResetSelectedPlayers -> _state.update { it.copy(
                selectedPlayers = listOf(null, null, null)
            ) }

            is StartAction.CreatePreset -> viewModelScope.launch {
                presetRepo.createPreset(GameType.WIZARD.toString(), action.presetName, action.playerIds)
            }
            is StartAction.DeletePreset -> viewModelScope.launch {
                presetRepo.deletePreset(action.presetId)
            }

            StartAction.StartGame -> {
                _selectedPlayerIds.update { it.filterNotNull() }
                _state.update { state -> state.copy(
                    currentGameId = UUID.randomUUID().toString(),
                    rounds = listOf(
                        WizardRoundData(
                            roundNumber = 1,
                            dealerId = this.state.value.selectedPlayers.first()!!.id
                        )
                    )
                ) }
            }
            is StartAction.ResumeGame -> TODO()
            is StartAction.DeleteGame -> TODO()

            else -> throw NotImplementedError("Action '$action' not implemented in WizardViewModel")
        }
    }

    fun onAction(action: WizardAction) {
        when (action) {
            is WizardAction.OnBidChange -> _state.update { state ->
                val currentRound = state.rounds.last()
                val newRound = currentRound.copy(
                    bids = currentRound.bids.toMutableMap().apply {
                        this[action.playerId] = action.newValue
                    }
                )
                state.copy(rounds = state.rounds.dropLast(1) + newRound)
            }
            is WizardAction.OnTricksWonChange -> _state.update { state ->
                val currentRound = state.rounds.last()
                val newRound = currentRound.copy(
                    tricksWon = currentRound.tricksWon.toMutableMap().apply {
                        this[action.playerId] = action.newValue
                    }
                )
                state.copy(rounds = state.rounds.dropLast(1) + newRound)
            }
            WizardAction.FinishBidding -> _state.update { state ->
                val currentRound = state.rounds.last()
                val newRound = currentRound.copy(
                    bidsFinal = true
                )
                state.copy(rounds = state.rounds.dropLast(1) + newRound)
            }
            WizardAction.FinishRound -> _state.update { state ->
                val previousRound = state.rounds.getOrNull(state.rounds.size - 2)
                val currentRound = state.rounds.last()
                val selectedPlayers = this.state.value.selectedPlayers
                // calculate scores
                val scores = selectedPlayers.filterNotNull().associate { player ->
                    val bid = currentRound.bids[player.id] ?: 0
                    val tricksWon = currentRound.tricksWon[player.id] ?: 0
                    val previousScore = previousRound?.scores[player.id] ?: 0
                    val roundScore = if (bid == tricksWon) {
                        20 + bid * 10
                    } else {
                        -10 * abs(bid - tricksWon)
                    }
                    player.id to (previousScore + roundScore)
                }
                val updatedCurrentRound = currentRound.copy(
                    scores = scores
                )
                // Finish game if last round played
                if (currentRound.roundNumber >= state.roundsToPlay) {
                    return@update state.copy(
                        rounds = state.rounds.dropLast(1) + updatedCurrentRound,
                        finished = true,
                    )
                }
                // prepare next round
                val nextDealerIndex = selectedPlayers.indexOfFirst { it?.id == currentRound.dealerId }
                    .let { if (it == -1) 0 else (it + 1) % selectedPlayers.size }
                val nextRound = WizardRoundData(
                    roundNumber = currentRound.roundNumber + 1,
                    dealerId = selectedPlayers[nextDealerIndex]?.id ?: "",
                )
                state.copy(rounds = state.rounds.dropLast(1) + updatedCurrentRound + nextRound)
            }
            else -> throw NotImplementedError("Action '$action' not implemented in WizardViewModel")
        }
    }

    private fun selectPlayer(index: Int, playerId: String) {
        _selectedPlayerIds.update { selectedPlayerIds ->
            val newValue = selectedPlayerIds.toMutableList().apply {
                this[index] = playerId
                if (index == this.lastIndex && this.size < 6) this.add(null) // auto add empty row if last row got a player
            }
            _state.update { it.copy(
                roundsToPlay = when(newValue.filterNotNull().size) {
                    in (0..3) -> 20
                    4 -> 15
                    5 -> 12
                    6 -> 10
                    else -> throw IllegalStateException("Too many players selected")
                }
            ) }
            return@update newValue
        }

    }

    private fun unselectPlayer(index: Int) {
        _selectedPlayerIds.update { selectedPlayerIds ->
            val updated = selectedPlayerIds.toMutableList()
            if (index < updated.size) {
                updated.removeAt(index)
            }
            if (updated.size < 6 && updated.last() != null) {
                updated.add(null)
            }
            _state.update { it.copy(
                roundsToPlay = when(updated.filterNotNull().size) {
                    in (0..3) -> 20
                    4 -> 15
                    5 -> 12
                    6 -> 10
                    else -> throw IllegalStateException("Too many players selected")
                }
            ) }
            return@update updated
        }
    }
}