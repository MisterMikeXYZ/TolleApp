package de.michael.tolleapp.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.games.util.player.Player
import de.michael.tolleapp.games.util.player.PlayerRepository
import de.michael.tolleapp.games.schwimmen.data.stats.SchwimmenStats
import de.michael.tolleapp.games.schwimmen.data.stats.SchwimmenStatsRepository
import de.michael.tolleapp.games.skyjo.data.SkyjoGameRepository
import de.michael.tolleapp.games.skyjo.data.SkyjoStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StatViewModel(
    private val skyjoRepo: SkyjoGameRepository,
    private val schwimmenRepo: SchwimmenStatsRepository,
    private val playerRepo: PlayerRepository,
) : ViewModel() {

    private val _allPlayers = playerRepo.getAllPlayers()
    private val _schwimmenStats = schwimmenRepo.getAllStats()
    private val _state = MutableStateFlow(StatState())

    val state = combine(
        _allPlayers,
        _schwimmenStats,
        _state
    ) { allPlayers, schwimmenList, state ->
        val playersSkyjoFull = buildSkyjoStats(allPlayers)

        val playersSchwimmenFull = allPlayers.map { player ->
            schwimmenList.find { it.playerId == player.id } ?: SchwimmenStats(playerId = player.id)
        }

        state.copy(
            playersSkyjo = playersSkyjoFull,
            playersSchwimmen = playersSchwimmenFull,
            playerNames = allPlayers.associate { it.id to it.name }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = StatState()
    )

    private suspend fun buildSkyjoStats(players: List<Player>): List<SkyjoStats> {
        return players.map { player ->
            SkyjoStats(
                playerId = player.id,
                totalGames = skyjoRepo.getTotalGamesPlayed(player.id),
                gamesWon = skyjoRepo.getGamesWon(player.id),
                gamesLost = skyjoRepo.getGamesLost(player.id),
                roundsPlayed = skyjoRepo.getRoundsPlayed(player.id),
                bestRound = skyjoRepo.getBestRoundScore(player.id),
                worstRound = skyjoRepo.getWorstRoundScore(player.id),
                avgRound = skyjoRepo.getAverageRoundScore(player.id),
                bestEnd = skyjoRepo.getBestEndScore(player.id),
                worstEnd = skyjoRepo.getWorstEndScore(player.id),
                totalEnd = skyjoRepo.getTotalEndScore(player.id),
            )
        }
    }

    fun selectGame(game: GameType) {
        _state.value = _state.value.copy(selectedGame = game)
    }

    fun resetCurrentGameStats() {
        viewModelScope.launch {
            when (_state.value.selectedGame) {
                GameType.SKYJO -> {
                    // Reset all Skyjo stats in DB
                    skyjoRepo.resetAllGameStats()

                    // Refresh state to reflect the reset
                    val allPlayers = playerRepo.getAllPlayersOnce()
                    val playersSkyjoFull = allPlayers.map { player ->
                        SkyjoStats(
                            playerId = player.id,
                            totalGames = 0,
                            gamesWon = 0,
                            gamesLost = 0,
                            roundsPlayed = 0,
                            bestRound = null,
                            worstRound = null,
                            avgRound = null,
                            bestEnd = null,
                            worstEnd = null,
                            totalEnd = 0,
                        )
                    }

                    _state.update { state ->
                        state.copy(
                            playersSkyjo = playersSkyjoFull
                        )
                    }
                }
                GameType.SCHWIMMEN -> {
                    schwimmenRepo.resetAllGameStats()
                    // Schwimmen flow is already collected, UI refreshes automatically
                }
                else -> { /* no-op */}
            }
        }
    }

}
