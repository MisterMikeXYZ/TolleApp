package de.michael.tolleapp.games.util.keyboards.components

import androidx.compose.runtime.MutableState

data class ExtraButton(
    val label: String,
    val enabled: Boolean = true,
    val onClick: (List<Int>) -> List<Int>,
)

enum class ExtraButtonType {
    DOUBLE,
}

fun createExtraButton(type: ExtraButtonType): ExtraButton {
    return when (type) {
        ExtraButtonType.DOUBLE -> ExtraButton(
            label = "2x",
            onClick = { values ->
                if (values.isNotEmpty()) listOf(values.sum() * 2) else values
            }
        )
    }
}