package de.michael.tolleapp.games.flip7.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.games.flip7.domain.Flip7Repository
import de.michael.tolleapp.games.flip7.domain.Flip7RoundData
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.games.util.player.PlayerRepository
import de.michael.tolleapp.games.util.presets.GamePresetRepository
import de.michael.tolleapp.games.util.startScreen.StartAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class Flip7ViewModel(
    private val gameRepo: Flip7Repository,
    private val playerRepo: PlayerRepository,
    private val presetRepo: GamePresetRepository
): ViewModel() {
    private val _allPlayers = playerRepo.getAllPlayers()
    private val _presets = presetRepo.getPresets(GameType.FLIP7.toString())
    private val _pausedGames = gameRepo.getPausedGames()
    private val _selectedPlayerIds = MutableStateFlow<List<String?>>(listOf(null, null, null))
    private val _state = MutableStateFlow(Flip7State())
    
    val state = combine(
        _allPlayers,
        _presets,
        _selectedPlayerIds,
        _pausedGames,
        _state
    ) { players, presets, selectedPlayerIds, pausedGames, state ->
        state.copy(
            allPlayers = players,
            presets = presets,
            pausedGames = pausedGames,
            selectedPlayers = selectedPlayerIds.map { selectedPlayerId ->
                players.find { it.id == selectedPlayerId }
            },
            selectedPlayerIds = selectedPlayerIds,

            )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5_000),
        initialValue = Flip7State()
    )

    fun onStartAction(action: StartAction) {
        when (action) {
            is StartAction.CreatePlayer -> viewModelScope.launch {
                val player = playerRepo.addPlayer(action.name)
                selectPlayer(action.index, player.id)
            }

            is StartAction.SelectPlayer -> selectPlayer(action.index, action.playerId)

            is StartAction.UnselectPlayer -> unselectPlayer(action.index)

            is StartAction.ResetSelectedPlayers -> _selectedPlayerIds.update { listOf(null, null) }

            is StartAction.CreatePreset -> viewModelScope.launch {
                presetRepo.createPreset(GameType.FLIP7, action.presetName, action.playerIds)
            }

            is StartAction.SelectPreset -> viewModelScope.launch {
                val players = state.value.presets.first { it.preset.id == action.presetId }.players
                _selectedPlayerIds.update { listOf(null, null) }
                players.map { it.playerId }.forEachIndexed { index, playerId ->
                    selectPlayer(index, playerId)
                }
            }

            is StartAction.DeletePreset -> viewModelScope.launch {
                presetRepo.deletePreset(action.presetId)
            }

            is StartAction.StartGame -> {
                val selectedPlayers = state.value.selectedPlayers.filterNotNull()
                if (selectedPlayers.isEmpty()) {
                    return
                }

                val gameId = UUID.randomUUID().toString()
                _selectedPlayerIds.update { it.filterNotNull() }

                _state.update { state -> state.copy(
                    gameId = gameId,
                    currentDealerId = selectedPlayers.first().id,
                    rounds = listOf(
                        Flip7RoundData(
                            roundNumber = 1,
                            dealerId = selectedPlayers.first().id
                        )
                    )
                ) }

                viewModelScope.launch {
                    gameRepo.createGame(gameId = gameId)
                    selectedPlayers.forEachIndexed { index, player ->
                        gameRepo.addPlayerToGame(gameId = gameId, playerId = player.id, index = index)
                    }
                }
            }

            is StartAction.ResumeGame -> {
                viewModelScope.launch {
                    val pausedGame = gameRepo.getGameById(action.gameId)
                        .getOrThrow()
                        ?: throw IllegalStateException("Could not find game #${action.gameId}")
                    _selectedPlayerIds.update { pausedGame.playerIds }
                    _state.update { state ->
                        state.copy(
                            gameId = pausedGame.gameId,
                            rounds = pausedGame.rounds,
                            currentDealerId = pausedGame.dealerId,
                            totalPoints = pausedGame.rounds
                                .flatMap { it.scores.entries }
                                .groupBy({ it.key }, { it.value })
                                .mapValues { (_, scores) -> scores.sum() }
                        )
                    }
                }
            }

            is StartAction.DeleteGame -> {
                viewModelScope.launch {
                    gameRepo.deleteGame(action.gameId)
                }
            }

            else -> throw NotImplementedError("Action '$action' not implemented in WizardViewModel")
        }
    }

    private fun unselectPlayer(index: Int) {
        _selectedPlayerIds.update { selectedPlayerIds ->
            val updated = selectedPlayerIds.toMutableList()
            if (index < updated.size) {
                updated.removeAt(index)
            }
            if (updated.size < 18 && updated.last() != null) {
                updated.add(null)
            }
            return@update updated
        }
    }

    private fun selectPlayer(index: Int, playerId: String) {
        _selectedPlayerIds.update { selectedPlayerIds ->
            val newValue = selectedPlayerIds.toMutableList().apply {
                this[index] = playerId
                if (index == this.lastIndex && this.size < 18) this.add(null) // auto add empty row if last row got a player
            }
            return@update newValue
        }
    }

    fun onAction(action: Flip7Action) {
        when (action) {
            is Flip7Action.UndoLastRound -> {
                viewModelScope.launch{
                    undoLastRound()
                }
            }

            is Flip7Action.EndRound -> {
                val currentRound = _state.value.rounds.last()
                val newRound = currentRound.copy(
                    roundNumber = currentRound.roundNumber,
                    dealerId = currentRound.dealerId,
                )
                val nextDealerId = advanceDealer()
                val nextRound = Flip7RoundData(
                    roundNumber = currentRound.roundNumber + 1,
                    dealerId = nextDealerId,
                    scores = emptyMap()
                )
                _state.update { state ->
                    state.copy(
                        rounds = state.rounds.dropLast(1) + newRound + nextRound,
                        totalPoints = state.totalPoints.toMutableMap().apply {
                            newRound.scores.forEach { (playerId, points) ->
                                val currentTotal = this[playerId] ?: 0
                                this[playerId] = currentTotal + points
                            }
                        },
                        visibleRoundRows =  if(state.rounds.size <= 5) 5 else state.visibleRoundRows + 1,
                    )
                }
                viewModelScope.launch {
                    gameRepo.upsertRound(_state.value.gameId, newRound)
                    gameRepo.upsertRound(_state.value.gameId, nextRound)
                }
                checkEndCondition()
            }

            is Flip7Action.AdvanceDealer -> { advanceDealer() }

            is Flip7Action.DeleteGame -> {
                viewModelScope.launch {
                    gameRepo.deleteGame(action.gameId)
                }
            }

            is Flip7Action.EndGame -> {
                println("Test")
                viewModelScope.launch {
                    val gameId = _state.value.gameId
                    gameRepo.finishGame(gameId)
                }
            }

            is Flip7Action.ResetGame -> {_state.update { Flip7State() } }

            is Flip7Action.SetLastKeyboardPage -> {
                _state.update { it.copy(lastKeyboardPage = action.page) }
            }

            is Flip7Action.OnSortDirectionChange -> _state.update { state ->
                state.copy(sortDirection = action.newDirection)
            }

            is Flip7Action.InputScore -> _state.update { state ->
                val currentRound = state.rounds.last()
                val newRound = currentRound.copy(
                    scores = currentRound.scores.toMutableMap().apply {
                        this[action.playerId] = action.newScore
                    }
                )
                state.copy(rounds = state.rounds.dropLast(1) + newRound)
            }

            else -> throw NotImplementedError("Action '$action' not implemented in WizardViewModel")
        }
    }

    private fun checkEndCondition() {
        val totals = _state.value.totalPoints
        val winnerCount = totals.values.count { it >= 200 }
        val winners = totals.values.count { it >= 200 } // TODO get the players with over 200 points

        if (winners == 1) {
            val sortedEntries = totals.entries.sortedBy { it.value }
            val lowestScore = sortedEntries.first().value
            val winners = sortedEntries.filter { it.value == lowestScore && it.value <= 199 }.map { it.key }
            val highestScore = sortedEntries.last().value
            val rounds = _state.value.rounds.dropLast(1)

            _state.update {
                it.copy(
                    isFinished = true,
                    rounds = rounds,
                )
            }

            // --- Persist winner ---
            viewModelScope.launch {
                gameRepo.setWinner(_state.value.gameId, winners)
            }
        }
        // If there are multiple winners, check if they have the same points. Only the one with the most points wins and if there are multiple with the same points, all players play another round

    }

    // Dealer logic --------------------------------------------------------------
    private fun advanceDealer(): String {
        val currentDealerId = state.value.currentDealerId
        val players = state.value.selectedPlayerIds.filterNotNull()
        var nextDealerId: String = currentDealerId ?: ""
        if (players.isNotEmpty()) {
            val currentIndex = players.indexOf(currentDealerId)
            val nextIndex =
                if (currentIndex == -1 || currentIndex + 1 >= players.size) 0 else currentIndex + 1
            nextDealerId = players[nextIndex]
            setDealer(nextDealerId)
        }
        return nextDealerId
    }

    private fun setDealer(dealerId: String) {
        val gameId = _state.value.gameId
        if (gameId.isEmpty()) return
        viewModelScope.launch {
            gameRepo.setDealer(gameId, dealerId)
            _state.update { it.copy(currentDealerId = dealerId) }
        }
    }

    private fun reverseDealer() {
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


    // Round logic ----------------------------------------------------------------
    // TODO make it work
    private suspend fun undoLastRound() {
        val s = _state.value
        val rounds = s.rounds

        // No rounds at all? Nothing to undo.
        if (rounds.isEmpty()) return

        // Our convention after EndRound():
        //   [..., committedRound(n), emptyRound(n+1)]
        // i.e., the last item is in-progress (empty scores), the one before it is the last committed.
        val hasInProgressTail = rounds.last().scores.isEmpty()
        val lastCommittedIndex = if (hasInProgressTail) rounds.lastIndex - 1 else rounds.lastIndex
        if (lastCommittedIndex < 0) return

        val lastCommitted = rounds[lastCommittedIndex]
        val lastCommittedScores = lastCommitted.scores

        // If the "last committed" round is actually empty, there's nothing to undo.
        if (lastCommittedScores.isEmpty()) return

        // 1) Persist side: if game was finished, unfinish it first
        if (s.isFinished) {
            gameRepo.unfinishGame(s.gameId)
        }

        // We persisted two rounds in EndRound(): the committed (n) and the empty (n+1).
        // Remove the empty (if present) AND the committed.
        if (hasInProgressTail) {
            gameRepo.removeLastRound(s.gameId) // removes empty (n+1)
        }
        gameRepo.removeLastRound(s.gameId)     // removes committed (n)

        // Also clear winners/losers since we're rolling back.
        gameRepo.clearLosers(s.gameId, s.selectedPlayerIds.filterNotNull())

        // 2) In-memory totals: subtract the last committed scores
        val newTotals = s.totalPoints.toMutableMap().apply {
            lastCommittedScores.forEach { (playerId, points) ->
                this[playerId] = (this[playerId] ?: 0) - points
            }
        }

        // 3) In-memory rounds:
        // We want to end up with an in-progress round that matches the undone round number & dealer.
        val newCurrentRound = lastCommitted.copy(scores = emptyMap())
        val newRounds = buildList {
            // keep everything before the last committed
            addAll(rounds.take(lastCommittedIndex))
            // and attach a fresh, empty round with the same number/dealer as the undone one
            add(newCurrentRound)
        }

        // 4) Visible rows: reduce but not below 5
        val newVisibleRows =
            if (s.visibleRoundRows <= 5) 5 else s.visibleRoundRows - 1

        // 5) Update state
        _state.update {
            it.copy(
                rounds = newRounds,
                totalPoints = newTotals,
                isFinished = false,
                visibleRoundRows = newVisibleRows
            )
        }

        // 6) Dealer: we advanced dealer in EndRound(), so reverse here
        reverseDealer()
    }
}