package de.michael.tolleapp.games.skyjo.presentation.components.keyboards

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.games.skyjo.presentation.SkyjoAction
import de.michael.tolleapp.games.skyjo.presentation.SkyjoState
import de.michael.tolleapp.games.skyjo.presentation.SkyjoViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SkyjoKeyboardSwitcher(
    activePlayerId: String?,
    onActivePlayerChange: (String?) -> Unit,
    points: MutableMap<String, Int?>,
    state: SkyjoState,
    onAction: (SkyjoAction) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = state.lastKeyboardPage) { 2 }

    fun handleSubmit(total: Int) {
        activePlayerId?.let { id ->
            points[id] = total
            val currentIndex = state.selectedPlayerIds.indexOf(activePlayerId)
            val nextId = state.selectedPlayerIds.getOrNull((currentIndex + 1) % state.selectedPlayerIds.size)

            if (nextId != null && points[nextId] == null) {
                onActivePlayerChange(nextId)
            } else {
                onActivePlayerChange(null)
                onClose()
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        onAction(SkyjoAction.SetLastKeyboardPage(pagerState.currentPage))
    }

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
            when (page) {
                0 -> SkyjoNormalKeyboard(
                    onSubmit = { handleSubmit(it) },
                    onBack = { onClose() },
                    modifier = Modifier.fillMaxWidth()
                )
                1 -> SkyjoNeleKeyboard(
                    onSubmit = { handleSubmit(it) },
                    onBack = { onClose() },
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
            repeat(2) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}



