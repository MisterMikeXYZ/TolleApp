package de.michael.tolleapp.games.skyjo.presentation

import de.michael.tolleapp.games.util.player.Player
import de.michael.tolleapp.games.util.table.SortDirection

sealed interface SkyjoAction {
    // Navigation
    data object NavigateToMainMenu : SkyjoAction


    // Game lifecycle
    data object ResetGame : SkyjoAction
    data class DeleteGame(val gameId: String) : SkyjoAction
    data object EndGame : SkyjoAction
    data class PlayAgain(val players: List<Player>) : SkyjoAction


    // Dealer management
    data object AdvanceDealer : SkyjoAction


    // Round management
    data object UndoLastRound : SkyjoAction
    data object EndRound : SkyjoAction
    data class InputScore(val playerId: String, val newScore: Int) : SkyjoAction


    // UI state
    data class SetLastKeyboardPage(val page: Int) : SkyjoAction
    data class OnSortDirectionChange(val newDirection: SortDirection) : SkyjoAction
}