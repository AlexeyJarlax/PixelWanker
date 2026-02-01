package com.pavlovalexey.pavlovAlexeySandbox.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pavlovalexey.pavlovAlexeySandbox.R
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.LocalHazeState
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp16
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp8
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

@Composable
fun StartBottomBar(
    currentPage: Int,
    onSelectPage: (Int) -> Unit,
    onShowAbout: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    barHeight: Dp,
) {
    val hazeState = LocalHazeState.current

    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val selectedColor = onSurface
    val unselectedColor = onSurface.copy(alpha = 0.70f)

    val glassStyle = remember(surface) {
        HazeStyle(
            backgroundColor = surface.copy(alpha = 0.06f),
            tint = HazeTint(surface.copy(alpha = 0.28f)),
            blurRadius = dp8,
            noiseFactor = 0.24f,
            fallbackTint = HazeTint(surface.copy(alpha = 0.68f))
        )
    }

    val shape = RoundedCornerShape(topStart = dp16, topEnd = dp16)
    val fallback = surface.copy(alpha = 0.78f)

    val items = listOf(
        BottomNavItem(stringResource(R.string.bottom_nav_grid), currentPage == 0) { onSelectPage(0) },
        BottomNavItem(stringResource(R.string.bottom_nav_apps), currentPage == 1) { onSelectPage(1) },
        BottomNavItem(stringResource(R.string.bottom_nav_about), false, onShowAbout),
        BottomNavItem(stringResource(R.string.bottom_nav_exit), false, onExit),
    )

    val barModifier = modifier
        .height(barHeight)
        .clip(shape)
        .then(
            if (hazeState != null) {
                Modifier.hazeEffect(state = hazeState, style = glassStyle) {
                    // КЛЮЧЕВАЯ ПРАВКА: принудительно синхронизируем blur с изменениями подложки
                    forceInvalidateOnPreDraw = true
                }
            } else {
                Modifier.background(fallback)
            }
        )

    NavigationBar(
        modifier = barModifier.fillMaxSize().padding(bottom = dp8),
        windowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.selected,
                onClick = item.onClick,
                icon = { Spacer(modifier = Modifier.size(0.dp)) },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    unselectedIconColor = unselectedColor,
                    selectedTextColor = selectedColor,
                    unselectedTextColor = unselectedColor,
                    indicatorColor = Color.Transparent
                ),
                alwaysShowLabel = true
            )
        }
    }
}

private data class BottomNavItem(
    val title: String,
    val selected: Boolean,
    val onClick: () -> Unit,
)
