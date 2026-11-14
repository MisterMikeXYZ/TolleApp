package de.michael.tolleapp.games.util.endScreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.michael.tolleapp.ui.theme.AppTheme

/** Players are already sorted descending by total score; Alice and Bob are tied for first place. */
@Preview(showBackground = true)
@Composable
private fun EndScreenTiePreview() {
    AppTheme {
        EndScreen(
            titleValue = "Wizard",
            sortedPlayerNames = listOf("Alice", "Bob", "Charlie", "Diana"),
            sortedScoreValues = listOf(
                listOf("30 | 1", "30 | 1", "20 | 0", "-10 | 2"),
                listOf("70 | 2", "50 | 1", "40 | 1", "10 | 0"),
                listOf("120 | 3", "120 | 2", "60 | 0", "-20 | 1"),
            ),
            sortedTotalValues = listOf("120", "120", "60", "-20"),
            navigateToMainMenu = {},
            showWinner = true,
            ranks = listOf(1, 1, 3, 4),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EndScreenNoTiePreview() {
    AppTheme {
        EndScreen(
            titleValue = "Wizard",
            sortedPlayerNames = listOf("Alice", "Bob", "Charlie"),
            sortedScoreValues = listOf(
                listOf("30 | 1", "20 | 0", "-10 | 2"),
                listOf("90 | 2", "40 | 1", "10 | 0"),
            ),
            sortedTotalValues = listOf("90", "40", "10"),
            navigateToMainMenu = {},
            showWinner = true,
        )
    }
}
