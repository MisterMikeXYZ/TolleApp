package de.michael.tolleapp.games.flip7.presentation

sealed interface Flip7Action {
    // Navigation
    data object NavigateToMainMenu : Flip7Action

    // Game lifecycle
    data object ResetGame : Flip7Action
    data class DeleteGame(val gameId: String) : Flip7Action
    data object EndGame : Flip7Action

    // Dealer management
    data object AdvanceDealer : Flip7Action

    // Round management
    data object UndoLastRound : Flip7Action
    data object EndRound : Flip7Action
    data class InputScore(val playerId: String, val newScore: Int) : Flip7Action

    // UI state
    data class SetLastKeyboardPage(val page: Int) : Flip7Action
    data class OnSortDirectionChange(val newDirection: de.michael.tolleapp.games.util.table.SortDirection) : Flip7Action
}