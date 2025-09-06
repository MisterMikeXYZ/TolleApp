package de.michael.tolleapp.presentation.skyjo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.data.player.Player
import de.michael.tolleapp.data.player.PlayerRepository
import de.michael.tolleapp.data.games.skyjo.game.SkyjoGameRepository
import de.michael.tolleapp.data.games.skyjo.stats.SkyjoStatsRepository
import de.michael.tolleapp.data.games.skyjo.game.SkyjoGame
import de.michael.tolleapp.data.games.presets.GamePresetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID


class SkyjoViewModel(
    private val skyjoStatsRepository: SkyjoStatsRepository,
    private val playerRepository: PlayerRepository,
    private val gameRepository: SkyjoGameRepository,
    private val presetRepository: GamePresetRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SkyjoState())
    val state: StateFlow<SkyjoState> = _state.asStateFlow()
    val presets = presetRepository.getPresets("skyjo")

    val pausedGames: StateFlow<List<SkyjoGame>> =
        gameRepository.getPausedGames()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // collect stats + players so UI always has names
            combine(
                skyjoStatsRepository.getPlayers(),
                playerRepository.getAllPlayers()
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
            presetRepository.createPreset(gameType, name, playerIds)
        }
    }

    fun deletePreset(presetId: Long) {
        viewModelScope.launch {
            presetRepository.deletePreset(presetId)
        }
    }

    fun pauseCurrentGame() {
        val state = _state.value
        if (state.currentGameId.isNotEmpty()) {
            viewModelScope.launch {
                gameRepository.saveSnapshot(state.currentGameId, state.perPlayerRounds)
            }
        }
    }

    fun resumeGame(gameId: String, onResumed: (() -> Unit)? = null) {
        viewModelScope.launch {
            val rounds = gameRepository.loadGame(gameId)
            val grouped = rounds.groupBy { it.playerId }
                .mapValues { entry -> entry.value.sortedBy { it.roundIndex }.map { it.roundScore } }

            val totals = grouped.mapValues { e -> e.value.sum() }.toMutableMap()

            val players = if (grouped.isNotEmpty()) {
                grouped.keys.toMutableList<String?>()
            } else {
                _state.value.selectedPlayerIds.filterNotNull().toMutableList<String?>()
            }

            if (players.isEmpty() || players.lastOrNull() != null) {
                players.add(null)
            }

            val maxRounds = maxOf(grouped.values.maxOfOrNull { it.size } ?: 0, 5)

            _state.update {
                it.copy(
                    currentGameId = gameId,
                    selectedPlayerIds = players,
                    perPlayerRounds = grouped.toMutableMap(),
                    totalPoints = totals,
                    visibleRoundRows = maxRounds,
                    isGameEnded = false,
                    winnerId = listOf(null),
                    loserId = listOf(null),
                    ranking = emptyList(),
                    currentGameRounds = maxRounds,
                )
            }


            val totalRoundsPlayed = grouped.values.maxOfOrNull { it.size } ?: 0
            val updatedPlayers = _state.value.selectedPlayerIds.filterNotNull()
            val dealerIndex = if (updatedPlayers.isNotEmpty()) totalRoundsPlayed % updatedPlayers.size else 0

            _state.update {
                    it.copy(dealerIndex = dealerIndex)
            }
            gameRepository.continueGame(gameId)
            onResumed?.invoke()
        }
    }

    fun endRound(points: Map<String, String>) {
        viewModelScope.launch {
            var newState: SkyjoState? = null

            _state.update { state ->
                val updatedRounds = state.perPlayerRounds.toMutableMap()
                val updatedTotals = state.totalPoints.toMutableMap()

                state.selectedPlayerIds.filterNotNull().forEach { playerId ->
                    val score = points[playerId]?.toIntOrNull() ?: 0

                    val currentPlayerRounds = updatedRounds[playerId]?.toMutableList() ?: mutableListOf()
                    currentPlayerRounds.add(score)
                    updatedRounds[playerId] = currentPlayerRounds
                    updatedTotals[playerId] = (updatedTotals[playerId] ?: 0) + score
                }

                val maxRounds = updatedRounds.values.maxOfOrNull { it.size } ?: 0
                var visible = state.visibleRoundRows
                while (maxRounds > visible) visible += 1

                val updatedState = state.copy(
                    perPlayerRounds = updatedRounds,
                    totalPoints = updatedTotals,
                    visibleRoundRows = visible
                )

                newState = updatedState
                updatedState
            }

            // ✅ write to DB AFTER state is consistent
            newState?.let { state ->
                val nextRoundIndex = (state.perPlayerRounds.values.maxOfOrNull { it.size } ?: 0)
                state.selectedPlayerIds.filterNotNull().forEach { playerId ->
                    val score = state.perPlayerRounds[playerId]?.lastOrNull() ?: 0
                    gameRepository.ensureSession(state.currentGameId)
                    gameRepository.addRound(
                        state.currentGameId,
                        playerId,
                        nextRoundIndex,
                        score
                    )
                }
            }

            // ✅ check end condition after everything
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
            // all others are neither winners nor losers
            val ranking = totals.entries.sortedBy { it.value }.map { it.key }
            _state.update {
                it.copy(
                    isGameEnded = true,
                    winnerId = winners, //if (winners.size == 1) winners.first() else null,
                    loserId = losers,
                    ranking = ranking
                )
            }
        }
    }

    fun addPlayer(name: String, rowIndex: Int) = viewModelScope.launch {
        val newPlayer = Player(name = name)
        val added = skyjoStatsRepository.addPlayer(newPlayer)
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

    fun resetSelectedPlayers() {
        _state.update { state ->
            state.copy(
                selectedPlayerIds = listOf(null, null),
            )
        }
    }

    fun startGame() {
        // Logic to start the game, e.g., navigating to the game screen
        // Create a new game ID
        val newGameId = UUID.randomUUID().toString()
        _state.update { state ->
            state.copy(
                currentGameId = newGameId,
                selectedPlayerIds = state.selectedPlayerIds.filterNotNull()
            )
        }

        viewModelScope.launch { gameRepository.startGame(newGameId) }
    }

    fun resetGame() {
        _state.update { state ->
            state.copy(
                currentGameId = "",
                selectedPlayerIds = listOf(null, null), // two empty slots
                perPlayerRounds = mutableMapOf(),
                totalPoints = mutableMapOf(),
                visibleRoundRows = 5,
                isGameEnded = false,
                winnerId = listOf(null),
                loserId = listOf(null),
                ranking = emptyList(),
                currentGameRounds = 0,
                dealerIndex = 0,
            )
        }
    }

    fun deleteGame() {
        val gameId = _state.value.currentGameId
        if (gameId.isNotEmpty()) {
            viewModelScope.launch { gameRepository.deleteGameCompletely(gameId) }
        }
        resetGame()
    }

    fun endGame() {
        viewModelScope.launch {
            val gameId = _state.value.currentGameId
            if (gameId.isNotEmpty()) {
                // read per-player rounds from Game DB
                val roundsByPlayer = gameRepository.getRoundsGroupedByPlayer(gameId)

                val winners = _state.value.winnerId.filterNotNull()
                val losers = _state.value.loserId.filterNotNull()

                roundsByPlayer.forEach { (playerId, rounds) ->
                    val isWinner = winners.contains(playerId)
                    val isLoser = losers.contains(playerId)
                    skyjoStatsRepository.finalizePlayerStats(playerId, rounds, isWinner, isLoser)
                }

                // mark and purge the finished session (don’t keep in temp DB)
                gameRepository.markEnded(gameId)
                gameRepository.deleteGameCompletely(gameId)
            }
        }
    }

    fun advanceDealer(totalPlayers: Int) {
        _state.update { state ->
            state.copy(
                dealerIndex = (state.dealerIndex + 1) % totalPlayers
            )
        }
    }
}