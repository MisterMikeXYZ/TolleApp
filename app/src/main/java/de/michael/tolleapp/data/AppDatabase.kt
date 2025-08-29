package de.michael.tolleapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import de.michael.tolleapp.data.player.Player
import de.michael.tolleapp.data.player.PlayerDao
import de.michael.tolleapp.data.skyjo.game.GameRound
import de.michael.tolleapp.data.skyjo.game.SkyjoGame
import de.michael.tolleapp.data.skyjo.game.SkyjoGameDao
import de.michael.tolleapp.data.skyjo.game.SkyjoGameRoundDao
import de.michael.tolleapp.data.skyjo.player.RoundResult
import de.michael.tolleapp.data.skyjo.player.RoundResultDao
import de.michael.tolleapp.data.skyjo.player.SkyjoStats
import de.michael.tolleapp.data.skyjo.player.SkyjoStatsDao

@Database(
    entities = [Player::class, SkyjoStats::class, RoundResult::class, SkyjoGame::class, GameRound::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun skyjoStatsDao(): SkyjoStatsDao
    abstract fun roundResultDao(): RoundResultDao
    abstract fun gameDao(): SkyjoGameDao
    abstract fun gameRoundDao(): SkyjoGameRoundDao
}

