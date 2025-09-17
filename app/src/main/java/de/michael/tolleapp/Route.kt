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

    @Serializable
    data object PlayerDeleteScreen : Route

    @Serializable
    data object Schwimmen : Route

    @Serializable
    data object SchwimmenGameScreenCanvas : Route

    @Serializable
    data object SchwimmenGameScreenCircle : Route

    @Serializable
    data object DartStartScreen : Route

    @Serializable
    data object DartGameScreen : Route
}