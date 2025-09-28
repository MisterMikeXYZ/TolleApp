package de.michael.tolleapp.games.skyjo.presentation

import de.michael.tolleapp.games.util.table.SortDirection

sealed interface SkyjoAction {
    // Navigation
    data object NavigateToMainMenu : SkyjoAction


    // Saved games
    data object DeleteAllSavedGames : SkyjoAction
    data object PauseCurrentGame : SkyjoAction
    data class ResumeGame(val gameId: String, val onResumed: (() -> Unit)? = null) : SkyjoAction


    // Game lifecycle
    data object ResetGame : SkyjoAction
    data class DeleteGame(val gameId: String) : SkyjoAction
    data object EndGame : SkyjoAction


    // Dealer management
    data object AdvanceDealer : SkyjoAction


    // Round management
    data class UndoLastRound(val onResult: (Boolean) -> Unit) : SkyjoAction
    data class EndRound(val points: Map<String, Int>) : SkyjoAction


    // UI state
    data class SetLastKeyboardPage(val page: Int) : SkyjoAction
    data class OnSortDirectionChange(val newDirection: SortDirection) : SkyjoAction
}