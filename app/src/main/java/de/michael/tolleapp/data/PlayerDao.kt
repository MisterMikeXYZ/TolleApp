package de.michael.tolleapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Player::class, RoundResult::class], version = 1)
abstract class SkyjoDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun roundResultDao(): RoundResultDao
}
