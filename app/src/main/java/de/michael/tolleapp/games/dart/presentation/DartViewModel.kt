package de.michael.tolleapp.games.dart.presentation

import DartGameRepository
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.games.dart.data.DartGameRound
import de.michael.tolleapp.games.dart.data.DartThrowData
import de.michael.tolleapp.games.dart.presentation.components.*
import de.michael.tolleapp.games.util.player.Player
import de.michael.tolleapp.games.util.player.PlayerRepository
import de.michael.tolleapp.games.util.presets.GamePresetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class DartViewModel(
    private val playerRepo: PlayerRepository,
    private val gameRepo: DartGameRepository,
    private val presetRepo: GamePresetRepository,
) : ViewModel() {

    private val throwStack = mutableListOf<ThrowAction>()
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

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState

    val presets = presetRepo.getPresets("dart")

    val pausedGames: StateFlow<List<de.michael.tolleapp.games.dart.data.DartGame>> =
        gameRepo.getActiveGames()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // --- Presets & players ---

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
        val added = gameRepo.addPlayer(newPlayer)
        if (added) {
            selectPlayer(rowIndex, newPlayer.id)
        }
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

    // --- UI composable ---

    @Composable
    fun PlayerScoreDisplayFor(playerId: String, modifier: Modifier = Modifier) {
        val dartState by state.collectAsState()
        val playerState = dartState.toPlayerState(playerId)

        PlayerScoreDisplays(
            startValue = dartState.gameStyle,
            isActive = dartState.selectedPlayerIds.getOrNull(dartState.activePlayerIndex) == playerId,
            playerState = playerState,
            modifier = modifier.height(60.dp)
        )
    }

    // --- Game lifecycle ---

    fun startGame(gameStyle: Int) {
        val players = _state.value.selectedPlayerIds.filterNotNull()
        if (players.isEmpty()) return

        val newGameId = UUID.randomUUID().toString()

        // Reset in-memory state and set up new game
        resetGame()
        _state.update {
            it.copy(
                currentGameId = newGameId,
                selectedPlayerIds = players,
                gameStyle = gameStyle,
            )
        }

        // Persist a fresh game row immediately so the game exists in the DB
        viewModelScope.launch {
            val game = de.michael.tolleapp.games.dart.data.DartGame(
                id = newGameId,
                isFinished = false,
                winnerId = null,
                gameStyle = gameStyle,
                ranking = "",
                activePlayerIndex = 0,
                totalPlayers = players.size
            )
            gameRepo.insertGame(game)
        }
    }

    fun endGame() = viewModelScope.launch {
        val ranking = _state.value.ranking.toMutableList()
        val loserId = ranking[ranking.size - 1]
        _state.update {
            it.copy(
                isGameEnded = true,
                loserId = loserId,
            )
        }
        // Persist final snapshot
        saveCurrentGame()
    }

    fun deleteGame(gameId: String? = null) {
        val newGameId = if (gameId.isNullOrEmpty()) _state.value.currentGameId else gameId
        viewModelScope.launch { gameRepo.deleteGameCompletely(newGameId) }
        resetGame()
    }

    fun resetGame() {
        _state.update {
            DartState(
                currentGameId = "",
                selectedPlayerIds = listOf(null),
                perPlayerRounds = emptyMap(),
                winnerId = null,
                loserId = null,
                ranking = emptyList(),
                isGameEnded = false,
                activePlayerIndex = 0
            )
        }
        throwStack.clear()
    }

    // --- Throws & rounds ---

    fun insertThrow(playerId: String, value: String, multiplier: String) {
        val fieldValue = value.toInt()
        val isDouble = multiplier == "Double"
        val isTriple = multiplier == "Triple"

        val playerRounds: MutableList<MutableList<ThrowData>> =
            state.value.perPlayerRounds[playerId]
                ?.map { it.toMutableList() }
                ?.toMutableList()
                ?: mutableListOf()

        val lastRound = playerRounds.lastOrNull()
        val bustCheck = lastRound?.any { it.isBust } ?: false

        if (playerRounds.isEmpty() || playerRounds.last().size == 3 || bustCheck) {
            playerRounds.add(mutableListOf())
        }

        val throwIndex = playerRounds.last().size
        val throwData = ThrowData(fieldValue, isDouble, isTriple, false, throwIndex)

        playerRounds.last().add(throwData)

        val totalRoundPoints = playerRounds.sumOf { round ->
            round.mapNotNull { it.calcScore() }.sum()
        }

        throwStack.add(
            ThrowAction(playerId, throwData, playerRounds.lastIndex)
        )

        _state.value = state.value.copy(
            perPlayerRounds = state.value.perPlayerRounds.toMutableMap().apply {
                this[playerId] = playerRounds
            }
        )

        // Bust handling
        if (_state.value.gameStyle - totalRoundPoints < 0) {
            bust(playerId)
            advancePlayer()
            // Persist after bust + turn advance
            saveCurrentGame()
            return
        }

        // If not finished and this was the 3rd dart, advance to next player
        if (!checkEndConditionForPlayer(playerId) && throwIndex == 2) {
            advancePlayer()
        }

        // Persist after a successful throw (or round end)
        saveCurrentGame()
    }

    fun bust(playerId: String) {
        val playerRounds: MutableList<MutableList<ThrowData>> =
            state.value.perPlayerRounds[playerId]
                ?.map { it.toMutableList() }
                ?.toMutableList()
                ?: mutableListOf()

        if (playerRounds.isNotEmpty()) {
            playerRounds.last().forEach { it.isBust = true }
            _state.value = state.value.copy(
                perPlayerRounds = state.value.perPlayerRounds.toMutableMap().apply {
                    this[playerId] = playerRounds
                }
            )
        }
    }

    fun undoThrow() {
        val lastAction = throwStack.removeLastOrNull() ?: return

        val playerRounds = state.value.perPlayerRounds[lastAction.playerId]
            ?.map { it.toMutableList() }
            ?.toMutableList() ?: return

        val round = playerRounds.getOrNull(lastAction.roundIndex) ?: return

        // Remove the throw
        val removedThrow = round.removeLastOrNull() ?: return

        if (round.any { it.isBust }) {
            // Reset all remaining throws in the round to "not bust"
            for (throwData in round) {
                throwData.isBust = false
            }
        }
        // Remove empty round
        if (round.isEmpty()) {
            playerRounds.removeAt(lastAction.roundIndex)
        }

        val totalRoundPoints = playerRounds.sumOf { r ->
            r.mapNotNull { it.calcScore() }.sum()
        }
        val remaining = state.value.gameStyle - totalRoundPoints

        val newRanking = state.value.ranking.toMutableList()
        if (remaining > 0 && newRanking.contains(lastAction.playerId)) {
            newRanking.remove(lastAction.playerId)
        }

        // Update state
        _state.update {
            it.copy(
                perPlayerRounds = it.perPlayerRounds.toMutableMap().apply {
                    this[lastAction.playerId] = playerRounds
                },
                activePlayerIndex = state.value.selectedPlayerIds
                    .filterNotNull()
                    .indexOf(lastAction.playerId), // focus back on undone player
                ranking = newRanking
            )
        }

        // Persist after undo to keep DB in sync
        saveCurrentGame()
    }

    private fun checkEndConditionForPlayer(playerId: String): Boolean {
        val playerRounds: MutableList<MutableList<ThrowData>> =
            state.value.perPlayerRounds[playerId]
                ?.map { it.toMutableList() }
                ?.toMutableList()
                ?: mutableListOf()
        val totalRoundPoints = playerRounds.sumOf { round ->
            round.mapNotNull { it.calcScore() }.sum()
        }
        if (_state.value.gameStyle - totalRoundPoints != 0) return false

        val ranking = _state.value.ranking.toMutableList()
        if (!ranking.contains(playerId)) {
            ranking.add(playerId)
        }
        _state.update {
            it.copy(
                ranking = ranking,
                winnerId = if (ranking.size == 1) playerId else it.winnerId,
            )
        }
        advancePlayer()

        if (_state.value.selectedPlayerIds.filterNotNull().size == ranking.size) {
            // Everyone finished
            viewModelScope.launch { endGame() }
        }
        return true
    }

    fun advancePlayer() {
        val totalPlayers = _state.value.selectedPlayerIds.filterNotNull().size
        if (totalPlayers == 0) return
        var newIndex = (_state.value.activePlayerIndex + 1) % totalPlayers
        val finished = _state.value.ranking.toSet()
        val activeIds = _state.value.selectedPlayerIds.filterNotNull()
        while (finished.contains(activeIds[newIndex])) {
            newIndex = (newIndex + 1) % totalPlayers
            if (_state.value.ranking.size == totalPlayers) {
                return
            }
        }
        _state.update { it.copy(activePlayerIndex = newIndex) }
    }

    // --- Persistence ---

    fun saveCurrentGame() = viewModelScope.launch {
        val dartState = _state.value
        if (dartState.currentGameId.isEmpty()) return@launch

        val players = dartState.selectedPlayerIds.filterNotNull()

        // Upsert game row
        val game = de.michael.tolleapp.games.dart.data.DartGame(
            id = dartState.currentGameId,
            isFinished = dartState.isGameEnded,
            winnerId = dartState.winnerId,
            gameStyle = dartState.gameStyle,
            ranking = dartState.ranking.joinToString(","),
            activePlayerIndex = dartState.activePlayerIndex,
            totalPlayers = players.size,
            endedAt = if (dartState.isGameEnded) System.currentTimeMillis() else null
        )
        gameRepo.insertGame(game)

        // Clear existing rounds (and cascaded throws) to avoid duplicates, then rewrite snapshot
        val existingRounds = gameRepo.getRoundsForGame(dartState.currentGameId)
        existingRounds.forEach { round -> gameRepo.deleteRound(round) }

        players.forEach { playerId ->
            val rounds = dartState.perPlayerRounds[playerId] ?: mutableListOf()

            if (rounds.isEmpty()) {
                // keep one empty round to indicate participation
                val emptyRound = DartGameRound(
                    gameId = dartState.currentGameId,
                    playerId = playerId,
                    roundIndex = 0,
                    isBust = false
                )
                gameRepo.insertRound(emptyRound)
            } else {
                rounds.forEachIndexed { roundIndex, throwsList ->
                    val round = DartGameRound(
                        gameId = dartState.currentGameId,
                        playerId = playerId,
                        roundIndex = roundIndex,
                        isBust = throwsList.any { it.isBust }
                    )
                    val roundId = gameRepo.insertRound(round)

                    throwsList.forEachIndexed { throwIndex, td ->
                        val dartThrow = DartThrowData(
                            roundId = roundId,
                            throwIndex = throwIndex,
                            fieldValue = td.fieldValue,
                            isDouble = td.isDouble,
                            isTriple = td.isTriple
                        )
                        gameRepo.insertThrow(dartThrow)
                    }
                }
            }
        }
    }

    fun resumeGame(gameId: String) = viewModelScope.launch {
        val gameWithRounds = gameRepo.getGameWithRounds(gameId) ?: return@launch
        val game = gameWithRounds.game

        val playerIds = gameWithRounds.rounds.map { it.round.playerId }.distinct()
        val perPlayerRounds = mutableMapOf<String, MutableList<MutableList<ThrowData>>>()

        playerIds.forEach { pid ->
            val roundsForPlayer = gameWithRounds.rounds
                .filter { it.round.playerId == pid }
                .sortedBy { it.round.roundIndex }
                .map { roundWithThrows ->
                    roundWithThrows.throws
                        .sortedBy { it.throwIndex }
                        .map { dartThrow ->
                            ThrowData(
                                fieldValue = dartThrow.fieldValue,
                                isDouble = dartThrow.isDouble,
                                isTriple = dartThrow.isTriple,
                                isBust = roundWithThrows.round.isBust,
                                throwIndex = dartThrow.throwIndex
                            )
                        }.toMutableList()
                }.toMutableList()
            perPlayerRounds[pid] = roundsForPlayer
        }

        val totalPlayers = game.totalPlayers
        val activePlayerIndex = game.activePlayerIndex.coerceIn(0, (totalPlayers - 1).coerceAtLeast(0))
        val ranking = game.ranking
            ?.split(",")
            ?.filter { finishedPlayerId ->
                val rounds = perPlayerRounds[finishedPlayerId] ?: emptyList()
                val totalPoints = rounds.sumOf { round -> round.sumOf { it.calcScore() ?: 0 } }
                totalPoints >= (game.gameStyle ?: 0)
            } ?: emptyList()

        _state.update {
            it.copy(
                currentGameId = game.id,
                selectedPlayerIds = playerIds,
                gameStyle = game.gameStyle ?: 301,
                perPlayerRounds = perPlayerRounds,
                ranking = ranking,
                activePlayerIndex = activePlayerIndex,
                winnerId = game.winnerId,
                isGameEnded = game.isFinished
            )
        }
    }
}
