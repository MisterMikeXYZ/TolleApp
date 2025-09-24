package de.michael.tolleapp.games.wizard.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WizardGameEntity(
    @PrimaryKey val id: String,
    val createdAt: Long = System.currentTimeMillis(),
    val roundsToPlay: Int,
    val finished: Boolean = false,
)