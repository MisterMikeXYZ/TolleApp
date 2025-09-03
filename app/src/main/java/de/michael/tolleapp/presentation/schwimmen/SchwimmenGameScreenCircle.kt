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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.atan2
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.translate

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

    val coroutineScope = rememberCoroutineScope()

    // Disable back button while in game
    BackHandler {
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Schwimmen") },
                navigationIcon = {
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
                        enabled = !state.isGameEnded
                    ) {
                        Icon(
                            imageVector = if (!resetPressedDelete) Icons.Default.Delete
                            else Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = if (!resetPressedDelete) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.error
                        )
                    }
                },
                actions = {
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
                        enabled = !state.isGameEnded
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
            )
        },
    ) { innerPadding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ){
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val waterPainter = rememberVectorPainter(Icons.Default.Water)
                Canvas(
                    modifier = Modifier
                        .size(300.dp)
                        .onSizeChanged { canvasSize = it } // capture size in px
                        //.clickable { viewModel.onCanvasClick() }
                        .pointerInput(players, lives, canvasSize) {
                            // detect taps using canvasSize (IntSize)
                            if (!state.isGameEnded) {
                                detectTapGestures { offset ->
                                    val width = canvasSize.width.toFloat()
                                    val height = canvasSize.height.toFloat()
                                    if (width == 0f || height == 0f) return@detectTapGestures

                                    val radius = minOf(width, height) / 2f
                                    val center = Offset(width / 2f, height / 2f)

                                    val dx = offset.x - center.x
                                    val dy = offset.y - center.y
                                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                                    if (distance > radius) return@detectTapGestures

                                    // Angle in degrees (0° = right, increasing counterclockwise)
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
                    // DrawScope -> here `size` is available
                    val width = size.width
                    val height = size.height
                    val radius = minOf(width, height) / 2f
                    val center = Offset(width / 2f, height / 2f)
                    val sliceAngle = 360f / players.size

                    val colors = listOf(
                        Color(0xFFEF9A9A),
                        Color(0xFF80CBC4),
                        Color(0xFF90CAF9),
                        Color(0xFFFFF59D),
                        Color(0xFFCE93D8),
                        Color(0xFFA5D6A7)
                    )

                    players.forEachIndexed { index, playerId ->
                        val startAngle = index * sliceAngle
                        val sweep = sliceAngle

                        // colored slice
                        drawArc(
                            color = colors[index % colors.size],
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2f, radius * 2f)
                        )

                        // separator line at the slice start
                        val lineAngleRad = startAngle * PI.toFloat() / 180f
                        drawLine(
                            color = Color.Black,
                            start = center,
                            end = Offset(
                                center.x + radius * cos(lineAngleRad),
                                center.y + radius * sin(lineAngleRad)
                            ),
                            strokeWidth = 4f
                        )

                        // lives text
                        val textAngleRad = (startAngle + sweep / 2f) * PI.toFloat() / 180f
                        val textRadius = radius * 0.6f
                        val textX = center.x + textRadius * cos(textAngleRad)
                        val textY = center.y + textRadius * sin(textAngleRad)
                        val playerLivesCount = lives[playerId] ?: 0
                        val heartSize = 100f // font size for each heart
                        val spacing = 120f   // horizontal spacing between hearts
                        val centerX = center.x + textRadius * cos(textAngleRad).toFloat()
                        val centerY = center.y + textRadius * sin(textAngleRad).toFloat()


                        val startX = textX - (playerLivesCount - 2) * spacing / 2 // center the hearts
                        when {
                            playerLivesCount in 2..4 -> {
                                for (i in 0 until playerLivesCount - 1) {
                                    drawContext.canvas.nativeCanvas.drawText(
                                        "❤️",
                                        startX + i * spacing,
                                        textY,
                                        android.graphics.Paint().apply {
                                            color = android.graphics.Color.RED
                                            textSize = heartSize
                                            textAlign = android.graphics.Paint.Align.CENTER
                                        }
                                    )
                                }
                            }

                            playerLivesCount == 1 -> {
                                val iconSize = heartSize // scale factor
                                drawIntoCanvas { canvas ->
                                    with(waterPainter) {
                                        translate(
                                            left = centerX - iconSize / 2,
                                            top = centerY - iconSize / 2
                                        ) {
                                            with(waterPainter) {
                                                draw(
                                                    size = Size(iconSize, iconSize) // correct type
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            playerLivesCount < 1 -> {
                                drawContext.canvas.nativeCanvas.drawText(
                                    "💀",
                                    centerX,
                                    centerY,
                                    android.graphics.Paint().apply {
                                        color = android.graphics.Color.RED
                                        textSize = heartSize
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                )
                            }
                        }

                        // name outside the slice
                        val nameRadius = radius * 1.1f
                        val nameX = center.x + nameRadius * cos(textAngleRad)
                        val nameY = center.y + nameRadius * sin(textAngleRad)
                        drawContext.canvas.nativeCanvas.drawText(
                            state.playerNames[playerId] ?: "Player",
                            nameX,
                            nameY,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 50f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }

                    // final separator (close the circle)
                    drawLine(
                        color = Color.Black,
                        start = center,
                        end = Offset(center.x + radius, center.y),
                        strokeWidth = 4f
                    )
                }
            }
            // Bottom button when game ended
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