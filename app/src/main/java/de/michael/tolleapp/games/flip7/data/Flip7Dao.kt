package de.michael.tolleapp.games.flip7.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import de.michael.tolleapp.games.flip7.data.entities.Flip7GameEntity
import de.michael.tolleapp.games.flip7.data.entities.Flip7PlayerEntity
import de.michael.tolleapp.games.flip7.data.entities.Flip7RoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface Flip7Dao {
    // Game operations ----------------------------------------------------------------------------
    @Upsert
    suspend fun upsertGame(gameEntity: Flip7GameEntity)
    @Query("SELECT * FROM Flip7GameEntity WHERE id = :gameId LIMIT 1")
    suspend fun getGameById(gameId: String): Flip7GameEntity?
    @Query("SELECT * FROM Flip7GameEntity WHERE isFinished = 0 ORDER BY createdAt DESC")
    fun getPausedGames(): Flow<List<Flip7GameEntity>>
    @Query("UPDATE Flip7GameEntity SET isFinished = 1, endedAt = :endedAt WHERE id = :gameId")
    suspend fun finishGame(gameId: String, endedAt: Long = System.currentTimeMillis())

    @Query("UPDATE Flip7GameEntity SET isFinished = 0, endedAt = NULL WHERE id = :gameId")
    suspend fun unfinishGame(gameId: String)
    @Query("DELETE FROM Flip7GameEntity WHERE id = :gameId")
    suspend fun deleteGameById(gameId: String)

    @Query("UPDATE Flip7GameEntity SET dealerId = :dealerId WHERE id = :gameId")
    suspend fun setDealer(gameId: String, dealerId: String)


    // Round operations ---------------------------------------------------------------------------
    @Query("SELECT * FROM Flip7RoundEntity WHERE gameId = :gameId ORDER BY roundNumber ASC")
    suspend fun getRoundsForGame(gameId: String): List<Flip7RoundEntity>
    @Upsert
    suspend fun upsertRound(round: Flip7RoundEntity)
    @Query("SELECT * FROM Flip7RoundEntity WHERE gameId = :gameId ORDER BY roundNumber DESC LIMIT 1")
    suspend fun getLastRound(gameId: String): Flip7RoundEntity?
    @Delete
    suspend fun deleteRound(round: Flip7RoundEntity)
    suspend fun removeLastRound(gameId: String) {
        getLastRound(gameId)?.let { deleteRound(it) }
    }

    // Player operations --------------------------------------------------------------------------
    @Query("SELECT playerId FROM Flip7PlayerEntity WHERE gameId = :gameId ORDER BY `index` ASC")
    suspend fun getPlayerIdsForGame(gameId: String): List<String>
    @Upsert
    suspend fun upsertPlayerInGame(player: Flip7PlayerEntity)
    @Query("UPDATE Flip7GameEntity SET winnerId = :winnerId WHERE id = :gameId")
    suspend fun setPlayerAsWinner(gameId: String, winnerId: String)
    @Query("UPDATE Flip7GameEntity SET winnerId = null WHERE id = :gameId")
    suspend fun clearWinner(gameId: String)
    @Query("DELETE FROM Flip7PlayerEntity WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun removePlayerFromGame(gameId: String, playerId: String)
}

@Dao
interface Flip7StatisticsDao {
    @Query(
        """
        SELECT COUNT(*) FROM Flip7PlayerEntity
        WHERE playerId = :playerId
    """
    )
    suspend fun getTotalGamesPlayed(playerId: String): Int

    @Query("""SELECT COUNT(*) FROM Flip7GameEntity WHERE winnerId = :playerId""")
    suspend fun getGamesWon(playerId: String): Int

    @Query(
        """SELECT COUNT(*) FROM Flip7RoundEntity WHERE gameId IN
        (SELECT DISTINCT gameId FROM Flip7PlayerEntity WHERE playerId = :playerId)"""
    )
    suspend fun getRoundsPlayed(playerId: String): Int

    @Query(
        """
        SELECT * 
        FROM Flip7RoundEntity 
        WHERE gameId IN (
            SELECT DISTINCT gameId 
            FROM Flip7PlayerEntity 
            WHERE playerId = :playerId
        )
    """
    )
    suspend fun getRoundsForPlayer(playerId: String): List<Flip7RoundEntity>

    // Delete all played games that are finished
    @Query("DELETE FROM Flip7GameEntity WHERE isFinished = 1")
    suspend fun deleteAllFinishedGames()
}