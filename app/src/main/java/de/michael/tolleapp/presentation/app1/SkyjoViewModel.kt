package de.michael.tolleapp.presentation.app1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.data.skyjo.game.SkyjoGameRepository
import de.michael.tolleapp.data.skyjo.player.PlayerRepository
import de.michael.tolleapp.data.skyjo.player.SkyjoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID


class SkyjoViewModel(
    private val repository: PlayerRepository,
    private val gameRepository: SkyjoGameRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SkyjoState())
    val state: StateFlow<SkyjoState> = _state.asStateFlow()

    //Flow<List<SkyjoGame>>
    // 👇 make the type explicit so Kotlin can’t infer the wrong thing
    val pausedGames: StateFlow<List<de.michael.tolleapp.data.skyjo.game.SkyjoGame>> =
        gameRepository.getPausedGames()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.getPlayers().collect { players ->
                _state.update { it.copy(players = players) }
            }
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
            val players = grouped.keys.toMutableList<String?>()

            val maxRounds = maxOf(grouped.values.maxOfOrNull { it.size } ?: 0, 5)

            // 👇 ensure trailing null for UI consistency
            if (players.isEmpty() || players.lastOrNull() != null) {
                players.add(null)
            }

            _state.update {
                it.copy(
                    currentGameId = gameId,
                    selectedPlayerIds = players, // 👈 restore players
                    perPlayerRounds = grouped.toMutableMap(),
                    totalPoints = totals,
                    visibleRoundRows = maxRounds, // 👈 restore row count
                    isGameEnded = false,          // just in case
                    winnerId = null,
                    ranking = emptyList()
                )
            }
            gameRepository.continueGame(gameId)
            onResumed?.invoke()
        }
    }

    fun endRound(points: Map<String, String>) {
        _state.update { state ->
            val updatedRounds = state.perPlayerRounds.toMutableMap()
            val updatedTotals = state.totalPoints.toMutableMap()
            val nextRoundIndex = (state.perPlayerRounds.values.maxOfOrNull { it.size } ?: 0) + 1

            state.selectedPlayerIds.filterNotNull().forEach { playerId ->
                val score = points[playerId]?.toIntOrNull() ?: 0

                // 👉 NEW: write round to the Game DB only
                viewModelScope.launch {
                    gameRepository.ensureSession(state.currentGameId)
                    gameRepository.addRound(
                        state.currentGameId,
                        playerId,
                        nextRoundIndex,
                        score
                    )
                }

                val currentRounds = updatedRounds[playerId]?.toMutableList() ?: mutableListOf()
                currentRounds.add(score)
                updatedRounds[playerId] = currentRounds
                updatedTotals[playerId] = (updatedTotals[playerId] ?: 0) + score
            }

            val maxRounds = updatedRounds.values.maxOfOrNull { it.size } ?: 0
            var visible = state.visibleRoundRows
            while (maxRounds > visible) visible += 1

            state.copy(
                perPlayerRounds = updatedRounds,
                totalPoints = updatedTotals,
                visibleRoundRows = visible
            )
        }

        checkEndCondition()
    }

    private fun checkEndCondition() {
        val totals = _state.value.totalPoints
        val hasLoser = totals.values.any { it >= 100 }
        if (hasLoser) {
            val ranking = totals.entries.sortedBy { it.value }.map { it.key }
            _state.update {
                it.copy(
                    isGameEnded = true,
                    winnerId = ranking.first(),
                    ranking = ranking
                )
            }
        }
    }

    fun addPlayer(name: String, rowIndex: Int) = viewModelScope.launch {
        val newPlayer = SkyjoPlayer(name = name)
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

    fun endGame() {
        _state.update { state ->
            state.copy(
                currentGameId = "",
                selectedPlayerIds = listOf(null, null), // two empty slots
                perPlayerRounds = mutableMapOf(),
                totalPoints = mutableMapOf(),
                visibleRoundRows = 5,
                isGameEnded = false,
                winnerId = null,
                ranking = emptyList()
            )
        }
    }

    fun deleteGame() {
        val gameId = _state.value.currentGameId
        if (gameId.isNotEmpty()) {
            viewModelScope.launch { gameRepository.deleteGameCompletely(gameId) }
        }
        endGame()
    }

    fun navigateToEndScreen() {
        viewModelScope.launch {
            val gameId = _state.value.currentGameId
            if (gameId.isNotEmpty()) {
                // read per-player rounds from Game DB
                val roundsByPlayer = gameRepository.getRoundsGroupedByPlayer(gameId)

                // update player stats based on *this* finished game
                roundsByPlayer.forEach { (playerId, rounds) ->
                    // per-round stats (best/worst rounds, totals)
                    rounds.forEach { repository.updateRoundStats(playerId, it) }
                    repository.updateEndStats(playerId, rounds.sum())
                }

                // mark and purge the finished session (don’t keep in temp DB)
                gameRepository.markEnded(gameId)
                gameRepository.deleteGameCompletely(gameId)
            }
        }
    }
}