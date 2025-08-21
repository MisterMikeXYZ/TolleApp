package de.michael.tolleapp

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Main : Route

    @Serializable
    data object Skyjo : Route

    @Serializable
    data object SkyjoGame : Route

    @Serializable
    data object SkyjoEnd : Route

    @Serializable
    data object Statistics : Route

    @Serializable
    data object Settings : Route
}