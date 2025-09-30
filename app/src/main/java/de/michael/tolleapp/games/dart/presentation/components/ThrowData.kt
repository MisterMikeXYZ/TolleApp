package de.michael.tolleapp.games.dart.presentation.components

data class ThrowData(
    val fieldValue: Int,
    val isDouble: Boolean,
    val isTriple: Boolean,
    var isBust: Boolean = false,
    val throwIndex: Int,
)

fun ThrowData.calcScore(): Int? {
    return when {
        isBust -> null
        isTriple -> fieldValue * 3
        isDouble -> fieldValue * 2
        else -> fieldValue
    }
}

fun ThrowData.displayValue(): String {
    return when {
        isTriple -> "T$fieldValue"
        isDouble -> "D$fieldValue"
        else -> fieldValue.toString()
    }
}

data class ThrowAction(
    val playerId: String,
    val throwData: ThrowData,
    val roundIndex: Int
)