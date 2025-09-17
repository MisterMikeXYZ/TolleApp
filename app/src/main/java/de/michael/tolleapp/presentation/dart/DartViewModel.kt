package de.michael.tolleapp.presentation.dart

import DartGameRepository
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.data.games.dart.DartGame
import de.michael.tolleapp.data.games.presets.GamePresetRepository
import de.michael.tolleapp.data.player.Player
import de.michael.tolleapp.data.player.PlayerRepository
import de.michael.tolleapp.presentation.dart.components.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import kotlin.collections.emptyList
import de.michael.tolleapp.presentation.dart.components.PlayerScoreDisplays

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

    val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState

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

    @Composable
    fun PlayerScoreDisplayFor(playerId: String, modifier: Modifier = Modifier) {
        val dartState by state.collectAsState()
        val playerState = dartState.toPlayerState(playerId)

        PlayerScoreDisplays(
            startValue = dartState.gameStyle, // 301, 501 etc.
            isActive = dartState.selectedPlayerIds.getOrNull(dartState.activePlayerIndex) == playerId,
            playerState = playerState,
            modifier = modifier.height(60.dp)
        )
    }

    fun insertThrow(value: String) {


        advancePlayer()
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
