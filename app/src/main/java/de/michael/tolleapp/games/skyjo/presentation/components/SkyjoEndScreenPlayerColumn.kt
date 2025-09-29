package de.michael.tolleapp.games.skyjo.presentation.components
//
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import de.michael.tolleapp.games.skyjo.presentation.SkyjoState
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import de.michael.tolleapp.ui.theme.AppTheme
//
//
//@Preview
//@Composable
//private fun PlayerScoreDisplaysPrev() {
//    val playerNames = mutableMapOf<String, String>()
//    playerNames.put("1", "Michi")
//    val perPlayerRounds = mutableMapOf<String, List<Int>>()
//    perPlayerRounds.put("1", listOf(2, 3, 4, 5, 6, 7, 100))
//    val totalPoints = mutableMapOf<String, Int>()
//    totalPoints.put("1", perPlayerRounds["1"]!!.sum())
//
//    AppTheme {
//        Surface {
//            SkyjoEndScreenPlayerColumn(
//                playerId = "1",
//                state = SkyjoState(
//                    //playerNames = playerNames,
//                    perPlayerRounds = perPlayerRounds,
//                    totalPoints = totalPoints,
//                ),
//                modifier = Modifier.width(40.dp)
//            )
//        }
//    }
//}
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SkyjoEndScreenPlayerColumn(
//    playerId: String,
//    state: SkyjoState,
//    modifier: Modifier,
//) {
//    Column(
//        modifier = modifier,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        val name = state.playerNames[playerId] ?: "?"
//        Box(
//            modifier = Modifier.padding(4.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                name.take(2),
//                style = MaterialTheme.typography.labelLarge,
//                maxLines = 1,
//            )
//        }
//
//        HorizontalDivider(modifier = Modifier.fillMaxWidth())
//
//        state.perPlayerRounds[playerId]!!.forEach {
//            Box(
//                modifier = Modifier.padding(4.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    it.toString(),
//                    style = MaterialTheme.typography.labelLarge,
//                    maxLines = 1,
//                )
//            }
//        }
//
//        HorizontalDivider(modifier = Modifier.fillMaxWidth())
//
//
//        Box(
//            modifier = Modifier.padding(4.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                state.totalPoints[playerId].toString(),
//                style = MaterialTheme.typography.labelLarge,
//                maxLines = 1,
//            )
//        }
//    }
//}