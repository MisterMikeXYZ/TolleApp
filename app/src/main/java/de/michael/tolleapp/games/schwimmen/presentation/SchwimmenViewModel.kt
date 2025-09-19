package de.michael.tolleapp.games.schwimmen.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.games.player.Player
import de.michael.tolleapp.games.player.PlayerRepository
import de.michael.tolleapp.games.presets.GamePresetRepository
import de.michael.tolleapp.games.schwimmen.data.game.GameScreenType
import de.michael.tolleapp.games.schwimmen.data.game.SchwimmenGame
import de.michael.tolleapp.games.schwimmen.data.game.SchwimmenGameRepository
import de.michael.tolleapp.games.schwimmen.data.stats.SchwimmenStatsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class SchwimmenViewModel(
    private val statsRepository: SchwimmenStatsRepository,
    private val playerRepo: PlayerRepository,
    private val gameRepository: SchwimmenGameRepository,
    private val presetRepo: GamePresetRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SchwimmenState())
    val state: StateFlow<SchwimmenState> = _state.asStateFlow()
    val presets = presetRepo.getPresets("schwimmen")



    val pausedGames: StateFlow<List<SchwimmenGame>> =
        gameRepository.getPausedGames()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            combine(
                statsRepository.getPlayers(),
                playerRepo.getAllPlayers(),
            ) { stats, players ->
                val namesMap = players.associate { it.id to it.name }
                namesMap
            }.collect { namesMap ->
                _state.update { it.copy(playerNames = namesMap) }
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

    fun resetSelectedPlayers() {
        _state.update { state ->
            state.copy(
                selectedPlayerIds = listOf(null, null),
            )
        }
    }

    fun resumeGame(gameId: String, onResumed: (() -> Unit)? = null) {
        viewModelScope.launch {
            val rounds = gameRepository.loadGame(gameId) // List<SchwimmenGameRound>
            val currentLives = rounds.associate { it.playerId to it.lives }
            val players = if (currentLives.isNotEmpty()) {
                currentLives.keys.toMutableList<String?>()
            } else {
                _state.value.selectedPlayerIds.filterNotNull().toMutableList<String?>()
            }
            if (players.isEmpty() || players.lastOrNull() != null) {
                players.add(null)
            }
            val loserId = state.value.losers.first()
            _state.update {
                it.copy(
                    currentGameId = gameId,
                    selectedPlayerIds = players,
                    perPlayerRounds = currentLives.toMutableMap(),
                    isGameEnded = false,
                    winnerId = null,
                    loserId = loserId,
                    ranking = emptyList(),
                    currentGameRounds = rounds.maxOfOrNull { it.roundIndex } ?: 1
                )
            }

            val updatedPlayers = _state.value.selectedPlayerIds.filterNotNull()
            val dealerIndex = if (updatedPlayers.isNotEmpty()) 0 else 0
            _state.update {
                it.copy(dealerIndex = dealerIndex)
            }

            gameRepository.continueGame(gameId)
            onResumed?.invoke()
        }
    }

    fun endRound(loserId: String) {
        val nextRoundIndex = _state.value.currentGameRounds + 1
        val updatedLives = _state.value.perPlayerRounds.toMutableMap()
        val players = _state.value.selectedPlayerIds.filterNotNull()
        if (players.isEmpty()) return

        players.forEach { playerId ->
            val currentLives = updatedLives[playerId] ?: 4
            val newLives = if (playerId == loserId) (currentLives - 1).coerceAtLeast(0) else currentLives
            updatedLives[playerId] = newLives

            viewModelScope.launch {
                try {
                    gameRepository.addRound(
                        _state.value.currentGameId,
                        playerId,
                        nextRoundIndex,
                        newLives
                    )
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }

        // Update state with new lives
        _state.update { s ->
            val newLosers = s.losers.toMutableList()
            updatedLives.forEach { (playerId, lives) ->
                if (lives == 0 && !newLosers.contains(playerId)) {
                    newLosers.add(playerId)
                }
            }

            s.copy(
                perPlayerRounds = updatedLives,
                currentGameRounds = nextRoundIndex,
                losers = newLosers
            )
        }

        checkEndCondition()
    }

    private fun checkEndCondition() {
        val lives = _state.value.perPlayerRounds
        val playersAlive = lives.count { it.value > 0 }

        if (playersAlive == 1) {
            val winnerId = lives.entries.first { it.value > 0 }.key

            // Build final ranking: winner first, then losers in reverse order
            val finalRanking = mutableListOf<String>()
            finalRanking.add(winnerId)
            finalRanking.addAll(_state.value.losers.reversed().filterNotNull())

            _state.update {
                it.copy(
                    isGameEnded = true,
                    winnerId = winnerId,
                    loserId = _state.value.losers.firstOrNull(),
                    ranking = finalRanking
                )
            }

            // Persist stats
            viewModelScope.launch {
                val currentRounds = _state.value.currentGameRounds

                lives.forEach { (playerId, remainingLives) ->
                    val isWinner = playerId == winnerId
                    val firstOut = playerId == _state.value.loserId
                    statsRepository.finalizePlayerStats(
                        playerId = playerId,
                        lives = remainingLives,
                        isWinner = isWinner,
                        firstOut = firstOut,
                        rounds = currentRounds
                    )
                }
            }
        }
    }


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
            if (updated.contains(playerId)) return
            while (updated.size <= rowIndex) {
                updated.add(null)
            }
            updated[rowIndex] = playerId
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
            while (updated.size < 2) {
                updated.add(null)
            }
            state.copy(selectedPlayerIds = updated)
        }
    }

    fun startGame(screenType: GameScreenType) {
        val newGameId = UUID.randomUUID().toString()
        val players = _state.value.selectedPlayerIds.filterNotNull()
        val initialLives = players.associateWith { 4 } // 4 lives per player

        _state.update { state ->
            state.copy(
                currentGameId = newGameId,
                selectedPlayerIds = players,
                perPlayerRounds = initialLives.toMutableMap(),
                currentGameRounds = 0,
                losers = emptyList(),
                isGameEnded = false,
                winnerId = null,
                loserId = null
            )
        }

        viewModelScope.launch {
            gameRepository.startGame(newGameId, screenType)
        }
    }


    fun resetGame() {
        _state.update { state ->
            state.copy(
                currentGameId = "",
                selectedPlayerIds = listOf(null, null),
                perPlayerRounds = mutableMapOf(),
                isGameEnded = false,
                winnerId = null,
                loserId = null,
                ranking = emptyList(),
                currentGameRounds = 0,
                dealerIndex = 0,
                screenType = GameScreenType.CANVAS,
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

    fun advanceDealer(totalPlayers: Int) {
        _state.update { state ->
            state.copy(
                dealerIndex = (state.dealerIndex + 1) % totalPlayers
            )
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
}