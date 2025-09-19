package de.michael.tolleapp.games.schwimmen.data.game

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SchwimmenGameDao {

    @Insert
    suspend fun insertGame(game: SchwimmenGame)

    @Update
    suspend fun updateGame(game: SchwimmenGame)

    @Query("UPDATE schwimmen_games SET endedAt = :endedAt WHERE id = :gameId")
    suspend fun markEnded(gameId: String, endedAt: Long)

    @Query("DELETE FROM schwimmen_games WHERE id = :gameId")
    suspend fun deleteGame(gameId: String)

    @Query("SELECT * FROM schwimmen_games WHERE id = :id LIMIT 1")
    suspend fun getGame(id: String): SchwimmenGame?

    @Query("SELECT * FROM schwimmen_games WHERE isFinished = 0 ORDER BY createdAt DESC")
    fun getPausedGames(): Flow<List<SchwimmenGame>>

//    @Query("SELECT * FROM schwimmen_games WHERE id = :gameId LIMIT 1")
//    suspend fun getGameById(gameId: String): SchwimmenGame?
//
//    @Query("SELECT * FROM schwimmen_games ORDER BY createdAt DESC")
//    fun getAllGames(): Flow<List<SchwimmenGame>>
//
//    @Query("UPDATE schwimmen_games SET endedAt = :endedAt, isFinished = 1 WHERE id = :gameId")
//    suspend fun finishGame(gameId: String, endedAt: Long = System.currentTimeMillis())
//
//    @Insert
//    suspend fun insertRound(round: SchwimmenGameRound): Long
//
//    @Query("SELECT * FROM schwimmen_game_rounds WHERE gameId = :gameId ORDER BY id DESC LIMIT 1")
//    suspend fun getLatestRound(gameId: String): SchwimmenGameRound?
//
//    @Query("SELECT * FROM schwimmen_game_rounds WHERE gameId = :gameId ORDER BY id ASC")
//    suspend fun getAllRoundsForGame(gameId: String): List<SchwimmenGameRound>

}

@Dao
interface SchwimmenGameRoundDao {
    @Insert
    suspend fun insertRound(round: SchwimmenGameRound)

    @Query("SELECT * FROM schwimmen_game_rounds WHERE gameId = :gameId ORDER BY roundIndex ASC, id ASC")
    suspend fun getRoundsForGame(gameId: String): List<SchwimmenGameRound>

    @Query("DELETE FROM schwimmen_game_rounds WHERE gameId = :gameId")
    suspend fun deleteRoundsForGame(gameId: String)
}

//@Dao
//interface SchwimmenGamePlayerDao {
//    @Insert
//    suspend fun insertPlayers(players: List<SchwimmenGamePlayer>)
//
//    @Query("SELECT * FROM schwimmen_game_players WHERE gameId = :gameId")
//    suspend fun getPlayersForGame(gameId: String): List<SchwimmenGamePlayer>
//
//    @Query("SELECT * FROM schwimmen_game_players WHERE gameId = :gameId AND isOut = 0")
//    suspend fun getAlivePlayers(gameId: String): List<SchwimmenGamePlayer>
//
//    @Query("SELECT * FROM schwimmen_game_players WHERE gameId = :gameId AND playerId = :playerId LIMIT 1")
//    suspend fun getPlayer(gameId: String, playerId: String): SchwimmenGamePlayer?
//
//    @Update
//    suspend fun updatePlayer(player: SchwimmenGamePlayer)
//}

