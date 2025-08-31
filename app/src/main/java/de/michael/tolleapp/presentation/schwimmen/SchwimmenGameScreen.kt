package de.michael.tolleapp.presentation.schwimmen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun SchwimmenGameScreen(
    viewModel: SchwimmenViewModel = koinViewModel(),
    onLifeClick: (playerId: String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    val players = state.selectedPlayerIds.filterNotNull()
    val lives = state.playerLives

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(300.dp)) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)
            val sliceAngle = 360f / players.size

            players.forEachIndexed { index, playerId ->
                val startAngle = index * sliceAngle
                val playerLives = lives[playerId] ?: 0

                // Draw slice
                drawArc(
                    color = Color(0xFF90CAF9),
                    startAngle = startAngle,
                    sweepAngle = sliceAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                )

                // Draw remaining lives inside slice
                val textAngle = (startAngle + sliceAngle / 2) * (PI / 180)
                val textRadius = radius * 0.6f
                val textX = center.x + textRadius * cos(textAngle).toFloat()
                val textY = center.y + textRadius * sin(textAngle).toFloat()
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "$playerLives ❤️",
                        textX,
                        textY,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 36f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }

                // Draw player name outside
                val nameRadius = radius * 1.1f
                val nameX = center.x + nameRadius * cos(textAngle).toFloat()
                val nameY = center.y + nameRadius * sin(textAngle).toFloat()
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        state.playerNames[playerId] ?: "Player",
                        nameX,
                        nameY,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}
