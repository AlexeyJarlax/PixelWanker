package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.start

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridSettingsStore
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridUserSettings
import com.pavlovalexey.pavlovAlexeySandbox.repository.InstalledAppsRepositoryImpl
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist.InstalledAppsViewModel
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist.InstalledAppsViewModelFactory
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.LocalHazeState
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp0
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp16
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp20
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp8
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StartScreen(
    onAppClick: (String) -> Unit,
    appsViewModel: InstalledAppsViewModel? = null,
) {
    val context = LocalContext.current
    val repository = remember(context) { InstalledAppsRepositoryImpl(context.applicationContext) }
    val factory = remember(repository) { InstalledAppsViewModelFactory(repository) }
    val resolvedViewModel = appsViewModel ?: viewModel(factory = factory)

    val appsUiState by resolvedViewModel.uiState.collectAsState()
    val apps by resolvedViewModel.apps.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val activity = context as? Activity

    val hazeState = remember { HazeState() }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Высота нижней навигации — строго полоска
    val bottomBarHeight = 60.dp
    val bottomBarOuterPadding = dp8

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // важно: haze ставим на корневой контейнер, который рисует фон/контент
                .haze(hazeState)
        ) {
            // Контент (пейджер) рисуется "под" нижней панелью, чтобы blur было что блюрить
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> PixelWankerPage()
                    1 -> AppsPage(
                        uiState = appsUiState,
                        apps = apps,
                        onAppClick = onAppClick
                    )
                }
            }

            BottomSectionSwitcher(
                currentPage = pagerState.currentPage,
                onSelectPage = { page ->
                    scope.launch { pagerState.animateScrollToPage(page) }
                },
                onShowAbout = { showAboutDialog = true },
                onExit = { activity?.finish() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = dp0, vertical = bottomBarOuterPadding),
                barHeight = bottomBarHeight
            )
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(text = "About") },
            text = { Text(text = "Pavlov Alexey Sandbox") },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(text = "OK")
                }
            }
        )
    }
}

@Composable
private fun BottomSectionSwitcher(
    currentPage: Int,
    onSelectPage: (Int) -> Unit,
    onShowAbout: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    barHeight: Dp = 60.dp,
) {
    val hazeState = LocalHazeState.current
    val surfaceColor = MaterialTheme.colorScheme.surface

    val glassStyle = remember(surfaceColor) {
        HazeStyle(
            backgroundColor = surfaceColor,
            tint = HazeTint(surfaceColor.copy(alpha = 0.6f)),
            blurRadius = dp20,
            noiseFactor = 0.60f,
            fallbackTint = HazeTint(surfaceColor.copy(alpha = 0.72f))
        )
    }

    val selectedColor = MaterialTheme.colorScheme.onSurface
    val unselectedColor = selectedColor.copy(alpha = 0.70f)

    val items = listOf(
        BottomNavItem(
            title = "Сетка",
            selected = currentPage == 0,
            onClick = { onSelectPage(0) }
        ),
        BottomNavItem(
            title = "Apps",
            selected = currentPage == 1,
            onClick = { onSelectPage(1) }
        ),
        BottomNavItem(
            title = "About",
            selected = false,
            onClick = onShowAbout
        ),
        BottomNavItem(
            title = "Exit",
            selected = false,
            onClick = onExit
        )
    )

    val shape = RoundedCornerShape(topStart = dp16, topEnd = dp16)

    Box(
        modifier = modifier
            .height(barHeight)
            .clip(shape)
            .then(
                if (hazeState != null) {
                    Modifier.hazeEffect(state = hazeState, style = glassStyle)
                } else {
                    Modifier
                }
            )
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxSize(),
            windowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = surfaceColor.copy(alpha = 0.72f),
            tonalElevation = dp8
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
}

private data class BottomNavItem(
    val title: String,
    val selected: Boolean,
    val onClick: () -> Unit,
)
