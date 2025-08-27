package de.michael.tolleapp.data.skyjo.game

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SkyjoGame::class, GameRound::class],
    version = 2,
    exportSchema = true
)
abstract class SkyjoGameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun gameRoundDao(): GameRoundDao
}
