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
    @Query("INSERT INTO WizardGameEntity (id, roundsToPlay) VALUES (:id, :roundsToPlay)")
    fun createGame(id: String, roundsToPlay: Int)

    @Query("SELECT * FROM WizardGameEntity WHERE id = :gameId")
    fun getGame(gameId: String): Flow<WizardGameEntity?>

    @Query("UPDATE WizardGameEntity SET finished = 1 WHERE id = :gameId")
    fun finishGame(gameId: String)

    @Query("DELETE FROM WizardGameEntity WHERE id = :gameId")
    fun deleteGame(gameId: String)

    // Round operations --------------------------------------------------------------
    @Upsert
    fun upsertRound(round: WizardRoundEntity)

    @Query("SELECT * FROM WizardRoundEntity WHERE gameId = :gameId ORDER BY roundNumber ASC")
    fun getRoundsForGame(gameId: String): Flow<List<WizardRoundEntity>>

    // Player operations -------------------------------------------------------------
    @Query("INSERT INTO WizardGamePlayerEntity (gameId, playerId) VALUES (:gameId, :playerId)")
    fun addPlayerToGame(gameId: String, playerId: String)

    @Query("DELETE FROM WizardGamePlayerEntity WHERE gameId = :gameId AND playerId = :playerId")
    fun removePlayerFromGame(gameId: String, playerId: String)

    @Query("SELECT playerId FROM WizardGamePlayerEntity WHERE gameId = :gameId")
    fun getPlayerIdsForGame(gameId: String): Flow<List<String>>
}