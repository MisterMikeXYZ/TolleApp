package de.michael.tolleapp.games.wizard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.BuildConfig
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.games.util.player.PlayerRepository
import de.michael.tolleapp.games.util.presets.GamePresetRepository
import de.michael.tolleapp.games.util.startScreen.StartAction
import de.michael.tolleapp.games.wizard.data.WizardRepository
import de.michael.tolleapp.games.wizard.domain.WizardRoundData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.*
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class WizardViewModel(
    private val playerRepo: PlayerRepository,
    private val wizardRepo: WizardRepository,
    private val presetRepo: GamePresetRepository
): ViewModel() {

    private val _allPlayers = playerRepo.getAllPlayers()
    private val _presets = presetRepo.getPresets(GameType.WIZARD.toString())
    private val _pausedGames = wizardRepo.getPausedGames()
        .getOrThrow()

    private val _selectedPlayerIds = MutableStateFlow<List<String?>>(
        listOf(null, null, null)
    )

    private val _state = MutableStateFlow(WizardState())

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
    }.stateIn(
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
            StartAction.ResetSelectedPlayers -> _selectedPlayerIds.update { listOf(null, null, null) }

            is StartAction.CreatePreset -> viewModelScope.launch {
                presetRepo.createPreset(GameType.WIZARD, action.presetName, action.playerIds)
            }
            is StartAction.SelectPreset -> viewModelScope.launch {
                val players = state.value.presets.first { it.preset.id == action.presetId }.players
                if (players.size > 6) return@launch // exceeds max players for Wizard
                _selectedPlayerIds.update { listOf(null, null, null) }
                players.map { it.playerId }.forEachIndexed { index, playerId ->
                    selectPlayer(index, playerId)
                }
            }
            is StartAction.DeletePreset -> viewModelScope.launch {
                presetRepo.deletePreset(action.presetId)
            }

            is StartAction.StartGame -> {
                val gameId = UUID.randomUUID().toString()
                _selectedPlayerIds.update { it.filterNotNull() }
                _state.update { state -> state.copy(
                    currentGameId = gameId,
                    rounds = listOf(
                        WizardRoundData(
                            roundNumber = 1,
                            dealerId = this.state.value.selectedPlayers.first()!!.id
                        )
                    ),
                    undoStack = emptyList(),
                ) }
                viewModelScope.launch {
                    wizardRepo.createGame(
                        gameId,
                        state.value.roundsToPlay
                    )
                    state.value.selectedPlayers.filterNotNull().forEach { player ->
                        wizardRepo.addPlayerToGame(gameId, player.id)
                    }
                    wizardRepo.upsertRound(
                        gameId,
                        state.value.rounds.first()
                    )
                }
            }

            is StartAction.ResumeGame -> {
                viewModelScope.launch {
                    val pausedGame = wizardRepo.getGame(action.gameId)
                        .getOrThrow()
                        .first()
                        ?: throw IllegalStateException("Could not find game #${action.gameId}")
                    _selectedPlayerIds.update { pausedGame.playerIds }
                    _state.update { state -> state.copy(
                        currentGameId = pausedGame.id,
                        roundsToPlay = pausedGame.roundsToPlay,
                        rounds = pausedGame.rounds,
                        undoStack = emptyList(),
                    ) }
                }
            }
            is StartAction.DeleteGame -> {
                viewModelScope.launch {
                    wizardRepo.deleteGame(action.gameId)
                }
            }

            else -> throw NotImplementedError("Action '$action' not implemented in WizardViewModel")
        }
    }

    fun onAction(action: WizardAction) {
        when (action) {
            is WizardAction.OnBidChange -> _state.update { state ->
                val currentRound = state.rounds.last()
                if (currentRound.bids[action.playerId] == action.newValue) return@update state
                val newRound = currentRound.copy(
                    bids = currentRound.bids.toMutableMap().apply {
                        this[action.playerId] = action.newValue
                    }
                )
                state.copy(
                    rounds = state.rounds.dropLast(1) + newRound,
                    undoStack = state.undoStack + listOf(state.rounds),
                )
            }
            is WizardAction.OnTricksWonChange -> {
                val currentRound = state.value.rounds.last()
                if (currentRound.tricksWon[action.playerId] == action.newValue) return
                val newRound = currentRound.copy(
                    tricksWon = currentRound.tricksWon.toMutableMap().apply {
                        this[action.playerId] = action.newValue
                    }
                )
                _state.update { state ->
                    state.copy(
                        rounds = state.rounds.dropLast(1) + newRound,
                        undoStack = state.undoStack + listOf(state.rounds),
                    )
                }
                viewModelScope.launch {
                    wizardRepo.upsertRound(
                        state.value.currentGameId!!,
                        listOf(currentRound, newRound)
                    )
                }
            }
            is WizardAction.OnSortDirectionChange -> _state.update { state ->
                state.copy(sortDirection = action.newDirection)
            }
            is WizardAction.FinishBidding -> {
                val currentRound = state.value.rounds.last()
                val newRound = currentRound.copy(
                    bidsFinal = true
                )
                _state.update { state ->
                    state.copy(
                        rounds = state.rounds.dropLast(1) + newRound,
                        undoStack = state.undoStack + listOf(state.rounds),
                    )
                }
                viewModelScope.launch {
                    wizardRepo.upsertRound(
                        state.value.currentGameId!!,
                        newRound
                    )
                }
            }
            is WizardAction.FinishRound -> {
                val previousRound = state.value.rounds.getOrNull(state.value.rounds.size - 2)
                val currentRound = state.value.rounds.last()
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
                if (currentRound.roundNumber >= state.value.roundsToPlay) {
                    _state.update { state ->
                        state.copy(
                            rounds = state.rounds.dropLast(1) + updatedCurrentRound,
                            undoStack = state.undoStack + listOf(state.rounds),
                            finished = true,
                        )
                    }
                    viewModelScope.launch {
                        wizardRepo.upsertRound(
                            state.value.currentGameId!!,
                            updatedCurrentRound
                        )
                        wizardRepo.finishGame(state.value.currentGameId!!)
                    }
                    return
                }
                // prepare next round
                val nextDealerIndex =
                    selectedPlayers.indexOfFirst { it?.id == currentRound.dealerId }
                        .let { if (it == -1) 0 else (it + 1) % selectedPlayers.size }
                val nextRound = WizardRoundData(
                    roundNumber = currentRound.roundNumber + 1,
                    dealerId = selectedPlayers[nextDealerIndex]?.id ?: "",
                )
                _state.update { state ->
                    state.copy(
                        rounds = state.rounds.dropLast(1) + updatedCurrentRound + nextRound,
                        undoStack = state.undoStack + listOf(state.rounds),
                    )
                }
                viewModelScope.launch {
                    wizardRepo.upsertRound(
                        state.value.currentGameId!!,
                        listOf(updatedCurrentRound, nextRound)
                    )
                }
            }
            is WizardAction.UndoLastChange -> {
                val gameId = _state.value.currentGameId ?: return
                val previousRounds = _state.value.undoStack.lastOrNull() ?: return
                val currentRounds = _state.value.rounds
                val wasFinished = _state.value.finished
                _state.update { state ->
                    state.copy(
                        rounds = previousRounds,
                        undoStack = state.undoStack.dropLast(1),
                        finished = false,
                    )
                }
                viewModelScope.launch {
                    // rounds that only exist in the undone state (e.g. the round started after FinishRound)
                    currentRounds
                        .filter { round -> previousRounds.none { it.roundNumber == round.roundNumber } }
                        .forEach { wizardRepo.deleteRound(gameId, it.roundNumber) }
                    wizardRepo.upsertRound(gameId, previousRounds.filter { it !in currentRounds })
                    if (wasFinished) wizardRepo.unfinishGame(gameId)
                }
            }
            is WizardAction.DeleteGame -> {
                viewModelScope.launch {
                    wizardRepo.deleteGame(state.value.currentGameId!!)
                }
            }
            is WizardAction.DebugSimulateRound -> {
                if (BuildConfig.DEBUG) viewModelScope.launch { simulateRound() }
            }
            else -> throw NotImplementedError("Action '$action' not implemented in WizardViewModel")
        }
    }

    /**
     * Debug helper: fills the current round with random valid values and finishes it.
     * Tricks won are randomly distributed (sum == round number), bids are tricks won +- 2.
     * Uses the regular actions, so every step is persisted and undoable.
     */
    private suspend fun simulateRound() {
        if (_state.value.finished) return
        val round = _state.value.rounds.lastOrNull() ?: return
        val players = state.value.selectedPlayers.filterNotNull()
        if (players.isEmpty()) return

        val tricks = players.associate { it.id to 0 }.toMutableMap()
        repeat(round.roundNumber) {
            val id = players.random().id
            tricks[id] = tricks.getValue(id) + 1
        }
        val bids = tricks.mapValues { (_, won) ->
            val roll = Random.nextInt(100)
            val offset = when {
                roll < 70 -> 0
                roll < 90 -> 1
                else -> 2
            } * if (Random.nextBoolean()) 1 else -1
            (won + offset).coerceIn(0, round.roundNumber)
        }

        suspend fun dispatch(action: WizardAction) {
            onAction(action)
            // wait until the derived public state caught up, as some handlers read state.value
            withTimeoutOrNull(2.seconds) {
                state.first { it.rounds == _state.value.rounds && it.finished == _state.value.finished }
            }
        }

        players.forEach { dispatch(WizardAction.OnBidChange(it.id, bids.getValue(it.id))) }
        if (!_state.value.rounds.last().bidsFinal) dispatch(WizardAction.FinishBidding)
        players.forEach { dispatch(WizardAction.OnTricksWonChange(it.id, tricks.getValue(it.id))) }
        dispatch(WizardAction.FinishRound)
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