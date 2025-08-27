package de.michael.tolleapp.data.skyjo.player

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SkyjoPlayer::class, RoundResult::class], version = 2)
abstract class SkyjoPlayerDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun roundResultDao(): RoundResultDao
}
