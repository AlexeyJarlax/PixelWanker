package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.start

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import android.app.Activity
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.pavlovalexey.pavlovAlexeySandbox.overlay.PixelWankerOverlayService
import com.pavlovalexey.pavlovAlexeySandbox.model.InstalledApp
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.UiState
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist.InstalledAppsViewModel
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist.InstalledAppListItem
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist.InstalledAppsViewModelFactory
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.AlexIconButton
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.AlexSearchTextField
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.MatrixBackground
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.VSpacer
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp12
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp16
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp20
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp40
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp48
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp8
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridSettingsStore
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridUserSettings
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.SpacerHeight
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.WankerProgress
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp0
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.WankerConfirmationDialog
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.LocalHazeState
import com.pavlovalexey.pavlovAlexeySandbox.utils.FirstLaunchDialogPrefs
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pavlovalexey.pavlovAlexeySandbox.repository.InstalledAppsRepositoryImpl
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp56

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

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(hazeState)
        ) {
            Scaffold { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
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
                            scope.launch {
                                pagerState.animateScrollToPage(page)
                            }
                        },
                        onShowAbout = { showAboutDialog = true },
                        onExit = { activity?.finish() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dp0, vertical = dp8)
                    )
                }
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(text = "About") },
            text = { Text(text = "Pavlov Alexey Sandbox") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showAboutDialog = false }) {
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
) {
    val hazeState = LocalHazeState.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val glassStyle = remember(surfaceColor) {
        HazeStyle(
            backgroundColor = Color.Transparent,
            tint = HazeTint(surfaceColor.copy(alpha = 0.32f)),
            blurRadius = dp20,
            noiseFactor = 0.10f,
            fallbackTint = HazeTint(surfaceColor.copy(alpha = 0.72f))
        )
    }
    val selectedColor = MaterialTheme.colorScheme.onSurface
    val unselectedColor = selectedColor.copy(alpha = 0.88f)
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

    Box(modifier = modifier) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = dp56)
                .then(
                    if (hazeState != null) {
                        Modifier.hazeEffect(state = hazeState, style = glassStyle)
                    } else {
                        Modifier
                    }
                ),
            containerColor = if (hazeState != null) {
                Color.Transparent
            } else {
                surfaceColor.copy(alpha = 0.72f)
            },
            tonalElevation = dp0
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    selected = item.selected,
                    onClick = item.onClick,
                    icon = {
                        Spacer(modifier = Modifier.size(dp0))
                    },
                    label = {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center
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
