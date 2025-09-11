package de.michael.tolleapp.presentation.schwimmen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.Route
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchwimmenGameScreenCanvas(
    viewModel: SchwimmenViewModel = koinViewModel(),
    navigateTo: (Route) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize(0, 0)) }

    val players = state.selectedPlayerIds.filterNotNull()
    val coroutineScope = rememberCoroutineScope()

    val textColor = colorScheme.onSurface
    val riverColor = if (isSystemInDarkTheme()) Color(0xFF0D47A1) else Color(0xFF5AB0FF)

    val boatStates = remember(state.perPlayerRounds) {
        mutableStateMapOf<String, Int>().apply {
            state.perPlayerRounds.forEach { (id, lives) ->
                val value = when (lives) {
                    4 -> 0 // all boats intact
                    3 -> 1
                    2 -> 2
                    1 -> 3 // swimmer
                    else -> 4 // skull
                }
                this[id] = value
            }
        }
    }

    BackHandler { }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    "Schwimmen",
                    color = MaterialTheme.colorScheme.onSurface
                ) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier
                    .clip(
                        shape = MaterialTheme.shapes.extraLarge.copy(
                            topStart = CornerSize(0.dp),
                            topEnd = CornerSize(0.dp),
                        )
                    ),
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
                                tint = if (!resetPressedDelete) colorScheme.onSurface
                                else colorScheme.error
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
                                    colorScheme.onSurface
                                else
                                    colorScheme.primary
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
            players.forEach { playerId ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { canvasSize = it }
                            //.clickable { viewModel.onCanvasClick() }
                            .pointerInput(playerId) {
                                if (!state.isGameEnded) {
                                    detectTapGestures { _ ->
                                        val current = boatStates[playerId] ?: 0
                                        val next = when (current) {
                                            in 0..1 -> current + 1 // sink first two boats
                                            2 -> 3 // sink third boat and instantly show swimmer
                                            3 -> 4 // swimmer gone → skull
                                            else -> 4
                                        }
                                        boatStates[playerId] = next
                                        coroutineScope.launch { viewModel.endRound(playerId) }
                                    }
                                }
                            }
                    ) {
                        val stateValue = boatStates[playerId] ?: 0

                        fun drawBoat(center: Offset, sunk: Boolean = false) {
                            val hull = Path().apply {
                                moveTo(center.x - 80f, center.y + 20f)
                                lineTo(center.x - 50f, center.y + 50f)
                                lineTo(center.x + 50f, center.y + 50f)
                                lineTo(center.x + 80f, center.y + 20f)
                                lineTo(center.x + 20f, center.y + 20f)
                                lineTo(center.x + 20f, center.y - 50f)
                                lineTo(center.x + 10f, center.y - 50f)
                                lineTo(center.x + 10f, center.y + 20f)
                                close()
                            }
                            drawPath(
                                hull,
                                color = if (sunk) Color(0xFF8B3E2F).copy(alpha = 0f) else Color(0xFF8B3E2F),
                                style = Fill
                            )

                            val sail = Path().apply {
                                moveTo(center.x + 10f, center.y - 50f)   // top of mast
                                lineTo(center.x + 10f, center.y + 10f)   // bottom of mast
                                lineTo(center.x - 70f, center.y - 20f)   // outer corner of sail
                                close()
                            }
                            drawPath(
                                sail,
                                color = if (sunk) Color(0xFFF7E08A).copy(alpha = 0f) else Color(0xFFF7E08A),
                                style = Fill
                            )
                        }

                        val w = size.width
                        val h = size.height

                        // Define river curve
                        val riverTop = h * 0.5f
                        val riverBottom = h
                        val riverPath = Path().apply {
                            moveTo(0f, riverTop)
                            quadraticBezierTo(w * 0.25f, riverTop - h * 0.05f, w * 0.5f, riverTop)
                            quadraticBezierTo(w * 0.75f, riverTop + h * 0.05f, w, riverTop)
                            lineTo(w, riverBottom)
                            lineTo(0f, riverBottom)
                            close()
                        }
                        drawPath(riverPath, color = riverColor, style = Fill)

                        //Draw text in the middle of the river
                        val playerName = state.playerNames[playerId] ?: playerId

                        // Determine ranking text
                        val rankText = if (state.isGameEnded) {
                            val rankIndex = state.ranking.indexOf(playerId)
                            val winnerId = state.winnerId
                            var winnerLives = state.perPlayerRounds[winnerId]!! - 1
                            if (rankIndex == 0) "🏆 ($winnerLives lives left)" else ", Platz ${rankIndex + 1}"
                        } else ""
                        val paint = android.graphics.Paint().apply {
                            color = textColor.toArgb()
                            textSize = 80f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true }
                        drawContext.canvas.nativeCanvas.drawText(
                            "$playerName$rankText",
                            size.width / 2f,       // centered horizontally in this player's Box
                            size.height * 0.9f,    // near the bottom of this player's Box
                            paint
                        )

                        // River control points
                        val p0 = Offset(0f, h * 0.5f)
                        val p1 = Offset(w * 0.25f, h * 0.45f)
                        val p2 = Offset(w * 0.5f, h * 0.5f)
                        val p3 = Offset(w * 0.75f, h * 0.55f)
                        val p4 = Offset(w, h * 0.5f)

                        // Compute Y of quadratic Bézier for first segment (0 <= x <= w/2)
                        fun bezierY(x: Float, start: Offset, control: Offset, end: Offset): Float {
                            // Solve t from x = (1-t)^2*start.x + 2*(1-t)*t*control.x + t^2*end.x
                            // For simplicity, approximate t linearly
                            val t = (x - start.x) / (end.x - start.x)
                            val y = (1-t)*(1-t)*start.y + 2*(1-t)*t*control.y + t*t*end.y
                            return y
                        }

                        // Function to get river Y at a given X
                        fun riverY(x: Float): Float {
                            return if (x <= w/2f) {
                                bezierY(x, p0, p1, p2)
                            } else {
                                bezierY(x, p2, p3, p4)
                            }
                        }

                        // Boat positions
                        val boatXPositions = listOf(w * 0.2f, w * 0.5f, w * 0.8f)
                        boatXPositions.forEachIndexed { index, x ->
                            val y = riverY(x) - h * 0.02f  // slightly above the river
                            val sunk = stateValue > index
                            drawBoat(Offset(x, y), sunk)
                        }

                        // Swimmer or skull
                        val swimmerX = w * 0.5f
                        val swimmerY = riverY(swimmerX) - h * 0.04f
                        if (stateValue == 3) {
                            drawCircle(Color(0xFFFFC107), radius = h * 0.05f, center = Offset(swimmerX, swimmerY))
                            drawLine(Color.Black, start = Offset(swimmerX, swimmerY + h * 0.02f), end = Offset(swimmerX, swimmerY + h * 0.12f), strokeWidth = 6f)
                        } else if (stateValue == 4) {
                            drawCircle(Color.White, radius = h * 0.06f, center = Offset(swimmerX, swimmerY))
                            drawCircle(Color.Black, radius = h * 0.012f, center = Offset(swimmerX - w * 0.01f, swimmerY - h * 0.01f))
                            drawCircle(Color.Black, radius = h * 0.012f, center = Offset(swimmerX + w * 0.01f, swimmerY - h * 0.01f))
                            drawLine(Color.Black, start = Offset(swimmerX - w * 0.02f, swimmerY + h * 0.02f), end = Offset(swimmerX + w * 0.02f, swimmerY + h * 0.02f), strokeWidth = 3f)
                        }
                    }
                }
            }
            // Bottom button when game ended
            if (state.isGameEnded) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        navigateTo(Route.Main)
                        viewModel.deleteGame()},
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