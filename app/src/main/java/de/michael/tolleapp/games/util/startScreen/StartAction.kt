package de.michael.tolleapp.games.util.startScreen

sealed interface StartAction {
    data class CreatePlayer(val name: String, val index: Int) : StartAction
    data class SelectPlayer(val index: Int, val playerId: String) : StartAction
    data class UnselectPlayer(val index: Int) : StartAction
    data object ResetSelectedPlayers : StartAction

    data class CreatePreset(val presetName: String, val playerIds: List<String>) : StartAction
    data class SelectPreset(val presetId: Long) : StartAction
    data class DeletePreset(val presetId: Long) : StartAction

    data object StartGame : StartAction
    data class ResumeGame(val gameId: String) : StartAction
    data class DeleteGame(val gameId: String) : StartAction

    data object NavigateToMainMenu : StartAction
    data object NavigateToGame : StartAction
}