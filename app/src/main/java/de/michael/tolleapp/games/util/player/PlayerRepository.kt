package de.michael.tolleapp.games.util.player

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PlayerRepository(
    private val playerDao: PlayerDao
) {
    fun getAllPlayers(): Flow<List<Player>> = playerDao.getAllPlayers()

    suspend fun getAllPlayersOnce(): List<Player> {
        return getAllPlayers().first()
    }

    suspend fun getPlayerById(id: String): Player? = playerDao.getPlayerById(id)

    suspend fun getPlayerByName(name: String): Player? = playerDao.getPlayerByName(name)

    suspend fun addPlayer(name: String): Player {
        val existing = playerDao.getPlayerByName(name)
        if (existing != null) {
            return existing
        }
        val newPlayer = Player(name = name)
        playerDao.insertPlayer(newPlayer)
        return newPlayer
    }

    suspend fun deletePlayer(player: Player) {
        playerDao.deletePlayerAndPresets(player)
    }
}
