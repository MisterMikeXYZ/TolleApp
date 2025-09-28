package de.michael.tolleapp.games.skyjo.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.michael.tolleapp.games.util.PausedGame

@Entity
data class SkyjoGameEntity(
    @PrimaryKey override val id: String,
    override val createdAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val isFinished: Boolean = false,
    val dealerId: String? = null,
    val endPoints: Int? = null,
): PausedGame(id, createdAt)