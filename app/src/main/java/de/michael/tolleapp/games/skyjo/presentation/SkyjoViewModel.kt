package de.michael.tolleapp.games.skyjo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.games.skyjo.data.SkyjoGame
import de.michael.tolleapp.games.skyjo.data.SkyjoGameLoser
import de.michael.tolleapp.games.skyjo.data.SkyjoGameRepository
import de.michael.tolleapp.games.skyjo.data.SkyjoGameWinner
import de.michael.tolleapp.games.util.player.PlayerRepository
import de.michael.tolleapp.games.util.presets.GamePresetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID


class SkyjoViewModel(
    private val playerRepo: PlayerRepository,
    private val gameRepo: SkyjoGameRepository,
    private val presetRepo: GamePresetRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SkyjoState())
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
        SkyjoState()
    )
    val presets = presetRepo.getPresets("skyjo")
    val pausedGames: StateFlow<List<SkyjoGame>> =
        gameRepo.getPausedGames()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // collect stats + players so UI always has names
            combine(
                gameRepo.getAllPlayers(),
                playerRepo.getAllPlayers()
            ) { stats, players ->
                val namesMap = players.associate { it.id to it.name }
                namesMap
            }.collect { namesMap ->
                _state.update { it.copy(playerNames = namesMap) }
            }
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

    fun pauseCurrentGame() {
        val state = _state.value
        if (state.currentGameId.isNotEmpty()) {
            viewModelScope.launch {
                gameRepo.saveSnapshot(state.currentGameId, state.perPlayerRounds)
            }
        }
    }

    fun resumeGame(gameId: String, onResumed: (() -> Unit)? = null) {
        viewModelScope.launch {
            val rounds = gameRepo.loadGame(gameId)
            val grouped = rounds.groupBy { it.playerId }
                .mapValues { entry -> entry.value.sortedBy { it.roundIndex }.map { it.roundScore } }
            val totals = grouped.mapValues { it.value.sum() }.toMutableMap()

            val players = if (grouped.isNotEmpty()) grouped.keys.toMutableList() else _state.value.selectedPlayerIds.filterNotNull().toMutableList()

            val maxRounds = maxOf(grouped.values.maxOfOrNull { it.size } ?: 0, 5)
            val game = pausedGames.value.find { it.id == gameId }
            val dealerId = game?.dealerId

            _state.update {
                it.copy(
                    currentGameId = gameId,
                    selectedPlayerIds = players,
                    perPlayerRounds = grouped,
                    totalPoints = totals,
                    visibleRoundRows = maxRounds,
                    isGameEnded = false,
                    winnerId = listOf(null),
                    loserId = listOf(null),
                    ranking = emptyList(),
                    currentGameRounds = maxRounds,
                    currentDealerId = dealerId
                )
            }

            gameRepo.continueGame(gameId)
            onResumed?.invoke()
        }
    }

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
                        SkyjoGameWinner(gameId = gameId, playerId = playerId)
                    }
                    val loserEntities = losers.map { playerId ->
                        SkyjoGameLoser(gameId = gameId, playerId = playerId)
                    }

                    gameRepo.insertWinnersAndLosers(gameId, winnerEntities, loserEntities)
                }
            }
        }
    }

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

    fun resetSelectedPlayers() {
        _state.update { state ->
            state.copy(
                selectedPlayerIds = listOf(null, null),
            )
        }
    }

    fun startGame(dealerId: String? = null, neleModus: Boolean) {
        val newGameId = UUID.randomUUID().toString()
        _state.update { state ->
            state.copy(
                currentGameId = newGameId,
                selectedPlayerIds = state.selectedPlayerIds.filterNotNull(),
                currentDealerId = dealerId,
                neleModus = neleModus,
            )
        }
        viewModelScope.launch { gameRepo.startGame(newGameId, dealerId) }
    }

    fun resetGame() {
        _state.update { SkyjoState() }
    }

    fun deleteGame(gameId: String?) {
        val newGameId = if (gameId.isNullOrEmpty()) _state.value.currentGameId else gameId
        viewModelScope.launch { gameRepo.deleteGameCompletely(newGameId) }
        resetGame()
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

        _state.update {
            it.copy(
                perPlayerRounds = updatedRounds,
                totalPoints = updatedTotals,
                isGameEnded = false,
                winnerId = listOf(null),
                loserId = listOf(null),
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
}