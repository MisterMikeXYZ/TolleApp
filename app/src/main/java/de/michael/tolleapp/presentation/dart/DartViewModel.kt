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
import de.michael.tolleapp.presentation.dart.components.ThrowAction
import de.michael.tolleapp.presentation.dart.components.ThrowData
import de.michael.tolleapp.presentation.dart.components.calcScore

class DartViewModel (
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

    fun insertThrow(playerId: String, value: String, multiplier: String) {
        val fieldValue = value.toInt()
        val isDouble = multiplier == "Double"
        val isTriple = multiplier == "Triple"

        val playerRounds: MutableList<MutableList<ThrowData>> =
            state.value.perPlayerRounds[playerId]
                ?.map { it.toMutableList() }
                ?.toMutableList()
                ?: mutableListOf()

        if (playerRounds.isEmpty() || playerRounds.last().size == 3) {
            playerRounds.add(mutableListOf())
        }

        val throwIndex = playerRounds.last().size
        val throwData = ThrowData(fieldValue, isDouble, isTriple, throwIndex)

        val totalRoundPoints = playerRounds.sumOf { it.sumOf { throwData -> throwData.calcScore() } }
        if (_state.value.gameStyle - (totalRoundPoints + throwData.calcScore()) < 0) {
            bust(playerId)
            return
        }

        playerRounds.last().add(throwData)

        throwStack.add(
            ThrowAction(playerId, throwData, playerRounds.lastIndex)
        )

        _state.value = state.value.copy(
            perPlayerRounds = state.value.perPlayerRounds.toMutableMap().apply {
                this[playerId] = playerRounds
            }
        )

        if (!checkEndConditionForPlayer(playerId) && throwIndex == 2) advancePlayer()
    }

    fun bust(playerId: String) {
        val playerRounds: MutableList<MutableList<ThrowData>> =
            state.value.perPlayerRounds[playerId]
                ?.map { it.toMutableList() }
                ?.toMutableList()
                ?: mutableListOf()
        playerRounds.removeLastOrNull()
        advancePlayer()
    } //TODO: Würfe dürfen nicht gelöscht werden, aber dürfen auch nicht in der Rechnung auftauchen

    fun undoThrow() {
        val lastAction = throwStack.removeLastOrNull() ?: return

        val playerRounds = state.value.perPlayerRounds[lastAction.playerId]
            ?.map { it.toMutableList() }
            ?.toMutableList() ?: return

        // Remove the throw
        playerRounds.getOrNull(lastAction.roundIndex)?.removeLastOrNull()

        // Remove empty round
        if (playerRounds.getOrNull(lastAction.roundIndex)?.isEmpty() == true) {
            playerRounds.removeAt(lastAction.roundIndex)
        }

        // If undoing a throw would put the player "back in the game",
        // we need to remove them from the ranking if they were marked finished.
        val totalRoundPoints = playerRounds.sumOf { round -> round.sumOf { it.calcScore() } }
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
                loserId = null,
                ranking = emptyList(),
                activePlayerIndex = 0
            )
        }
    }

    private fun checkEndConditionForPlayer(playerId: String): Boolean {
        val rounds = _state.value.perPlayerRounds[playerId]
        val totalRoundPoints = rounds!!.sumOf { it.sumOf { throwData -> throwData.calcScore() } }
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

        if (_state.value.selectedPlayerIds.size == ranking.size) endGame()
        return true
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
                loserId = null,
                ranking = emptyList(),
                isGameEnded = false,
                activePlayerIndex = 0
            )
        }
    }

    fun advancePlayer() {
        val totalPlayers = _state.value.selectedPlayerIds.filterNotNull().size
        if (totalPlayers == 0) return
        var newIndex = (_state.value.activePlayerIndex + 1) % totalPlayers
        while (_state.value.ranking.contains(_state.value.selectedPlayerIds.filterNotNull()[newIndex])) {
            newIndex = (newIndex + 1) % totalPlayers
            if (_state.value.ranking.size == totalPlayers) {
                return
            }
        }
        _state.update { it.copy(activePlayerIndex = newIndex) }
    }
}
