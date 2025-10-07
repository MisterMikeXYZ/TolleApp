package de.michael.tolleapp

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object BeforeNav: Route
    @Serializable
    sealed interface Before: Route {
        @Serializable
        data object StartScreen : Before

        @Serializable
        data object Statistics : Before

        @Serializable
        data object Settings : Before

        @Serializable
        data object PlayerDeleteScreen : Before

        @Serializable
        data object TestScreen : Before
    }

    @Serializable
    data object SkyjoNav: Route
    @Serializable
    sealed interface Skyjo: Route {
        @Serializable
        data object Start : Skyjo
        @Serializable
        data object Game: Skyjo
        @Serializable
        data object End: Skyjo
    }

    @Serializable
    data object SchwimmenNav: Route
    @Serializable
    sealed interface Schwimmen: Route {
        @Serializable
        data object Start : Schwimmen
        @Serializable
        data class Game(val canvas: Boolean): Schwimmen
    }

    @Serializable
    data object DartNav: Route
    @Serializable
    sealed interface Dart: Route {
        @Serializable
        data object Start : Dart
        @Serializable
        data object Game: Dart
    }

    @Serializable
    data object WizardNav: Route
    @Serializable
    sealed interface Wizard: Route {
        @Serializable
        data object Start : Wizard

        @Serializable
        data object Game: Wizard

        @Serializable
        data object End: Wizard
    }

    @Serializable
    data object RommeNav: Route
    @Serializable
    sealed interface Romme : Route {
        @Serializable
        data object Start : Romme

        @Serializable
        data object Game: Romme

        @Serializable
        data object End: Romme
    }

    @Serializable
    data object RandomizerNav: Route
    @Serializable
    sealed interface Randomizer: Route {
        @Serializable
        data object Start : Randomizer
    }

    @Serializable
    data object Flip7Nav: Route
    @Serializable
    sealed interface Flip7: Route {
        @Serializable
        data object Start : Flip7

        @Serializable
        data object Game : Flip7

        @Serializable
        data object End : Flip7
    }
}