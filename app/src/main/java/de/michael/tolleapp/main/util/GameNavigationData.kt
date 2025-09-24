package de.michael.tolleapp.main.util

import de.michael.tolleapp.Route

data class GameNavigationData(
    val name: String,
    val gameRoute: Route,
)

val gameNavigationDataList = listOf(
    GameNavigationData(
        name = "Skyjo",
        gameRoute = Route.SkyjoNav
    ),
    GameNavigationData(
        name = "Dart",
        gameRoute = Route.DartNav
    ),
    GameNavigationData(
        name = "Schwimmen",
        gameRoute = Route.SchwimmenNav
    ),
    GameNavigationData(
        name = "Wizard",
        gameRoute = Route.WizardNav
    ),
    GameNavigationData(
        name = "Randomizer",
        gameRoute = Route.RandomizerNav
    ),
    GameNavigationData(
        name = "Statistik",
        gameRoute = Route.Before.Statistics
    ),
)
