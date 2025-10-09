package de.michael.tolleapp.statistics

import DartGameRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.michael.tolleapp.games.flip7.domain.Flip7Repository
import de.michael.tolleapp.games.schwimmen.data.stats.SchwimmenStats
import de.michael.tolleapp.games.schwimmen.data.stats.SchwimmenStatsRepository
import de.michael.tolleapp.games.skyjo.domain.SkyjoRepository
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.games.util.player.Player
import de.michael.tolleapp.games.util.player.PlayerRepository
import de.michael.tolleapp.statistics.gameStats.DartStats
import de.michael.tolleapp.statistics.gameStats.Flip7Stats
import de.michael.tolleapp.statistics.gameStats.SkyjoStats
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StatViewModel(
    private val skyjoRepo: SkyjoRepository,
    private val schwimmenRepo: SchwimmenStatsRepository,
    private val flip7Repo: Flip7Repository,
    private val dartRepo: DartGameRepository,
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
        val playersFlip7 = buildFlip7Stats(allPlayers)
        val playersDart = buildDartStats(allPlayers)

        val playersSchwimmenFull = allPlayers.map { player ->
            schwimmenList.find { it.playerId == player.id } ?: SchwimmenStats(playerId = player.id)
        }

        state.copy(
            playersSkyjo = playersSkyjoFull,
            playersSchwimmen = playersSchwimmenFull,
            playersFlip7 = playersFlip7,
            playersDart = playersDart,
            playerNames = allPlayers.associate { it.id to it.name }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = StatState()
    )

    private suspend fun buildSkyjoStats(players: List<Player>): List<SkyjoStats> {
        return players.map { player ->
            skyjoRepo.getStatsForPlayer(player.id)
        }
    }

    private suspend fun buildFlip7Stats(players: List<Player>): List<Flip7Stats> {
        return players.map { player ->
            flip7Repo.getStatsForPlayer(player.id)
        }
    }

    private suspend fun buildDartStats(players: List<Player>): List<DartStats> {
        return players.map { player ->
            dartRepo.getStatsForPlayer(player.id)
        }
    }

    fun selectGame(game: GameType) {
        _state.value = _state.value.copy(selectedGame = game)
    }

    fun resetCurrentGameStats() {
        viewModelScope.launch {
            val allPlayers = playerRepo.getAllPlayersOnce()
            when (_state.value.selectedGame) {
                GameType.SKYJO -> {
//                    skyjoRepo.resetAllGameStats()
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
                GameType.FLIP7 -> {
                    flip7Repo.resetAllGameStats()

                    val playersFlip7 = allPlayers.map { player ->
                        Flip7Stats(
                            playerId = player.id,
                            totalGames = 0,
                            gamesWon = 0,
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
                            playersFlip7 = playersFlip7
                        )
                    }
                }
                GameType.DART -> {
                    dartRepo.resetAllGameStats()

                    val playersDart = allPlayers.map { player ->
                        DartStats(
                            playerId = player.id,
                        )
                    }

                    _state.update { state ->
                        state.copy(
                            playersDart = playersDart
                        )
                    }
                }
                else -> { /* no-op */}
            }
        }
    }

}
