package de.michael.tolleapp.games.romme.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.michael.tolleapp.games.romme.data.entities.RommeGameEntity
import de.michael.tolleapp.games.romme.data.entities.RommeRoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RommeDao {

    // Game operations --------------------------------------------------------------
    @Upsert
    suspend fun upsertGame(gameEntity: RommeGameEntity)

    @Query("SELECT * FROM RommeGameEntity WHERE id = :gameId")
    fun getGame(gameId: String): Flow<RommeGameEntity?>

    @Query("SELECT * FROM RommeGameEntity WHERE finished = 0 ORDER BY createdAt DESC")
    fun getPausedGames(): Flow<List<RommeGameEntity>>

    @Query("UPDATE RommeGameEntity SET finished = 1 WHERE id = :gameId")
    suspend fun finishGame(gameId: String)

    @Query("DELETE FROM RommeGameEntity WHERE id = :gameId")
    suspend fun deleteGame(gameId: String)

    // Round operations --------------------------------------------------------------
    @Upsert
    suspend fun upsertRound(round: RommeRoundEntity)

    @Query("SELECT * FROM RommeRoundEntity WHERE gameId = :gameId ORDER BY roundNumber ASC")
    fun getRoundsForGame(gameId: String): Flow<List<RommeRoundEntity>>

    // Player operations -------------------------------------------------------------
    @Query("INSERT INTO RommeGamePlayerEntity (gameId, playerId) VALUES (:gameId, :playerId)")
    suspend fun addPlayerToGame(gameId: String, playerId: String)

    @Query("DELETE FROM RommeGamePlayerEntity WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun removePlayerFromGame(gameId: String, playerId: String)

    @Query("SELECT playerId FROM RommeGamePlayerEntity WHERE gameId = :gameId")
    fun getPlayerIdsForGame(gameId: String): Flow<List<String>>
}