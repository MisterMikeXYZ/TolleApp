package de.michael.tolleapp.games.skyjo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.games.skyjo.domain.SkyjoRepository
import de.michael.tolleapp.games.skyjo.domain.SkyjoRoundData
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.games.util.player.PlayerRepository
import de.michael.tolleapp.games.util.presets.GamePresetRepository
import de.michael.tolleapp.games.util.startScreen.StartAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.collections.first
import kotlin.collections.set

class SkyjoViewModel(
    private val gameRepo: SkyjoRepository,
    private val playerRepo: PlayerRepository,
    private val presetRepo: GamePresetRepository
): ViewModel() {
    private val _allPlayers = playerRepo.getAllPlayers()
    private val _presets = presetRepo.getPresets(GameType.SKYJO.toString())
    private val _pausedGames = gameRepo.getPausedGames().getOrThrow()
    private val _selectedPlayerIds = MutableStateFlow<List<String?>>(listOf(null, null))
    private val _state = MutableStateFlow(SkyjoState())

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
            initialValue = SkyjoState()
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
                presetRepo.createPreset(GameType.SKYJO, action.presetName, action.playerIds)
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
                    currentGameId = gameId,
                    rounds = listOf(
                        SkyjoRoundData(
                            roundNumber = 1,
                            dealerId = selectedPlayers.first().id
                        )
                    )
                ) }

                viewModelScope.launch {
                    gameRepo.createGame(gameId = gameId, endPoints = state.value.endPoints)
                    state.value.selectedPlayers.filterNotNull().forEach { player ->
                        gameRepo.addPlayerToGame(gameId = gameId, playerId = player.id)
                    }
                }
            }

            is StartAction.ResumeGame -> {
                viewModelScope.launch {
                    val pausedGame = gameRepo.getGameById(action.gameId)
                        .getOrThrow()
                        .first()
                        ?: throw IllegalStateException("Could not find game #${action.gameId}")
                    _selectedPlayerIds.update { pausedGame.playerIds }
                    _state.update { state -> state.copy(
                        currentGameId = pausedGame.id,
                        endPoints = pausedGame.endPoints,
                        rounds = pausedGame.rounds,
                    ) }
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
            if (updated.size < 8 && updated.last() != null) {
                updated.add(null)
            }
            return@update updated
        }
    }

    private fun selectPlayer(index: Int, playerId: String) {
        _selectedPlayerIds.update { selectedPlayerIds ->
            val newValue = selectedPlayerIds.toMutableList().apply {
                this[index] = playerId
                if (index == this.lastIndex && this.size < 8) this.add(null) // auto add empty row if last row got a player
            }
            return@update newValue
        }
    }


    fun onAction(action: SkyjoAction) {
        when (action) {
            is SkyjoAction.UndoLastRound -> {
                viewModelScope.launch{
                    undoLastRound()
                }
            }

            is SkyjoAction.EndRound -> {
                val currentRound = _state.value.rounds.last()
                val newRound = currentRound.copy(
                    roundNumber = currentRound.roundNumber,
                    dealerId = currentRound.dealerId,
                )
                val nextDealerId = advanceDealer()
                val nextRound = SkyjoRoundData(
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
                        visibleRoundRows =  if(state.visibleRoundRows > 5) state.visibleRoundRows + 1 else 5,
                    )
                }
                viewModelScope.launch {
                    gameRepo.upsertRound(_state.value.currentGameId, newRound)
                }
                checkEndCondition()
            }

            is SkyjoAction.AdvanceDealer -> { advanceDealer() }

            is SkyjoAction.DeleteGame -> {
                viewModelScope.launch {
                    gameRepo.deleteGame(action.gameId)
                }
            }

            is SkyjoAction.EndGame -> {
                viewModelScope.launch {
                    val gameId = _state.value.currentGameId
                    gameRepo.finishGame(gameId)
                }
            }

            is SkyjoAction.ResetGame -> {_state.update { SkyjoState() } }

            is SkyjoAction.SetLastKeyboardPage -> {
                _state.update { it.copy(lastKeyboardPage = action.page) }
            }

            is SkyjoAction.OnSortDirectionChange -> _state.update { state ->
                state.copy(sortDirection = action.newDirection)
            }

            is SkyjoAction.InputScore -> _state.update { state ->
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
        val hasLoser = totals.values.any { it >= 100 }

        if (hasLoser) {
            val sortedEntries = totals.entries.sortedBy { it.value }
            val lowestScore = sortedEntries.first().value
            val winners = sortedEntries.filter { it.value == lowestScore && it.value <= 99 }.map { it.key }
            val highestScore = sortedEntries.last().value
            val losers = sortedEntries.filter { it.value == highestScore && it.value >= 100 }.map { it.key }
            val ranking = totals.entries.sortedBy { it.value }.map { it.key }

            _state.update {
                it.copy(
                    isGameEnded = true,
                    winnerId = winners,
                    loserId = losers,
                    ranking = ranking
                )
            }

            // --- Persist winners and losers in DB ---
            viewModelScope.launch {
                gameRepo.setWinnerAndLoser(_state.value.currentGameId, winners, losers)
            }
        }
    }


    // Dealer logic --------------------------------------------------------------
    private fun advanceDealer(): String {
        val currentDealerId = _state.value.currentDealerId
        val players = _state.value.selectedPlayerIds.filterNotNull()
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
        val gameId = _state.value.currentGameId
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
    private suspend fun undoLastRound(): Boolean {
        val stateValue = _state.value
        val playerIds = stateValue.selectedPlayerIds
        if (stateValue.rounds.first().scores.isEmpty()) return false

        if (stateValue.isGameEnded) gameRepo.unfinishGame(stateValue.currentGameId)

        val updatedRounds = stateValue.rounds.toMutableList()
        val lastRound = updatedRounds.last()
        if (lastRound.scores.isNotEmpty()) {

        }
        updatedRounds.removeAt(updatedRounds.lastIndex)
        val updatedTotals = stateValue.totalPoints.toMutableMap()
        stateValue.rounds.last().scores.forEach { (playerId, points) ->
            val currentTotal = updatedTotals[playerId] ?: 0
            updatedTotals[playerId] = currentTotal - points
        }

        gameRepo.clearWinnersAndLosers(stateValue.currentGameId, playerIds.filterNotNull())
        gameRepo.removeLastRound(stateValue.currentGameId)

        val visibleRoundRows = if (_state.value.visibleRoundRows <= 5) 5 else _state.value.visibleRoundRows - 1

        _state.update {
            it.copy(
                totalPoints = updatedTotals,
                isGameEnded = false,
                winnerId = listOf(null),
                loserId = listOf(null),
                visibleRoundRows = visibleRoundRows,
                rounds = updatedRounds,
                ranking = emptyList(),

            )
        }
        reverseDealer()
        return true
    }
}