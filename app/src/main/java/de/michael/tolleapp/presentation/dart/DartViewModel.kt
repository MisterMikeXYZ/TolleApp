package de.michael.tolleapp.presentation.dart

import DartGameRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.data.games.dart.DartGame
import de.michael.tolleapp.data.games.presets.GamePresetRepository
import de.michael.tolleapp.data.player.Player
import de.michael.tolleapp.data.player.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

class DartViewModel (
    private val playerRepo: PlayerRepository,
    private val gameRepo: DartGameRepository,
    private val presetRepo: GamePresetRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(DartState())
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
        DartState()
    )

    val presets = presetRepo.getPresets("dart")

    val pausedGames: StateFlow<List<DartGame>> =
        gameRepo.getActiveGames()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun addPlayer(name: String, rowIndex: Int) = viewModelScope.launch {
        val newPlayer = Player(name = name)
        playerRepo.addPlayer(name)
        selectPlayer(rowIndex, newPlayer.id)
    }

    fun selectPlayer(rowIndex: Int, playerId: String) {
        _state.update { current ->
            val updated = current.selectedPlayerIds.toMutableList()
            while (updated.size <= rowIndex) updated.add(null)
            updated[rowIndex] = playerId
            if (rowIndex == updated.lastIndex) updated.add(null)
            current.copy(selectedPlayerIds = updated)
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
        _state.update { it.copy(selectedPlayerIds = listOf(null)) }
    }

    fun startGame(gameStyle: Int) {
        val players = _state.value.selectedPlayerIds.filterNotNull()
        if (players.isEmpty()) return

        val initialScore = gameStyle
        val totals = players.associateWith { initialScore }.toMutableMap()

        val newGameId = java.util.UUID.randomUUID().toString()
        _state.update {
            it.copy(
                currentGameId = newGameId,
                selectedPlayerIds = players,
                totalPoints = totals,
                perPlayerRounds = mutableMapOf(),
                currentGameRounds = 1,
                gameStyle = gameStyle,
                isGameEnded = false,
                winnerId = null,
                loserIds = emptyList(),
                ranking = emptyList(),
                activePlayerIndex = 0
            )
        }
    }

    fun endRound(roundScores: Map<String, List<Int>>) {
        val gameId = _state.value.currentGameId
        if (gameId.isEmpty()) return

        _state.update { state ->
            val updatedRounds = state.perPlayerRounds.toMutableMap()
            val updatedTotals = state.totalPoints.toMutableMap()
            val bestRounds = state.bestRound.toMutableMap()
            val worstRounds = state.worstRound.toMutableMap()
            val perfects = state.perfectRounds.toMutableMap()
            val triple20s = state.tripleTwentyHits.toMutableMap()

            state.selectedPlayerIds.filterNotNull().forEach { playerId ->
                val darts = roundScores[playerId] ?: listOf(0, 0, 0)
                // Save round
                val playerRounds = updatedRounds[playerId]?.toMutableList() ?: mutableListOf()
                playerRounds.add(darts)
                updatedRounds[playerId] = playerRounds

                // Update total points (subtract sum of darts)
                val newTotal = (updatedTotals[playerId] ?: state.gameStyle) - darts.sum()
                updatedTotals[playerId] = newTotal

                // Update best/worst rounds
                val roundSum = darts.sum()
                bestRounds[playerId] = maxOf(bestRounds[playerId] ?: 0, roundSum)
                worstRounds[playerId] = minOf(worstRounds[playerId] ?: Int.MAX_VALUE, roundSum)

                // Update perfect rounds
                if (darts.all { it == 60 }) {
                    perfects[playerId] = (perfects[playerId] ?: 0) + 1
                }

                // Update triple 20 hits
                triple20s[playerId] = (triple20s[playerId] ?: 0) + darts.count { it == 60 }
            }

            state.copy(
                perPlayerRounds = updatedRounds,
                totalPoints = updatedTotals,
                bestRound = bestRounds,
                worstRound = worstRounds,
                perfectRounds = perfects,
                tripleTwentyHits = triple20s,
                currentGameRounds = state.currentGameRounds + 1,
                activePlayerIndex = (state.activePlayerIndex + 1) % state.selectedPlayerIds.size
            )
        }

        checkEndCondition()
    }

    private fun checkEndCondition() {
        val totals = _state.value.totalPoints
        val winner = totals.entries.find { it.value <= 0 }?.key
        if (winner != null) {
            _state.update {
                it.copy(
                    isGameEnded = true,
                    winnerId = winner,
                    loserIds = it.selectedPlayerIds.filterNotNull().filter { pid -> pid != winner },
                    ranking = totals.entries.sortedBy { it.value }.map { e -> e.key }
                )
            }
        }
    }

    fun resumeGame(gameId: String) = viewModelScope.launch {
        val (roundsByPlayer, gameStyle) = gameRepo.loadGame(gameId)

        val players = roundsByPlayer.keys.toList()

        val totals = players.associateWith { roundsByPlayer[it]?.sumOf { round -> round.sum() } ?: 0 }
        val best = players.associateWith { roundsByPlayer[it]?.maxOfOrNull { it.sum() } ?: 0 }
        val worst = players.associateWith { roundsByPlayer[it]?.minOfOrNull { it.sum() } ?: 0 }
        val perfects = players.associateWith { roundsByPlayer[it]?.count { it.sum() == 180 } ?: 0 }
        val triple20s = players.associateWith { roundsByPlayer[it]?.sumOf { it.count { d -> d == 60 } } ?: 0 }

        _state.update {
            it.copy(
                currentGameId = gameId,
                selectedPlayerIds = players,
                perPlayerRounds = roundsByPlayer,
                totalPoints = totals,
                bestRound = best,
                worstRound = worst,
                perfectRounds = perfects,
                tripleTwentyHits = triple20s,
                currentGameRounds = roundsByPlayer.values.maxOfOrNull { it.size } ?: 1,
                activePlayerIndex = 0,
                gameStyle = gameStyle,
                isGameEnded = false
            )
        }
    }

    fun endGame() = viewModelScope.launch {
        val stateCopy = _state.value
        val totals = stateCopy.totalPoints

        if (totals.isEmpty()) return@launch

        val sorted = totals.entries.sortedByDescending { it.value }
        val maxScore = sorted.first().value

        val winners = sorted.filter { it.value == maxScore }.map { it.key }.firstOrNull()
        val minScore = sorted.last().value
        val losers = sorted.filter { it.value == minScore }.map { it.key }

        _state.update {
            it.copy(
                isGameEnded = true,
                winnerId = winners,
                loserIds = losers,
                ranking = sorted.map { it.key }
            )
        }

        // mark game as finished in DB
        sorted.forEach { (playerId, _) ->
            // Optional: could update stats repository if needed
        }
    }

    fun finishGame() {
        viewModelScope.launch {
            val gameId = _state.value.currentGameId
            if (gameId.isEmpty()) return@launch

            val totals = _state.value.totalPoints
            val maxScore = totals.values.maxOrNull() ?: 0
            val winner = totals.filter { it.value == maxScore }.keys.firstOrNull()

            // Update DB
            val game = gameRepo.getAllGames().find { it.id == gameId } ?: return@launch
            gameRepo.updateGame(game.copy(isFinished = true, endedAt = System.currentTimeMillis(), winnerId = winner))

            // Update state
            _state.update {
                it.copy(
                    isGameEnded = true,
                    winnerId = winner,
                    ranking = totals.entries.sortedByDescending { e -> e.value }.map { it.key }
                )
            }
        }
    }

    fun deleteGame() {
        viewModelScope.launch {
            val gameId = _state.value.currentGameId
            val game = gameRepo.getAllGames().find { it.id == gameId } ?: return@launch
            gameRepo.deleteGame(game)
            resetGame()
        }
    }

    fun resetGame() {
        _state.update {
            DartState(
                currentGameId = "",
                currentGameRounds = 1,
                selectedPlayerIds = listOf(null),
                perPlayerRounds = emptyMap(),
                totalPoints = emptyMap(),
                bestRound = emptyMap(),
                worstRound = emptyMap(),
                perfectRounds = emptyMap(),
                tripleTwentyHits = emptyMap(),
                winnerId = null,
                loserIds = listOf(null),
                ranking = emptyList(),
                isGameEnded = false,
                activePlayerIndex = 0
            )
        }
    }

    fun advancePlayer() {
        val totalPlayers = _state.value.selectedPlayerIds.filterNotNull().size
        if (totalPlayers == 0) return
        _state.update { it.copy(activePlayerIndex = (it.activePlayerIndex + 1) % totalPlayers) }
    }
}
