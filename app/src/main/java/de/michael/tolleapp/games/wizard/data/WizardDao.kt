package de.michael.tolleapp.games.wizard.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.michael.tolleapp.games.wizard.data.entities.WizardGameEntity
import de.michael.tolleapp.games.wizard.data.entities.WizardRoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WizardDao {

    // Game operations --------------------------------------------------------------
    @Upsert
    suspend fun upsertGame(gameEntity: WizardGameEntity)

    @Query("SELECT * FROM WizardGameEntity WHERE id = :gameId")
    fun getGame(gameId: String): Flow<WizardGameEntity?>

    @Query("SELECT * FROM WizardGameEntity WHERE finished = 0 ORDER BY createdAt DESC")
    fun getPausedGames(): Flow<List<WizardGameEntity>>

    @Query("UPDATE WizardGameEntity SET finished = 1 WHERE id = :gameId")
    suspend fun finishGame(gameId: String)

    @Query("DELETE FROM WizardGameEntity WHERE id = :gameId")
    suspend fun deleteGame(gameId: String)

    // Round operations --------------------------------------------------------------
    @Upsert
    suspend fun upsertRound(round: WizardRoundEntity)

    @Query("SELECT * FROM WizardRoundEntity WHERE gameId = :gameId ORDER BY roundNumber ASC")
    fun getRoundsForGame(gameId: String): Flow<List<WizardRoundEntity>>

    // Player operations -------------------------------------------------------------
    @Query("INSERT INTO WizardGamePlayerEntity (gameId, playerId) VALUES (:gameId, :playerId)")
    suspend fun addPlayerToGame(gameId: String, playerId: String)

    @Query("DELETE FROM WizardGamePlayerEntity WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun removePlayerFromGame(gameId: String, playerId: String)

    @Query("SELECT playerId FROM WizardGamePlayerEntity WHERE gameId = :gameId")
    fun getPlayerIdsForGame(gameId: String): Flow<List<String>>
}