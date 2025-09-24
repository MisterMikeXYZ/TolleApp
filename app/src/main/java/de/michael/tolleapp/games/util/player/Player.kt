package de.michael.tolleapp.games.util.player

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "players")
data class Player(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
)
