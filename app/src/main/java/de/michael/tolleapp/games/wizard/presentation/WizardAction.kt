package de.michael.tolleapp.games.wizard.presentation

sealed interface WizardAction {
    data object NavigateToMainMenu : WizardAction

    data class OnBidChange(val playerId: String, val newValue: Int?) : WizardAction
    data class OnTricksWonChange(val playerId: String, val newValue: Int?) : WizardAction

    data object FinishBidding : WizardAction
    data object FinishRound : WizardAction

    data object OnGameFinished : WizardAction
}