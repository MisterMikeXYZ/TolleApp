package de.michael.tolleapp.games.flip7.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.michael.tolleapp.games.util.PausedGame

@Entity
data class Flip7GameEntity (
   @PrimaryKey override val id: String,
   override val createdAt: Long = System.currentTimeMillis(),
   val endedAt: Long? = null,
   val isFinished: Boolean = false,
   val dealerId: String? = null,
   val winnerId: String? = null,
): PausedGame(id, createdAt)