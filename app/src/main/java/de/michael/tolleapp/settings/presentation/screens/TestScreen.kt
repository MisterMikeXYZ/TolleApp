package de.michael.tolleapp.settings.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.games.util.CustomTopBar

@Composable
fun TestScreen(
    navigateBack: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Theme Preview",
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
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
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> ColorsPage()
                    1 -> TypographyPage()
                }
            }

            // Page Indicators
            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(2) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(if (isSelected) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorsPage() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Material Colors",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ColorSample("Primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
        ColorSample("Secondary", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary)
        ColorSample("Tertiary", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary)
        ColorSample("PrimaryContainer", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        ColorSample("SecondaryContainer", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        ColorSample("TertiaryContainer", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        ColorSample("Error", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError)
        ColorSample("ErrorContainer", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        ColorSample("Surface", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
        ColorSample("SurfaceVariant", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        ColorSample("Outline", MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ColorSample(name: String, background: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(0.9f)
            .background(background)
            .padding(16.dp)
    ) {
        Text(
            text = "$name\nOn$name",
            color = textColor
        )
    }
}

@Composable
private fun TypographyPage() {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Typography", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        TypographySample("Display Large", MaterialTheme.typography.displayLarge)
        TypographySample("Display Medium", MaterialTheme.typography.displayMedium)
        TypographySample("Display Small", MaterialTheme.typography.displaySmall)
        TypographySample("Headline Large", MaterialTheme.typography.headlineLarge)
        TypographySample("Headline Medium", MaterialTheme.typography.headlineMedium)
        TypographySample("Headline Small", MaterialTheme.typography.headlineSmall)
        TypographySample("Title Large", MaterialTheme.typography.titleLarge)
        TypographySample("Title Medium", MaterialTheme.typography.titleMedium)
        TypographySample("Title Small", MaterialTheme.typography.titleSmall)
        TypographySample("Body Large", MaterialTheme.typography.bodyLarge)
        TypographySample("Body Medium", MaterialTheme.typography.bodyMedium)
        TypographySample("Body Small", MaterialTheme.typography.bodySmall)
        TypographySample("Label Large", MaterialTheme.typography.labelLarge)
        TypographySample("Label Medium", MaterialTheme.typography.labelMedium)
        TypographySample("Label Small", MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TypographySample(label: String, style: androidx.compose.ui.text.TextStyle) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(text = "Lorem ipsum dolor sit amet...", style = style)
    }
}
