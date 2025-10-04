package de.michael.tolleapp.games.util.keyboards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.games.util.keyboards.components.ExtraButtonType
import de.michael.tolleapp.games.util.keyboards.util.Keyboard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyboardSwitcher(
    keyboards: List<Keyboard>,
    initialKeyboardIndex: Int = 0,
    onSubmit: (Int) -> Unit,
    onHideKeyboard: () -> Unit,
    initialValue: Int? = null,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = initialKeyboardIndex) { keyboards.size }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(8.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when (keyboards[page]) {
                Keyboard.NUMBER -> NumberKeyboard(
                    onSubmit = onSubmit,
                    hideKeyboard = onHideKeyboard,
                    initialValue = initialValue,
                    minusAllowed = false,
                    withDoubleSubmit = keyboards[page] == Keyboard.NUMBER_WITH_2X,
                    modifier = Modifier.fillMaxWidth()
                )
                Keyboard.NUMBER_WITH_2X -> NumberKeyboard(
                    onSubmit = onSubmit,
                    hideKeyboard = onHideKeyboard,
                    initialValue = initialValue,
                    minusAllowed = false, //keyboards[page] == Keyboard.NUMBER_WITH_MINUS,
                    withDoubleSubmit = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Keyboard.FLIP7 -> CustomSingleCardKeyboard(
                    onSubmit = onSubmit,
                    onBack = onHideKeyboard,
                    lowestNumber = 0,
                    highestNumber = 12,
                    higherNumbers = listOf(15),
                    numberOfRows = 4,
                    extraButtonTypes = listOf(ExtraButtonType.DOUBLE),
                    modifier = Modifier.fillMaxWidth()
                )
                Keyboard.NUMBER_WITH_MINUS -> NumberKeyboard(
                    onSubmit = onSubmit,
                    hideKeyboard = onHideKeyboard,
                    initialValue = initialValue,
                    minusAllowed = true, //keyboards[page] == Keyboard.NUMBER_WITH_MINUS,
                    withDoubleSubmit = keyboards[page] == Keyboard.NUMBER_WITH_2X,
                    modifier = Modifier.fillMaxWidth()
                )
                Keyboard.SKYJO -> SkyjoNeleKeyboard(
                    onSubmit = onSubmit,
                    onBack = onHideKeyboard,
                    modifier = Modifier.fillMaxWidth()
                )
                Keyboard.DART -> TODO()
                Keyboard.ROMME -> RommeKeyboard(
                    onSubmit = onSubmit,
                    hideKeyboard = onHideKeyboard,
                    initialValue = initialValue?.let { listOf(it) } ?: emptyList(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(keyboards.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(8.dp)
                        .background(
                            color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}



