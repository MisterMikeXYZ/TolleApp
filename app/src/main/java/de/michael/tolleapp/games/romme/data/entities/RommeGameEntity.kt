package de.michael.tolleapp.games.romme.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity()
data class RommeGameEntity(
    @PrimaryKey val id: String,
    val createdAt: Long = System.currentTimeMillis(),
    val finished: Boolean = false,
)