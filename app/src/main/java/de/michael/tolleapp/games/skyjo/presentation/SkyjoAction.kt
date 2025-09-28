package de.michael.tolleapp.games.skyjo.presentation

sealed interface SkyjoAction {
    data object NavigateToMainMenu : SkyjoAction

    data object advanceDealer : SkyjoAction


    data class setDealer(val dealerId: String) : SkyjoAction



}