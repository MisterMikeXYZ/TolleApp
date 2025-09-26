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
import de.michael.tolleapp.games.util.keyboards.util.Keyboard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyboardSwitcher(
    keyboards: List<Keyboard>,
    initialKeyboardIndex: Int = 0,
    onSubmit: (Int) -> Unit,
    onHideKeyboard: () -> Unit,
    initialValue: Int? = null,
    minusAllowed: Boolean = false,
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
                    minusAllowed = minusAllowed,
                    modifier = Modifier.fillMaxWidth()
                )
                Keyboard.SKYJO -> TODO()
                Keyboard.DART -> TODO()
                Keyboard.ROMME -> TODO()
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



