package de.michael.tolleapp.games.randomizer.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.Route
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.PI
import kotlin.math.atan2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomizerScreen(
    viewModel: RandomizerViewModel = koinViewModel(),
    navigateTo: (Route) -> Unit,
    navigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (expanded) 2.5f else 1f)

    val transition = updateTransition(targetState = expanded, label = "expandTransition")

    val animatedSize by transition.animateDp(
        transitionSpec = {
            if (targetState) {
                // expanding
                tween(durationMillis = 600, easing = FastOutSlowInEasing)
            } else {
                // collapsing
                tween(durationMillis = 400, easing = LinearOutSlowInEasing)
            }
        },
        label = "circleSize"
    ) { state ->
        if (state) 400.dp else 150.dp
    }

    val animatedTextSize by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                tween(durationMillis = 600, easing = FastOutSlowInEasing)
            } else {
                tween(durationMillis = 400, easing = LinearOutSlowInEasing)
            }
        },
        label = "textSize"
    ) { state ->
        if (state) 100f else 40f
    }

    val sliceColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    val textColor = android.graphics.Color.WHITE
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    var showCreatePlayerDialog by remember { mutableStateOf(false) }
    var newPlayerName by remember { mutableStateOf("") }
    var pendingRowIndex by remember { mutableStateOf<Int?>(null) }

    val presets by viewModel.presets.collectAsState(initial = emptyList())
    var presetExpanded by remember { mutableStateOf(false) }
    var showPresetDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    if (showCreatePlayerDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlayerDialog = false },
            title = { Text("Neuen Spieler erstellen") },
            text = {
                TextField(
                    value = newPlayerName,
                    onValueChange = { newPlayerName = it },
                    label = { Text("Spielername") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newPlayerName.trim()
                        if (name.isNotEmpty() && pendingRowIndex != null) {
                            viewModel.addPlayer(name, pendingRowIndex!!)
                        }
                        newPlayerName = ""
                        showCreatePlayerDialog = false
                    }
                ) {
                    Text("Erstellen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        newPlayerName = ""
                        showCreatePlayerDialog = false
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
    if (showPresetDialog) {
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            title = { Text("Neues Preset erstellen") },
            text = {
                TextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    label = { Text("Preset Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = newPresetName.trim()
                    if (name.isNotEmpty()) {
                        viewModel.createPreset(
                            "randomizer",
                            name,
                            state.selectedPlayerIds.filterNotNull()
                        )
                    }
                    newPresetName = ""
                    showPresetDialog = false
                }) {
                    Text("Erstellen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPresetDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.reset()
    }

    BackHandler {
        if (state.randomizerType != "Zufallsgenerator") {
            viewModel.reset()
        } else {
            navigateBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.randomizerType) },
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
                    IconButton(
                        onClick = {
                            if (state.randomizerType != "Zufallsgenerator") {
                                viewModel.reset()
                            } else {
                                navigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            if (state.randomizerType == "Zufallsgenerator") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    if (!expanded) {
                        // Collapsed circle
                        Button(
                            onClick = { expanded = true },
                            shape = CircleShape,
                            modifier = Modifier.size(150.dp)
                        ) {
                            Text("Select Type")
                        }
                    } else {
                        val pieceLabels = listOf("Numbers", "Names", "Colors", "Items")
                        val anglePerPiece = 360f / pieceLabels.size

                        Canvas(
                            modifier = Modifier
                                .size(animatedSize)
                                .pointerInput(Unit) {
                                    // Handle taps inside circle
                                    detectTapGestures { offset ->
                                        val center = Offset(size.width / 2f, size.height / 2f)
                                        val dx = offset.x - center.x
                                        val dy = offset.y - center.y
                                        val angle = (atan2(dy, dx) * 180f / PI + 360f) % 360f

                                        val clickedIndex = (angle / anglePerPiece).toInt()
                                        val type = pieceLabels[clickedIndex]

                                        viewModel.setRandomizerType(type)
                                        expanded = false
                                    }

                                }
                        ) {
                            val radius = size.minDimension / 2
                            val rect = Rect(
                                Offset(size.width / 2 - radius, size.height / 2 - radius),
                                Size(radius * 2, radius * 2)
                            )

                            pieceLabels.forEachIndexed { i, label ->
                                val startAngle = i * anglePerPiece
                                drawArc(
                                    color = sliceColor,
                                    startAngle = startAngle,
                                    sweepAngle = anglePerPiece,
                                    useCenter = true,
                                    topLeft = rect.topLeft,
                                    size = rect.size
                                )

                                val midAngleRad =
                                    Math.toRadians((startAngle + anglePerPiece / 2).toDouble())
                                val textRadius = radius * 0.6f // move text towards center
                                val textX =
                                    size.width / 2 + textRadius * kotlin.math.cos(midAngleRad)
                                        .toFloat()
                                val textY =
                                    size.height / 2 + textRadius * kotlin.math.sin(midAngleRad)
                                        .toFloat()

                                drawContext.canvas.nativeCanvas.drawText(
                                    label,
                                    textX,
                                    textY,
                                    android.graphics.Paint().apply {
                                        color = textColor
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        textSize = animatedTextSize
                                        isAntiAlias = true
                                    }
                                )
                            }

                            pieceLabels.forEachIndexed { i, label ->
                                val startAngle = i * anglePerPiece
                                val lineAngleRad = Math.toRadians((startAngle + anglePerPiece).toDouble())
                                val lineX = size.width / 2 + radius * kotlin.math.cos(lineAngleRad).toFloat()
                                val lineY = size.height / 2 + radius * kotlin.math.sin(lineAngleRad).toFloat()

                                drawLine(
                                    color = onSurfaceColor,
                                    start = Offset(size.width / 2f, size.height / 2f),
                                    end = Offset(lineX, lineY),
                                    strokeWidth = 8f
                                )
                            }
                        }
                    }
                }
            }
            if (state.randomizerType == "Numbers") {
                Text("Hallo")
            }
            if (state.randomizerType == "Names") {
                Box (modifier = Modifier

                    .fillMaxWidth()
                ) {
                    Button(
                        onClick = { presetExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Presets")
                    }
                    DropdownMenu(
                        expanded = presetExpanded,
                        onDismissRequest = { presetExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Neues Preset erstellen") },
                            onClick = {
                                presetExpanded = false
                                showPresetDialog = true
                            },
                        )
                        presets.forEach { presetWithPlayers ->
                            DropdownMenuItem(
                                text = { Text(presetWithPlayers.preset.name) },
                                onClick = {
                                    viewModel.resetSelectedPlayers()
                                    presetWithPlayers.players.forEachIndexed { index, presetPlayer ->
                                        viewModel.selectPlayer(index, presetPlayer.playerId)
                                    }
                                    presetExpanded = false
                                },
                                trailingIcon = {
                                    var resetPressedDelete by remember { mutableStateOf(false) }
                                    LaunchedEffect(resetPressedDelete) {
                                        if (resetPressedDelete) {
                                            delay(2000)
                                            resetPressedDelete = false
                                        }
                                    }
                                    IconButton(onClick = {
                                        if (!resetPressedDelete) resetPressedDelete = true
                                        else {
                                            viewModel.deletePreset(presetWithPlayers.preset.id)
                                            presetExpanded = false
                                            resetPressedDelete = false
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (!resetPressedDelete) Icons.Default.Delete
                                            else Icons.Default.DeleteForever,
                                            contentDescription = null,
                                            tint = if (!resetPressedDelete) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(3.dp))
                Column (
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize()
                ){
                    state.selectedPlayerIds.forEachIndexed { index, selectedId ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            var expanded by remember { mutableStateOf(false) }
                            val selectedPlayer = state.selectedPlayerIds[index]?.let { state.playerNames[it] } ?: "Spieler auswählen"

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = {
                                    if (index == 0 || state.selectedPlayerIds[index - 1] != null) {
                                        expanded = !expanded
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                TextField(
                                    value = selectedPlayer,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Spieler") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Neuen Spieler erstellen…") },
                                        onClick = {
                                            expanded = false
                                            pendingRowIndex = index
                                            showCreatePlayerDialog = true
                                        }
                                    )
                                    state.playerNames.filter { (id, _) -> id !in state.selectedPlayerIds } //THIS
                                        .forEach { (id, name) ->
                                            DropdownMenuItem(
                                                text = { Text(name) }, //THIS
                                                onClick = {
                                                    viewModel.selectPlayer(index, id) //THIS
                                                    expanded = false
                                                }
                                            )
                                        }

                                }
                            }

                            //X Button to remove player
                            if (index >= 2) {
                                IconButton(
                                    onClick = { viewModel.removePlayer(index) },
                                    enabled = index < state.selectedPlayerIds.size - 1
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Entfernen",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = if (index < state.selectedPlayerIds.size - 1) 1f else 0.3f
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
