package de.michael.tolleapp.presentation.schwimmen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.data.player.Player
import de.michael.tolleapp.data.schwimmen.game.SchwimmenGameRepository
import de.michael.tolleapp.data.schwimmen.stats.SchwimmenStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class SchwimmenViewModel(
    private val statsRepository: SchwimmenStatsRepository,
    private val gameRepository: SchwimmenGameRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SchwimmenState())
    val state: StateFlow<SchwimmenState> = _state.asStateFlow()

    // match Skyjo: expose paused games as a StateFlow
    val pausedGames =
        gameRepository.getAllGames()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Observe players table so we always have up-to-date names
        viewModelScope.launch {
            statsRepository.getPlayers().collect { players ->
                val namesMap = players.associate { it.id to it.name }
                _state.update { it.copy(playerNames = namesMap) }
                // ensure at least two slots available
                _state.update { s ->
                    val list = s.selectedPlayerIds.toMutableList()
                    while (list.size < 2) list.add(null)
                    s.copy(selectedPlayerIds = list)
                }
            }
        }
    }

    // ----------------------------
    // Player management
    // ----------------------------

    // Like Skyjo: create Player in DB, then select it in the row
    fun addPlayer(name: String, rowIndex: Int) = viewModelScope.launch {
        val newPlayer = Player(name = name)
        val added = statsRepository.addPlayer(newPlayer)
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
            while (updated.size <= rowIndex) updated.add(null)
            updated[rowIndex] = playerId
            // auto-add empty slot if last row got a player
            if (rowIndex == updated.lastIndex) updated.add(null)
            state.copy(selectedPlayerIds = updated)
        }
    }

    fun removePlayer(rowIndex: Int) {
        _state.update { state ->
            val updated = state.selectedPlayerIds.toMutableList()
            if (rowIndex < updated.size) updated.removeAt(rowIndex)
            while (updated.size < 2) updated.add(null)
            state.copy(selectedPlayerIds = updated)
        }
    }

    // ----------------------------
    // Game lifecycle
    // ----------------------------
    fun startNewGame() {
        val newGameId = UUID.randomUUID().toString()
        _state.update { s ->
            val players = s.selectedPlayerIds.filterNotNull()
            s.copy(
                currentGameId = newGameId,
                // everyone starts with 3 lives
                playerLives = players.associateWith { 3 }
            )
        }
        viewModelScope.launch { gameRepository.createGame(newGameId) }
    }

    fun pauseCurrentGame() {
        val current = _state.value
        if (current.currentGameId.isNotEmpty()) {
            viewModelScope.launch {
                // TODO: persist snapshot if you add rounds/lives persistence later
            }
        }
    }

    fun resumeGame(gameId: String) {
        viewModelScope.launch {
            val game = gameRepository.getGameById(gameId)
            if (game != null) {
                _state.update { it.copy(currentGameId = gameId) }
            }
        }
    }

    fun deletePausedGame(gameId: String) {
        viewModelScope.launch {
            gameRepository.finishGame(gameId)
            _state.update { it.copy(currentGameId = "", isGameEnded = false) }
        }
    }

    fun resetGame() {
        _state.update {
            it.copy(
                currentGameId = "",
                selectedPlayerIds = listOf(null, null),
                playerLives = emptyMap(),
                isGameEnded = false,
                winnerId = emptyList(),
                loserId = emptyList(),
                dealerIndex = 0
            )
        }
    }

    // ----------------------------
    // Round logic (kept from your version)
    // ----------------------------
    suspend fun endRound(loserId: String) {
        val currentLives = _state.value.playerLives[loserId] ?: 3
        val newLives = statsRepository.loseLife(loserId, currentLives)

        val updatedLives = _state.value.playerLives.toMutableMap()
        updatedLives[loserId] = newLives

        _state.update { it.copy(playerLives = updatedLives) }

        if (statsRepository.checkGameOver(updatedLives)) {
            val result = statsRepository.getRoundResults(updatedLives)
            _state.update {
                it.copy(
                    isGameEnded = true,
                    winnerId = result.winners,
                    loserId = result.losers
                )
            }

            viewModelScope.launch {
                result.winners.forEach { statsRepository.recordGamePlayed(it, won = true, firstOut = false) }
                result.losers.forEach { statsRepository.recordGamePlayed(it, won = false, firstOut = true) }

                val currentGameId = _state.value.currentGameId
                if (currentGameId.isNotEmpty()) gameRepository.finishGame(currentGameId)
            }
        }
    }

    // ----------------------------
    // Dealer rotation
    // ----------------------------
    fun advanceDealer() {
        _state.update { state ->
            val totalPlayers = state.selectedPlayerIds.count { it != null }
            if (totalPlayers == 0) return@update state
            state.copy(dealerIndex = (state.dealerIndex + 1) % totalPlayers)
        }
    }
}
