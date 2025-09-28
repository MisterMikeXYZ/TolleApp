package de.michael.tolleapp.games.skyjo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.games.skyjo.data.entities.SkyjoPlayerEntity
import de.michael.tolleapp.games.skyjo.domain.SkyjoRepository
import de.michael.tolleapp.games.skyjo.domain.SkyjoRoundData
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.games.util.player.PlayerRepository
import de.michael.tolleapp.games.util.presets.GamePresetRepository
import de.michael.tolleapp.games.util.startScreen.StartAction
import de.michael.tolleapp.games.util.table.SortDirection
import de.michael.tolleapp.games.wizard.domain.WizardRoundData
import de.michael.tolleapp.games.wizard.presentation.WizardAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.collections.first
import kotlin.collections.set
import kotlin.math.abs

class SkyjoViewModel(
    private val gameRepo: SkyjoRepository,
    private val playerRepo: PlayerRepository,
    private val presetRepo: GamePresetRepository
): ViewModel() {
    private val _allPlayers = playerRepo.getAllPlayers()
    private val _presets = presetRepo.getPresets(GameType.SKYJO.toString())
    private val _pausedGames = gameRepo.getPausedGames().getOrThrow()
    private val _selectedPlayerIds = MutableStateFlow<List<String?>>(listOf(null, null))
    private val _state = MutableStateFlow(SkyjoState())

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
            initialValue = SkyjoState()
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
                presetRepo.createPreset(GameType.WIZARD.toString(), action.presetName, action.playerIds)
            }
            is StartAction.SelectPreset -> viewModelScope.launch {
                val players = state.value.presets.first { it.preset.id == action.presetId }.players
                _selectedPlayerIds.update { listOf(null, null, null) }
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
                _state.update { state -> state.copy(
                    currentGameId = gameId,
                    rounds = listOf(
                        SkyjoRoundData(
                            roundNumber = 1,
                            dealerId = this.state.value.selectedPlayers.first()!!.id
                        )
                    )
                ) }
                viewModelScope.launch {
                    gameRepo.createGame(gameId = gameId, endPoints = state.value.endPoints)
                    state.value.selectedPlayers.filterNotNull().forEach { player ->
                        gameRepo.addPlayerToGame(gameId = gameId, playerId = player.id)
                    }
                    gameRepo.upsertRound(
                        gameId,
                        state.value.rounds.first()
                    )
                }
            }
            is StartAction.ResumeGame -> {
                viewModelScope.launch {
                    val pausedGame = gameRepo.getGameById(action.gameId)
                        .getOrThrow()
                        .first()
                        ?: throw IllegalStateException("Could not find game #${action.gameId}")
                    _selectedPlayerIds.update { pausedGame.playerIds }
                    _state.update { state -> state.copy(
                        currentGameId = pausedGame.id,
                        endPoints = pausedGame.endPoints,
                        rounds = pausedGame.rounds,
                    ) }
                }
            }
            is StartAction.DeleteGame -> {
                viewModelScope.launch {
                    gameRepo.deleteGame(action.gameId)
                }
            }

            else -> throw NotImplementedError("Action '$action' not implemented in WizardViewModel")
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




    fun onAction(action: SkyjoAction) {
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
            is WizardAction.OnTricksWonChange -> {
                val currentRound = state.value.rounds.last()
                val newRound = currentRound.copy(
                    tricksWon = currentRound.tricksWon.toMutableMap().apply {
                        this[action.playerId] = action.newValue
                    }
                )
                _state.update { state ->
                    state.copy(rounds = state.rounds.dropLast(1) + newRound)
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
            WizardAction.FinishBidding -> {
                val currentRound = state.value.rounds.last()
                val newRound = currentRound.copy(
                    bidsFinal = true
                )
                _state.update { state ->
                    state.copy(rounds = state.rounds.dropLast(1) + newRound)
                }
                viewModelScope.launch {
                    wizardRepo.upsertRound(
                        state.value.currentGameId!!,
                        newRound
                    )
                }
            }
            WizardAction.FinishRound -> {
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
                    state.copy(rounds = state.rounds.dropLast(1) + updatedCurrentRound + nextRound)
                }
                viewModelScope.launch {
                    wizardRepo.upsertRound(
                        state.value.currentGameId!!,
                        listOf(updatedCurrentRound, nextRound)
                    )
                }
            }
            WizardAction.DeleteGame -> {
                viewModelScope.launch {
                    wizardRepo.deleteGame(state.value.currentGameId!!)
                }
            }
            else -> throw NotImplementedError("Action '$action' not implemented in WizardViewModel")
        }
    }




    // Game logic
    fun endRound(points: Map<String, String>) {
        viewModelScope.launch {
            val stateValue = _state.value
            val updatedRounds = stateValue.perPlayerRounds.toMutableMap()
            val updatedTotals = stateValue.totalPoints.toMutableMap()

            stateValue.selectedPlayerIds.filterNotNull().forEach { playerId ->
                val score = points[playerId]?.toIntOrNull() ?: 0
                val currentPlayerRounds = updatedRounds[playerId]?.toMutableList() ?: mutableListOf()
                currentPlayerRounds.add(score)
                updatedRounds[playerId] = currentPlayerRounds
                updatedTotals[playerId] = (updatedTotals[playerId] ?: 0) + score
            }

            _state.update {
                it.copy(
                    perPlayerRounds = updatedRounds,
                    totalPoints = updatedTotals,
                    visibleRoundRows = maxOf(it.visibleRoundRows, updatedRounds.values.maxOfOrNull { r -> r.size } ?: 5)
                )
            }

            // save rounds
            val nextRoundIndex = updatedRounds.values.maxOfOrNull { it.size } ?: 0
            stateValue.selectedPlayerIds.filterNotNull().forEach { playerId ->
                val score = updatedRounds[playerId]?.lastOrNull() ?: 0
                gameRepo.ensureSession(stateValue.currentGameId)
                gameRepo.addRound(stateValue.currentGameId, playerId, nextRoundIndex, score)
            }

            advanceDealer()
            checkEndCondition()
        }
    }

    private fun checkEndCondition() {
        val totals = _state.value.totalPoints
        val hasLoser = totals.values.any { it >= 100 }

        if (hasLoser) {
            val sortedEntries = totals.entries.sortedBy { it.value }
            val lowestScore = sortedEntries.first().value
            val winners = sortedEntries.filter { it.value == lowestScore && it.value <= 99 }.map { it.key }
            val highestScore = sortedEntries.last().value
            val losers = sortedEntries.filter { it.value == highestScore && it.value >= 100 }.map { it.key }
            val ranking = totals.entries.sortedBy { it.value }.map { it.key }

            _state.update {
                it.copy(
                    isGameEnded = true,
                    winnerId = winners,
                    loserId = losers,
                    ranking = ranking
                )
            }

            // --- Persist winners and losers in DB ---
            viewModelScope.launch {
                val gameId = _state.value.currentGameId
                if (gameId.isNotEmpty()) {
                    val winnerEntities = winners.map { playerId ->
                        SkyjoPlayerEntity(gameId = gameId, playerId = playerId)
                    }
                    val loserEntities = losers.map { playerId ->
                        SkyjoGameLoser(gameId = gameId, playerId = playerId)
                    }

                    gameRepo.insertWinnersAndLosers(gameId, winnerEntities, loserEntities)
                }
            }
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

    fun resetGame() {
        _state.update { SkyjoState() }
    }

    fun endGame() {
        viewModelScope.launch {
            val gameId = _state.value.currentGameId
            gameRepo.markEnded(gameId)
        }
    }

    fun setDealer(dealerId: String?) {
        val gameId = _state.value.currentGameId
        if (gameId.isEmpty()) return
        viewModelScope.launch {
            gameRepo.setDealer(gameId, dealerId)
            _state.update { it.copy(currentDealerId = dealerId) }
        }
    }

    fun advanceDealer() {
        val currentDealerId = _state.value.currentDealerId
        val players = _state.value.selectedPlayerIds.filterNotNull()
        if (players.isNotEmpty()) {
            val currentIndex = players.indexOf(currentDealerId)
            val nextIndex =
                if (currentIndex == -1 || currentIndex + 1 >= players.size) 0 else currentIndex + 1
            val nextDealerId = players[nextIndex]
            setDealer(nextDealerId)
        }
    }

    suspend fun undoLastRound(): Boolean {
        val stateValue = _state.value
        if (stateValue.perPlayerRounds.isEmpty()) return false

        if (stateValue.isGameEnded) gameRepo.markNotEnded(stateValue.currentGameId)

        val updatedRounds = stateValue.perPlayerRounds.toMutableMap()
        val updatedTotals = stateValue.totalPoints.toMutableMap()

        val lastRoundIndex = updatedRounds.values.maxOfOrNull { it.size } ?: return false
        if (lastRoundIndex == 0) return false

        stateValue.selectedPlayerIds.filterNotNull().forEach { playerId ->
            val currentRounds = updatedRounds[playerId]?.toMutableList() ?: return@forEach
            if (currentRounds.isNotEmpty()) {
                val removedScore = currentRounds.removeAt(currentRounds.lastIndex)
                updatedRounds[playerId] = currentRounds
                updatedTotals[playerId] = (updatedTotals[playerId] ?: 0) - removedScore

                gameRepo.removeLastRound(stateValue.currentGameId, playerId)
            }
        }

        val visibleRoundRows = if (_state.value.visibleRoundRows <= 5) 5 else _state.value.visibleRoundRows - 1

        _state.update {
            it.copy(
                perPlayerRounds = updatedRounds,
                totalPoints = updatedTotals,
                isGameEnded = false,
                winnerId = listOf(null),
                loserId = listOf(null),
                visibleRoundRows = visibleRoundRows
            )
        }
        reverseDealer()
        return true
    }

    fun reverseDealer() {
        val currentDealerId = _state.value.currentDealerId
        val players = _state.value.selectedPlayerIds.filterNotNull()
        if (players.isNotEmpty()) {
            val currentIndex = players.indexOf(currentDealerId)
            val prevIndex =
                if (currentIndex <= 0) players.lastIndex else currentIndex - 1
            val prevDealerId = players[prevIndex]
            setDealer(prevDealerId)
        }
    }

    fun setLastKeyboardPage(page: Int) {
        _state.update { it.copy(lastKeyboardPage = page) }
    }

    fun setSortDirection(newDirection: SortDirection) {
        _state.update { it.copy(sortDirection = newDirection) }
    }
}