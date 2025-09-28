package de.michael.tolleapp.games.romme.presentation

import de.michael.tolleapp.games.util.table.SortDirection

sealed interface RommeAction {
    data object NavigateToMainMenu : RommeAction

    data class OnRoundScoreChange(val playerId: String, val newValue: Int) : RommeAction
    data class OnSortDirectionChange(val newDirection: SortDirection) : RommeAction

    data object FinishRound : RommeAction

    data object OnGameFinished : RommeAction

    data object DeleteGame : RommeAction
}