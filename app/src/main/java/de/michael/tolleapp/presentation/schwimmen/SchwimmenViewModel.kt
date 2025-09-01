package de.michael.tolleapp.presentation.schwimmen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.data.player.Player
import de.michael.tolleapp.data.schwimmen.game.GameScreenType
import de.michael.tolleapp.data.schwimmen.game.SchwimmenGameRepository
import de.michael.tolleapp.data.schwimmen.stats.SchwimmenStatsRepository
import de.michael.tolleapp.data.schwimmen.game.RoundPlayer
import de.michael.tolleapp.data.schwimmen.game.SchwimmenGame
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class SchwimmenViewModel(
    private val statsRepository: SchwimmenStatsRepository,
    private val gameRepository: SchwimmenGameRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SchwimmenState())
    val state: StateFlow<SchwimmenState> = _state.asStateFlow()

//    val pausedGames: StateFlow<List<de.michael.tolleapp.data.schwimmen.game.SchwimmenGame>> =
//        gameRepository.getAllGames()
//            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pausedGames: StateFlow<List<SchwimmenGame>> =
        gameRepository.getAllGames()
            .map { games -> games.filter { !it.isFinished } } // ✅ only include unfinished
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            statsRepository.getPlayers().collect { players ->
                val namesMap = players.associate { it.id to it.name }
                _state.update { it.copy(playerNames = namesMap) }

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
    fun addPlayer(name: String, rowIndex: Int) = viewModelScope.launch {
        val newPlayer = Player(name = name)
        val added = statsRepository.addPlayer(newPlayer)
        if (added) selectPlayer(rowIndex, newPlayer.id)
    }

    fun selectPlayer(rowIndex: Int, playerId: String) {
        _state.update { state ->
            val updated = state.selectedPlayerIds.toMutableList()
            if (updated.contains(playerId)) return
            while (updated.size <= rowIndex) updated.add(null)
            updated[rowIndex] = playerId
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
    fun startNewGame(screenType: GameScreenType) {
        val newGameId = UUID.randomUUID().toString()
        _state.update { s ->
            val players = s.selectedPlayerIds.filterNotNull()
            s.copy(
                currentGameId = newGameId,
                playerLives = players.associateWith { 4 },
                screenType = screenType,
            )
        }

        viewModelScope.launch {
            gameRepository.createGame(newGameId, screenType)
            persistCurrentGameSnapshot(newGameId)
        }
    }

    fun pauseCurrentGame() {
        val gameId = _state.value.currentGameId
        if (gameId.isNotEmpty()) {
            viewModelScope.launch { persistCurrentGameSnapshot(gameId) }
        }
    }

    fun resumeGame(gameId: String) {
        viewModelScope.launch {
            val game = gameRepository.getGameById(gameId)
            if (game != null) {
                _state.update { s ->
                    s.copy(
                        currentGameId = gameId,
                        screenType = game.screenType // ✅ safe now
                    )
                }
            }
            val latestRound = gameRepository.getLatestRound(gameId)
            if (latestRound != null) {
                // Load players for that round
                val roundPlayers: List<RoundPlayer> = gameRepository.getPlayersForRound(latestRound.id)
                val ids = roundPlayers.map { it.playerId }
                val lives = roundPlayers.associate { it.playerId to it.lives }

                _state.update { s ->
                    s.copy(
                        currentGameId = gameId,
                        selectedPlayerIds = ids,
                        playerLives = lives,
                        dealerIndex = latestRound.dealerIndex,
                        isGameEnded = false,
                        winnerId = emptyList(),
                        loserId = emptyList(),
                        screenType = game!!.screenType
                    )
                }
            } else {
                _state.update { it.copy(currentGameId = gameId, screenType = game!!.screenType) }
            }
        }
    }

    fun deleteGame() {
        val gameId = _state.value.currentGameId
        if (gameId.isNotEmpty()) {
            viewModelScope.launch { gameRepository.deleteGameCompletely(gameId) }
        }
        resetGame()
    }

    fun deletePausedGame(gameId: String) {
        viewModelScope.launch {
            gameRepository.deleteGameCompletely(gameId)
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
                dealerIndex = 0,
                //playerNames = emptyMap(),
                currentGameRounds = 0,
                ranking = emptyList(),
                screenType = GameScreenType.CIRCLE,
                winnerLivesLeft = 0,
            )
        }
    }

    // ----------------------------
    // Round logic
    // ----------------------------
    suspend fun endRound(loserId: String) {
        val currentLives = _state.value.playerLives[loserId] ?: 4
        val newLives = (currentLives - 1).coerceAtLeast(0)

        val updatedLives = _state.value.playerLives.toMutableMap()
        updatedLives[loserId] = newLives

        _state.update { it.copy(playerLives = updatedLives) }

        val currentGameId = _state.value.currentGameId
        if (currentGameId.isNotEmpty()) {
            gameRepository.saveGameSnapshot(
                currentGameId,
                _state.value.selectedPlayerIds.filterNotNull(),
                updatedLives,
                _state.value.dealerIndex
            )
        }
        // Check game end instantly
        endGameIfOnePlayerLeft()
    }

    fun endGameIfOnePlayerLeft() {
        val lives = _state.value.playerLives
        val alivePlayers = lives.filter { it.value > 0 }

        if (alivePlayers.size == 1 && _state.value.currentGameId.isNotEmpty()) {
            val winnerId = alivePlayers.keys.first()
            val winnerLives = alivePlayers[winnerId] ?: 0

            val losers = lives.filter { it.value == 0 }.keys.toList()
            val ranking = listOf(winnerId) + losers // winner first

            _state.update { s ->
                s.copy(
                    isGameEnded = true,
                    winnerId = listOf(winnerId),
                    loserId = losers,
                    ranking = ranking,
                    winnerLivesLeft = winnerLives
                )
            }

            // Persist stats and finish game
            viewModelScope.launch {
                _state.value.winnerId.forEach { statsRepository.recordGamePlayed(it!!, won = true, firstOut = false) }
                losers.forEach { statsRepository.recordGamePlayed(it, won = false, firstOut = true) }

                if (_state.value.currentGameId.isNotEmpty()) {
                    gameRepository.finishGame(_state.value.currentGameId)
                }
            }
        }
    }

    private fun computeRoundResult(playerLives: Map<String, Int>): Pair<List<String>, List<String>> {
        val losers = playerLives.filter { it.value == 0 }.keys.toList()
        val alive = playerLives.filter { it.value > 0 }.keys.toList()
        val winners = if (alive.size == 1) alive else emptyList()
        return Pair(winners, losers)
    }

    // ----------------------------
    // Dealer rotation
    // ----------------------------
    fun advanceDealer() {
        _state.update { state ->
            val totalPlayers = state.selectedPlayerIds.count { it != null }
            if (totalPlayers == 0) return@update state
            val newState = state.copy(dealerIndex = (state.dealerIndex + 1) % totalPlayers)
            val currentGameId = newState.currentGameId
            if (currentGameId.isNotEmpty()) {
                viewModelScope.launch {
                    gameRepository.saveGameSnapshot(
                        currentGameId,
                        newState.selectedPlayerIds.filterNotNull(),
                        newState.playerLives,
                        newState.dealerIndex
                    )
                }
            }
            newState
        }
    }

    // ----------------------------
    // Helpers
    // ----------------------------
    private suspend fun persistCurrentGameSnapshot(gameId: String) {
        if (gameId.isEmpty()) return
        val s = _state.value
        gameRepository.saveGameSnapshot(
            gameId,
            s.selectedPlayerIds.filterNotNull(),
            s.playerLives,
            s.dealerIndex
        )
    }
}
