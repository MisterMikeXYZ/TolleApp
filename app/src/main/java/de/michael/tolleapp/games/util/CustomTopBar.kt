package de.michael.tolleapp.games.util

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(
    title: String,
    navigationIcon: @Composable (RowScope.() -> Unit) = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        navigationIcon = { Row { navigationIcon.invoke(this) } },
        actions = { actions.invoke(this) },
        modifier = modifier
            .clip(
                shape = MaterialTheme.shapes.extraLarge.copy(
                    topStart = CornerSize(0.dp),
                    topEnd = CornerSize(0.dp),
                )
            ),
    )
}