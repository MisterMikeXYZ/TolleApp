package de.michael.tolleapp.presentation.schwimmen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.Route
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchwimmenGameScreenCircle(
    viewModel: SchwimmenViewModel = koinViewModel(),
    navigateTo: (Route) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize(0, 0)) }

    val players = state.selectedPlayerIds.filterNotNull()
    val lives = state.perPlayerRounds

    val sliceColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
    val separatorColor = MaterialTheme.colorScheme.outline
    val errorArgb = MaterialTheme.colorScheme.error.toArgb()
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface.toArgb()

    val coroutineScope = rememberCoroutineScope()

    BackHandler {}

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Schwimmen") },
                navigationIcon = {
                    if (!state.isGameEnded) {
                        var resetPressedDelete by remember { mutableStateOf(false) }
                        LaunchedEffect(resetPressedDelete) {
                            if (resetPressedDelete) {
                                delay(2000)
                                resetPressedDelete = false
                            }
                        }
                        IconButton(
                            onClick = {
                                if (!resetPressedDelete) resetPressedDelete = true
                                else {
                                    viewModel.deleteGame()
                                    navigateTo(Route.Main)
                                    resetPressedDelete = false
                                }
                            },
                        ) {
                            Icon(
                                imageVector = if (!resetPressedDelete) Icons.Default.Delete
                                else Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = if (!resetPressedDelete) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                actions = {
                    if (!state.isGameEnded) {
                        var resetPressedSave by remember { mutableStateOf(false) }
                        LaunchedEffect(resetPressedSave) {
                            if (resetPressedSave) {
                                delay(2000)
                                resetPressedSave = false
                            }
                        }
                        IconButton(
                            onClick = {
                                if (!resetPressedSave) resetPressedSave = true
                                else {
                                    viewModel.pauseCurrentGame()
                                    navigateTo(Route.Main)
                                    resetPressedSave = false
                                }
                            },
                        ) {
                            Icon(
                                imageVector = if (!resetPressedSave) Icons.Default.Save
                                else Icons.Default.SaveAs,
                                contentDescription = null,
                                tint = if (!resetPressedSave)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(300.dp)
                        .onSizeChanged { canvasSize = it }
                        .pointerInput(players, lives, canvasSize) {
                            if (!state.isGameEnded) {
                                detectTapGestures { offset ->
                                    val width = canvasSize.width.toFloat()
                                    val height = canvasSize.height.toFloat()
                                    if (width == 0f || height == 0f) return@detectTapGestures

                                    val radius = minOf(width, height) / 2f
                                    val center = Offset(width / 2f, height / 2f)

                                    val dx = offset.x - center.x
                                    val dy = offset.y - center.y
                                    val distance = sqrt(dx * dx + dy * dy)
                                    if (distance > radius) return@detectTapGestures

                                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                                    if (angle < 0) angle += 360.0

                                    val sliceAngle = 360f / players.size
                                    val sliceIndex = (angle / sliceAngle).toInt()

                                    if (sliceIndex in players.indices) {
                                        val tappedPlayer = players[sliceIndex]
                                        coroutineScope.launch {
                                            viewModel.endRound(tappedPlayer)
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val radius = minOf(width, height) / 2f
                    val center = Offset(width / 2f, height / 2f)
                    val sliceAngle = 360f / players.size
                    val elementScale = 2f

                    players.forEachIndexed { index, playerId ->
                        val startAngle = index * sliceAngle

                        // Slice
                        drawArc(
                            color = sliceColor,
                            startAngle = startAngle,
                            sweepAngle = sliceAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2f, radius * 2f)
                        )

                        // Separator
                        val lineAngleRad = startAngle * PI.toFloat() / 180f
                        drawLine(
                            color = separatorColor,
                            start = center,
                            end = Offset(
                                center.x + radius * cos(lineAngleRad),
                                center.y + radius * sin(lineAngleRad)
                            ),
                            strokeWidth = 3.dp.toPx()//1.5f,
                            //style = Stroke(width = 3.dp.toPx()),
                        )

                        // Lives
                        val playerLivesCount = lives[playerId] ?: 0
                        val textAngleRad = (startAngle + sliceAngle / 2f) * PI.toFloat() / 180f
                        val textRadius = radius * 0.6f
                        val textX = center.x + textRadius * cos(textAngleRad)
                        val textY = center.y + textRadius * sin(textAngleRad)
                        val iconSize = 40f * elementScale
                        val spacing = 50f * elementScale
                        val count = playerLivesCount - 1
                        val totalWidth = (count - 1) * spacing
                        val startX = textX - totalWidth / 2f

                        when {
                            playerLivesCount > 1 -> {
                                repeat(playerLivesCount - 1) { i ->
                                    drawContext.canvas.nativeCanvas.drawText(
                                        "♥",
                                        startX + i * spacing,
                                        textY,
                                        android.graphics.Paint().apply {
                                            color = primary.toArgb()
                                            textSize = iconSize
                                            textAlign = android.graphics.Paint.Align.CENTER
                                        }
                                    )
                                }
                            }
                            playerLivesCount == 1 -> {
                                drawContext.canvas.nativeCanvas.drawText(
                                    "\uD83C\uDF0A", // water drop instead of painter
                                    textX,
                                    textY,
                                    android.graphics.Paint().apply {
                                        color = primary.toArgb()
                                        textSize = iconSize
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                )
                            }
                            else -> {
                                drawContext.canvas.nativeCanvas.drawText(
                                    "☠",
                                    textX,
                                    textY + iconSize,
                                    android.graphics.Paint().apply {
                                        color = errorArgb
                                        textSize = iconSize * 3f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                )
                            }
                        }
                        // Player name (curved around circle, 8.dp outside)
                        val nameRadius = radius + 8.dp.toPx()
                        val nameAngleRad = (startAngle + sliceAngle / 2f) * PI.toFloat() / 180f
                        val nameX = center.x + nameRadius * cos(nameAngleRad)
                        val nameY = center.y + nameRadius * sin(nameAngleRad)

                        val playerName = state.playerNames[playerId] ?: "Player"

                        drawIntoCanvas { canvas ->
                            val paint = android.graphics.Paint().apply {
                                color = onSurface
                                textSize = 36f * elementScale
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }

                            canvas.save()
                            canvas.translate(nameX, nameY)
                            canvas.rotate(Math.toDegrees(nameAngleRad.toDouble()).toFloat() + 90f)
                            canvas.nativeCanvas.drawText(playerName, 0f, 0f, paint)
                            canvas.restore()
                        }
                    }

                    // Close circle
                    drawLine(
                        color = separatorColor,
                        start = center,
                        end = Offset(center.x + radius, center.y),
                        strokeWidth = 3.dp.toPx()
                    )

                    // Circle outline
                    drawCircle(
                        color = separatorColor,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            if (state.isGameEnded) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        navigateTo(Route.Main)
                        viewModel.deleteGame()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Back to Main")
                }
            }
        }
    }
}