package de.michael.tolleapp.data.schwimmen.game

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.michael.tolleapp.data.schwimmen.stats.SchwimmenGamePlayer
import kotlinx.coroutines.flow.Flow

@Dao
interface SchwimmenGameDao {
    @Insert
    suspend fun insertGame(game: SchwimmenGame)

    @Query("SELECT * FROM schwimmen_games WHERE id = :gameId LIMIT 1")
    suspend fun getGameById(gameId: String): SchwimmenGame?

    @Query("SELECT * FROM schwimmen_games ORDER BY createdAt DESC")
    fun getAllGames(): Flow<List<SchwimmenGame>>

    @Update
    suspend fun updateGame(game: SchwimmenGame)

    @Query("UPDATE schwimmen_games SET endedAt = :endedAt, isFinished = 1 WHERE id = :gameId")
    suspend fun finishGame(gameId: String, endedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM schwimmen_games WHERE id = :gameId")
    suspend fun deleteGame(gameId: String)

}

@Dao
interface SchwimmenGamePlayerDao {
    @Insert
    suspend fun insertPlayers(players: List<SchwimmenGamePlayer>)

    @Query("SELECT * FROM schwimmen_game_players WHERE gameId = :gameId")
    suspend fun getPlayersForGame(gameId: String): List<SchwimmenGamePlayer>

    @Query("SELECT * FROM schwimmen_game_players WHERE gameId = :gameId AND isOut = 0")
    suspend fun getAlivePlayers(gameId: String): List<SchwimmenGamePlayer>

    @Query("SELECT * FROM schwimmen_game_players WHERE gameId = :gameId AND playerId = :playerId LIMIT 1")
    suspend fun getPlayer(gameId: String, playerId: String): SchwimmenGamePlayer?

    @Update
    suspend fun updatePlayer(player: SchwimmenGamePlayer)

    // ⚡️ Shortcut: decrement life
    @Query("""
        UPDATE schwimmen_game_players
        SET livesRemaining = livesRemaining - 1,
            isOut = CASE WHEN livesRemaining - 1 <= 0 THEN 1 ELSE isOut END
        WHERE gameId = :gameId AND playerId = :playerId
    """)
    suspend fun decrementLife(gameId: String, playerId: String)
}
