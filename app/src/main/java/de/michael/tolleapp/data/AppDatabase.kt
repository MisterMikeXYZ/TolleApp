package de.michael.tolleapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import de.michael.tolleapp.data.player.Player
import de.michael.tolleapp.data.player.PlayerDao
import de.michael.tolleapp.data.schwimmen.game.RoundPlayer
import de.michael.tolleapp.data.schwimmen.game.RoundPlayerDao
import de.michael.tolleapp.data.schwimmen.game.GameRound as SchwimmenGameRound
import de.michael.tolleapp.data.schwimmen.game.SchwimmenGame
import de.michael.tolleapp.data.schwimmen.game.SchwimmenGameDao
import de.michael.tolleapp.data.schwimmen.game.SchwimmenGamePlayerDao
import de.michael.tolleapp.data.schwimmen.stats.SchwimmenGamePlayer
import de.michael.tolleapp.data.schwimmen.stats.SchwimmenStats
import de.michael.tolleapp.data.schwimmen.stats.SchwimmenStatsDao
import de.michael.tolleapp.data.skyjo.game.GameRound
import de.michael.tolleapp.data.skyjo.game.SkyjoGame
import de.michael.tolleapp.data.skyjo.game.SkyjoGameDao
import de.michael.tolleapp.data.skyjo.game.SkyjoGameRoundDao
import de.michael.tolleapp.data.skyjo.stats.SkyjoRoundResult
import de.michael.tolleapp.data.skyjo.stats.SkyjoRoundResultDao
import de.michael.tolleapp.data.skyjo.stats.SkyjoStats
import de.michael.tolleapp.data.skyjo.stats.SkyjoStatsDao

@Database(
    entities = [
        Player::class,
        SkyjoStats::class,
        SkyjoRoundResult::class,
        SkyjoGame::class,
        GameRound::class,
        SchwimmenStats::class,
        SchwimmenGame::class,
        SchwimmenGameRound::class,
        SchwimmenGamePlayer::class,
        RoundPlayer::class,
    ],
    version = 7
)
abstract class AppDatabase : RoomDatabase() {

    // Player
    abstract fun playerDao(): PlayerDao

    // Skyjo
    abstract fun skyjoStatsDao(): SkyjoStatsDao
    abstract fun skyjoRoundResultDao(): SkyjoRoundResultDao
    abstract fun skyjoGameDao(): SkyjoGameDao
    abstract fun skyjoGameRoundDao(): SkyjoGameRoundDao

    // Schwimmen
    abstract fun schwimmenStatsDao(): SchwimmenStatsDao
    abstract fun schwimmenGameDao(): SchwimmenGameDao
    abstract fun schwimmenGamePlayerDao(): SchwimmenGamePlayerDao
    abstract fun roundPlayerDao(): RoundPlayerDao
}
