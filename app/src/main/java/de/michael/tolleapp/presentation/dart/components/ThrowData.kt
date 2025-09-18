package de.michael.tolleapp.presentation.dart.components

data class ThrowData(
    val fieldValue: Int,
    val isDouble: Boolean,
    val isTriple: Boolean,
    val throwIndex: Int,
)

fun ThrowData.calcScore(): Int {
    return when {
        isTriple -> fieldValue * 3
        isDouble -> fieldValue * 2
        else -> fieldValue
    }
}

data class ThrowAction(
    val playerId: String,
    val throwData: ThrowData,
    val roundIndex: Int
)