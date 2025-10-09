package de.michael.tolleapp.games.dart.data

import androidx.room.*
import de.michael.tolleapp.games.util.player.Player
import de.michael.tolleapp.statistics.gameStats.DartStats
import kotlinx.coroutines.flow.Flow

@Dao
interface DartGameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player)

    // Game handling
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: DartGame)

    @Update
    suspend fun updateGame(game: DartGame)

    @Query("DELETE FROM dart_games WHERE id = :gameId")
    suspend fun deleteGame(gameId: String)

    @Query("DELETE FROM dart_game_rounds WHERE gameId = :gameId")
    suspend fun deleteRoundsForGame(gameId: String)

    @Query("SELECT * FROM dart_games WHERE id = :gameId LIMIT 1")
    suspend fun getGameById(gameId: String): DartGame?

    @Query("SELECT * FROM dart_games ORDER BY createdAt DESC")
    suspend fun getAllGames(): List<DartGame>

    @Query("SELECT * FROM dart_games WHERE isFinished = 0 ORDER BY createdAt DESC")
    fun getPausedGames(): Flow<List<DartGame>>

    @Query("SELECT * FROM dart_games WHERE winnerId = :playerId")
    suspend fun getGamesWonByPlayer(playerId: String): List<DartGame>


    // Round handling
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: DartGameRound) : Long

    @Update
    suspend fun updateRound(round: DartGameRound)

    @Delete
    suspend fun deleteRound(round: DartGameRound)

    @Query("SELECT * FROM dart_game_rounds WHERE gameId = :gameId ORDER BY roundIndex ASC")
    suspend fun getRoundsForGame(gameId: String): List<DartGameRound>

    @Query("SELECT * FROM dart_game_rounds WHERE gameId = :gameId AND playerId = :playerId ORDER BY roundIndex ASC")
    suspend fun getRoundsForPlayerInGame(gameId: String, playerId: String): List<DartGameRound>


    // Statistics queries

    @Query("""
        SELECT COUNT(*) FROM dart_games 
        WHERE id IN (SELECT gameId FROM dart_game_rounds WHERE playerId = :playerId)
    """)
    suspend fun getTotalGamesPlayed(playerId: String): Int

    @Query("SELECT COUNT(*) FROM dart_games WHERE winnerId = :playerId")
    suspend fun getGamesWon(playerId: String): Int

    @Query("""
        SELECT (COUNT(*) - (
            SELECT COUNT(*) FROM dart_games WHERE winnerId = :playerId
        )) FROM dart_games 
        WHERE id IN (SELECT gameId FROM dart_game_rounds WHERE playerId = :playerId)
        """)
    suspend fun getGamesLost(playerId: String): Int

    @Query("SELECT COUNT(*) FROM dart_game_rounds WHERE playerId = :playerId")
    suspend fun getRoundsPlayed(playerId: String): Int

    @Query("""
        WITH pt AS (
            SELECT 
                t.id,
                t.roundId,
                t.throwIndex,
                t.fieldValue,
                t.isDouble,
                t.isTriple,
                CASE
                    WHEN t.fieldValue = 25 THEN CASE WHEN t.isDouble = 1 THEN 50 ELSE 25 END
                    ELSE t.fieldValue * CASE WHEN t.isTriple = 1 THEN 3 WHEN t.isDouble = 1 THEN 2 ELSE 1 END
                END AS score
            FROM dart_throws t
            INNER JOIN dart_game_rounds r ON r.id = t.roundId
            WHERE r.playerId = :playerId
        ),
        rs AS (
            SELECT 
                r.id AS roundId,
                r.gameId,
                r.isBust,
                r.roundIndex,
                IFNULL(SUM(pt.score), 0) AS roundScore,
                COUNT(pt.id) AS dartsInRound
            FROM dart_game_rounds r
            LEFT JOIN pt ON pt.roundId = r.id
            WHERE r.playerId = :playerId
            GROUP BY r.id
        ),
        gp AS (
            SELECT COUNT(DISTINCT r.gameId) AS gamesPlayed
            FROM dart_game_rounds r
            WHERE r.playerId = :playerId
        ),
        gw AS (
            SELECT COUNT(*) AS gamesWon
            FROM dart_games g
            WHERE g.winnerId = :playerId
        ),
        totals AS (
            SELECT 
                (SELECT COUNT(*) FROM dart_game_rounds WHERE playerId = :playerId) AS roundsPlayed,
                (SELECT gamesPlayed FROM gp) AS gamesPlayed,
                (SELECT gamesWon FROM gw) AS gamesWon,
                (SELECT COUNT(*) FROM pt) AS dartsThrown,
                (SELECT IFNULL(SUM(score),0) FROM pt) AS allPoints,
                (SELECT MAX(score) FROM pt) AS highestThrow,
                (SELECT MAX(roundScore) FROM rs) AS highestRound
        ),
        first9 AS (
            SELECT 
                IFNULL(SUM(score),0) AS f9Points,
                COUNT(*) AS f9Darts
            FROM pt
            WHERE EXISTS (
                SELECT 1 FROM dart_game_rounds r2 
                WHERE r2.id = pt.roundId 
                  AND r2.playerId = :playerId 
                  AND r2.roundIndex IN (0,1,2)  -- adjust to (1,2,3) if 1-based
            )
        ),
        dist AS (
            SELECT
                /* count per raw fieldValue */
                SUM(CASE WHEN fieldValue = 0  THEN 1 ELSE 0 END) AS c0,
                SUM(CASE WHEN fieldValue = 1  THEN 1 ELSE 0 END) AS c1,
                SUM(CASE WHEN fieldValue = 2  THEN 1 ELSE 0 END) AS c2,
                SUM(CASE WHEN fieldValue = 3  THEN 1 ELSE 0 END) AS c3,
                SUM(CASE WHEN fieldValue = 4  THEN 1 ELSE 0 END) AS c4,
                SUM(CASE WHEN fieldValue = 5  THEN 1 ELSE 0 END) AS c5,
                SUM(CASE WHEN fieldValue = 6  THEN 1 ELSE 0 END) AS c6,
                SUM(CASE WHEN fieldValue = 7  THEN 1 ELSE 0 END) AS c7,
                SUM(CASE WHEN fieldValue = 8  THEN 1 ELSE 0 END) AS c8,
                SUM(CASE WHEN fieldValue = 9  THEN 1 ELSE 0 END) AS c9,
                SUM(CASE WHEN fieldValue = 10 THEN 1 ELSE 0 END) AS c10,
                SUM(CASE WHEN fieldValue = 11 THEN 1 ELSE 0 END) AS c11,
                SUM(CASE WHEN fieldValue = 12 THEN 1 ELSE 0 END) AS c12,
                SUM(CASE WHEN fieldValue = 13 THEN 1 ELSE 0 END) AS c13,
                SUM(CASE WHEN fieldValue = 14 THEN 1 ELSE 0 END) AS c14,
                SUM(CASE WHEN fieldValue = 15 THEN 1 ELSE 0 END) AS c15,
                SUM(CASE WHEN fieldValue = 16 THEN 1 ELSE 0 END) AS c16,
                SUM(CASE WHEN fieldValue = 17 THEN 1 ELSE 0 END) AS c17,
                SUM(CASE WHEN fieldValue = 18 THEN 1 ELSE 0 END) AS c18,
                SUM(CASE WHEN fieldValue = 19 THEN 1 ELSE 0 END) AS c19,
                SUM(CASE WHEN fieldValue = 20 THEN 1 ELSE 0 END) AS c20,
                SUM(CASE WHEN fieldValue = 25 THEN 1 ELSE 0 END) AS c25
            FROM pt
        ),
        throwKinds AS (
            SELECT
                IFNULL(SUM(CASE WHEN isTriple = 1 THEN 1 ELSE 0 END),0) AS triples,
                IFNULL(SUM(CASE WHEN isDouble = 1 THEN 1 ELSE 0 END),0) AS doubles,
                IFNULL(SUM(CASE WHEN fieldValue = 25 AND isDouble = 0 THEN 1 ELSE 0 END),0) AS bulls,
                IFNULL(SUM(CASE WHEN fieldValue = 25 AND isDouble = 1 THEN 1 ELSE 0 END),0) AS doubleBulls,
                IFNULL(SUM(CASE WHEN isTriple = 1 AND fieldValue = 10 THEN 1 ELSE 0 END),0) AS triple10
            FROM pt
        ),
        dartPos AS (
            SELECT
                AVG(CASE WHEN throwIndex = 0 THEN score END) AS d1avg,
                AVG(CASE WHEN throwIndex = 1 THEN score END) AS d2avg,
                AVG(CASE WHEN throwIndex = 2 THEN score END) AS d3avg
            FROM pt
        )
        SELECT
            :playerId AS playerId,

            /* General */
            totals.roundsPlayed AS roundsPlayed,
            totals.highestThrow AS highestThrow,

            /* Game */
            totals.gamesPlayed AS gamesPlayed,
            totals.gamesWon AS gamesWon,
            CASE WHEN totals.gamesPlayed > 0 
                 THEN totals.gamesWon * 1.0 / totals.gamesPlayed 
                 ELSE 0.0 END AS winRate,

            /* High rounds */
            SUM(CASE WHEN rs.roundScore >=  60 THEN 1 ELSE 0 END) AS over60Rounds,
            SUM(CASE WHEN rs.roundScore >= 100 THEN 1 ELSE 0 END) AS over100Rounds,
            SUM(CASE WHEN rs.roundScore >= 140 THEN 1 ELSE 0 END) AS over140Rounds,
            SUM(CASE WHEN rs.roundScore =  180 AND rs.isBust = 0 THEN 1 ELSE 0 END) AS perfectRounds,

            /* Throws */
            totals.dartsThrown AS dartsThrown,
            CASE WHEN totals.dartsThrown > 0 
                 THEN ROUND(totals.allPoints * 1.0 / (totals.dartsThrown / 3.0)) 
                 ELSE NULL END AS average3Darts,
            CASE WHEN first9.f9Darts > 0 
                 THEN ROUND(first9.f9Points * 1.0 / (first9.f9Darts / 3.0))
                 ELSE NULL END AS first9Average,
            totals.highestRound AS highestRound,
            totals.allPoints AS allPoints,
            ROUND(dartPos.d1avg) AS dart1Average,
            ROUND(dartPos.d2avg) AS dart2Average,
            ROUND(dartPos.d3avg) AS dart3Average,

            /* Throwrates (fractions, not %; multiply by 100 in UI if needed) */
            CASE WHEN totals.dartsThrown > 0 THEN throwKinds.triples     * 1.0 / totals.dartsThrown ELSE NULL END AS tripleRate,
            CASE WHEN totals.dartsThrown > 0 THEN throwKinds.doubles     * 1.0 / totals.dartsThrown ELSE NULL END AS doubleRate,
            CASE WHEN totals.dartsThrown > 0 THEN throwKinds.triple10    * 1.0 / totals.dartsThrown ELSE NULL END AS triple10Rate,
            CASE WHEN totals.dartsThrown > 0 THEN throwKinds.bulls       * 1.0 / totals.dartsThrown ELSE NULL END AS bullsRate,
            CASE WHEN totals.dartsThrown > 0 THEN throwKinds.doubleBulls * 1.0 / totals.dartsThrown ELSE NULL END AS doubleBullsRate,

            CASE WHEN totals.dartsThrown > 0 THEN dist.c0  * 1.0 / totals.dartsThrown ELSE NULL END AS miss,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c1  * 1.0 / totals.dartsThrown ELSE NULL END AS one,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c2  * 1.0 / totals.dartsThrown ELSE NULL END AS two,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c3  * 1.0 / totals.dartsThrown ELSE NULL END AS three,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c4  * 1.0 / totals.dartsThrown ELSE NULL END AS four,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c5  * 1.0 / totals.dartsThrown ELSE NULL END AS five,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c6  * 1.0 / totals.dartsThrown ELSE NULL END AS six,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c7  * 1.0 / totals.dartsThrown ELSE NULL END AS seven,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c8  * 1.0 / totals.dartsThrown ELSE NULL END AS eight,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c9  * 1.0 / totals.dartsThrown ELSE NULL END AS nine,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c10 * 1.0 / totals.dartsThrown ELSE NULL END AS ten,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c11 * 1.0 / totals.dartsThrown ELSE NULL END AS eleven,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c12 * 1.0 / totals.dartsThrown ELSE NULL END AS twelve,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c13 * 1.0 / totals.dartsThrown ELSE NULL END AS thirteen,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c14 * 1.0 / totals.dartsThrown ELSE NULL END AS fourteen,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c15 * 1.0 / totals.dartsThrown ELSE NULL END AS fifteen,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c16 * 1.0 / totals.dartsThrown ELSE NULL END AS sixteen,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c17 * 1.0 / totals.dartsThrown ELSE NULL END AS seventeen,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c18 * 1.0 / totals.dartsThrown ELSE NULL END AS eighteen,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c19 * 1.0 / totals.dartsThrown ELSE NULL END AS nineteen,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c20 * 1.0 / totals.dartsThrown ELSE NULL END AS twenty,
            CASE WHEN totals.dartsThrown > 0 THEN dist.c25 * 1.0 / totals.dartsThrown ELSE NULL END AS bull,

            /* Checkout – needs more schema info (remaining score / finish flag) */
            NULL AS highestCheckout,
            NULL AS minDarts
        FROM rs, totals, first9, dist, throwKinds, dartPos
    """)
    suspend fun getStatsForPlayer(playerId: String): DartStats

    @Query("DELETE FROM dart_games WHERE isFinished = 1")
    suspend fun deleteAllFinishedGames()
}