package de.michael.tolleapp.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class MainViewModel () : ViewModel(){
    private val _state = MutableStateFlow(MainState())

    val state = _state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainState()
    )
//    TODO
//    Dart: viel
//    Skyjo:
//    1. Leere Spielerauswahl ist scrollable
//    2. Alle Spiele speichern und in Liste anzeigen
//    3.
}