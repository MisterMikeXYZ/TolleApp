package de.michael.tolleapp.games.wizard.presentation

import de.michael.tolleapp.games.util.table.SortDirection

sealed interface WizardAction {
    data object NavigateToMainMenu : WizardAction

    data class OnBidChange(val playerId: String, val newValue: Int?) : WizardAction
    data class OnTricksWonChange(val playerId: String, val newValue: Int?) : WizardAction

    data class OnSortDirectionChange(val newDirection: SortDirection) : WizardAction

    data object FinishBidding : WizardAction
    data object FinishRound : WizardAction

    data object OnGameFinished : WizardAction

    data object DeleteGame : WizardAction
}