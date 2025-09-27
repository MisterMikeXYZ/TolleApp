package de.michael.tolleapp.games.romme.presentation

sealed interface RommeAction {
    data object NavigateToMainMenu : RommeAction

    data class OnRoundScoreChange(val playerId: String, val newValue: Int) : RommeAction

    data object FinishRound : RommeAction

    data object OnGameFinished : RommeAction

    data object DeleteGame : RommeAction
}