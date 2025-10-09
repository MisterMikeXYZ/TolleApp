package de.michael.tolleapp.games.skyjo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import de.michael.tolleapp.games.skyjo.data.entities.SkyjoGameEntity
import de.michael.tolleapp.games.skyjo.data.entities.SkyjoPlayerEntity
import de.michael.tolleapp.games.skyjo.data.entities.SkyjoRoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkyjoDao {

    // Game operations ----------------------------------------------------------------------------
    @Upsert
    suspend fun upsertGame(gameEntity: SkyjoGameEntity)
    @Query("SELECT * FROM SkyjoGameEntity WHERE id = :gameId LIMIT 1")
    suspend fun getGameById(gameId: String): SkyjoGameEntity?
    @Query("SELECT * FROM SkyjoGameEntity WHERE isFinished = 0 ORDER BY createdAt DESC")
    fun getPausedGames(): Flow<List<SkyjoGameEntity>>
    @Query("UPDATE SkyjoGameEntity SET isFinished = 1, endedAt = :endedAt WHERE id = :gameId")
    suspend fun finishGame(gameId: String, endedAt: Long = System.currentTimeMillis())

    @Query("UPDATE SkyjoGameEntity SET isFinished = 0, endedAt = NULL WHERE id = :gameId")
    suspend fun unfinishGame(gameId: String)
    @Query("DELETE FROM SkyjoGameEntity WHERE id = :gameId")
    suspend fun deleteGameById(gameId: String)

    @Query("UPDATE SkyjoGameEntity SET dealerId = :dealerId WHERE id = :gameId")
    suspend fun setDealer(gameId: String, dealerId: String)


    // Round operations ---------------------------------------------------------------------------
    @Query("SELECT * FROM SkyjoRoundEntity WHERE gameId = :gameId ORDER BY roundNumber ASC")
    suspend fun getRoundsForGame(gameId: String): List<SkyjoRoundEntity>
    @Upsert
    suspend fun upsertRound(round: SkyjoRoundEntity)
    @Query("SELECT * FROM SkyjoRoundEntity WHERE gameId = :gameId ORDER BY roundNumber DESC LIMIT 1")
    suspend fun getLastRound(gameId: String): SkyjoRoundEntity?
    @Delete
    suspend fun deleteRound(round: SkyjoRoundEntity)
    suspend fun removeLastRound(gameId: String) {
        getLastRound(gameId)?.let { deleteRound(it) }
    }

    // Player operations --------------------------------------------------------------------------
    @Query("SELECT playerId FROM SkyjoPlayerEntity WHERE gameId = :gameId ORDER BY `index` ASC")
    suspend fun getPlayerIdsForGame(gameId: String): List<String>
    @Upsert
    suspend fun upsertPlayerInGame(player: SkyjoPlayerEntity)
    @Query("UPDATE SkyjoPlayerEntity SET isWinner = 1 WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun setPlayerAsWinner(gameId: String, playerId: String)
    @Query("UPDATE SkyjoPlayerEntity SET isLoser = 1 WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun setPlayerAsLoser(gameId: String, playerId: String)
    @Query("UPDATE SkyjoPlayerEntity SET isWinner = 0, isLoser = 0 WHERE gameId = :gameId AND playerId IN (:playerIds)")
    suspend fun clearWinnersAndLosers(gameId: String, playerIds: List<String>)
    @Query("DELETE FROM SkyjoPlayerEntity WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun removePlayerFromGame(gameId: String, playerId: String)
}



@Dao
interface SkyjoGameStatisticsDao {
    @Query(
        """
        SELECT COUNT(*) FROM SkyjoPlayerEntity
        WHERE playerId = :playerId
    """
    )
    suspend fun getTotalGamesPlayed(playerId: String): Int

    @Query("""SELECT COUNT(*) FROM SkyjoPlayerEntity WHERE playerId = :playerId AND isWinner = 1""")
    suspend fun getGamesWon(playerId: String): Int

    @Query("""SELECT COUNT(*) FROM SkyjoPlayerEntity  WHERE playerId = :playerId AND isLoser = 1""")
    suspend fun getGamesLost(playerId: String): Int

    @Query(
        """SELECT COUNT(*) FROM SkyjoRoundEntity WHERE gameId IN
        (SELECT DISTINCT gameId FROM SkyjoPlayerEntity WHERE playerId = :playerId)"""
    )
    suspend fun getRoundsPlayed(playerId: String): Int

    @Query(
        """
        SELECT * 
        FROM SkyjoRoundEntity 
        WHERE gameId IN (
            SELECT DISTINCT gameId 
            FROM SkyjoPlayerEntity 
            WHERE playerId = :playerId
        )
    """
    )
    suspend fun getRoundsForPlayer(playerId: String): List<SkyjoRoundEntity>
}
