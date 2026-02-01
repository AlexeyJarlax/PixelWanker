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
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlovalexey.pavlovAlexeySandbox.model.InstalledApp
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.UiState
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist.InstalledAppsViewModel
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist.InstalledAppListItem
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeEffect

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StartScreen(
    onAppClick: (String) -> Unit,
    appsViewModel: InstalledAppsViewModel = hiltViewModel(),
) {
    val appsUiState by appsViewModel.uiState.collectAsState()
    val apps by appsViewModel.apps.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
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
private fun PixelWankerPage() {
    val context = LocalContext.current
    val activity = context as? Activity
    val sizes = remember { listOf(12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 40, 60) }
    val saved = remember {
        GridSettingsStore.loadOrDefault(context)
    }

    var unit by remember { mutableStateOf(saved.unit) }
    var selectedSize by remember { mutableStateOf(saved.cellValue) }
    var baseColor by remember { mutableStateOf(saved.baseColor) }
    var sizeMenuExpanded by remember { mutableStateOf(false) }
    var pendingStart by remember { mutableStateOf(false) }
    var showFirstLaunchDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun saveNow() {
        GridSettingsStore.save(
            context = context,
            settings = GridUserSettings(
                cellValue = selectedSize,
                unit = unit,
                baseColor = baseColor
            )
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (pendingStart && Settings.canDrawOverlays(context)) {
            PixelWankerOverlayService.start(
                context = context,
                cellValue = selectedSize,
                unit = unit,
                baseColor = baseColor
            )
            activity?.finish()
            pendingStart = false
        }
    }

    fun runWithFirstLaunchDialog(action: () -> Unit) {
        if (FirstLaunchDialogPrefs.shouldShow(context)) {
            pendingAction = action
            showFirstLaunchDialog = true
        } else {
            action()
        }
    }

    fun startOverlayOrRequestPermission() {
        saveNow()
        if (Settings.canDrawOverlays(context)) {
            PixelWankerOverlayService.start(
                context = context,
                cellValue = selectedSize,
                unit = unit,
                baseColor = baseColor
            )
            activity?.finish()
        } else {
            pendingStart = true
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            permissionLauncher.launch(intent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dp16)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PixelWanker",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        SpacerHeight()

        Text(
            text = "Don’t be a wanker — stop guessing,\nstart measuring",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        SpacerHeight()
        Text(
            text = "Pixel wanker for Android dev is a utility for Android developers and designers who want a fast way to check UI spacing, alignment, and visual rhythm directly on the device. The app generates a customizable on-screen grid and lets you overlay it on top of any app",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )




        Spacer(modifier = Modifier.height(dp16))

        Row(horizontalArrangement = Arrangement.spacedBy(dp8)) {
            FilterChip(
                selected = unit == "px",
                onClick = {
                    unit = "px"
                    saveNow()
                },
                label = { Text("px") }
            )
            FilterChip(
                selected = unit == "dp",
                onClick = {
                    unit = "dp"
                    saveNow()
                },
                label = { Text("dp") }
            )
        }

        SpacerHeight()

        Box {
            AlexIconButton(
                text = "Размер: $selectedSize $unit",
                isFillMaxWidth = false,
                outlined = true,
                onClick = { sizeMenuExpanded = true },
            )
            SpacerHeight()
            DropdownMenu(
                expanded = sizeMenuExpanded,
                onDismissRequest = { sizeMenuExpanded = false }
            ) {
                sizes.forEach { v ->
                    DropdownMenuItem(
                        text = { Text("$v $unit") },
                        onClick = {
                            selectedSize = v
                            sizeMenuExpanded = false
                            saveNow()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(dp12))

        Row(horizontalArrangement = Arrangement.spacedBy(dp8)) {
            FilterChip(
                selected = baseColor == android.graphics.Color.WHITE,
                onClick = {
                    baseColor = android.graphics.Color.WHITE
                    saveNow()
                },
                label = { Text("Белый") }
            )
            FilterChip(
                selected = baseColor == android.graphics.Color.BLACK,
                onClick = {
                    baseColor = android.graphics.Color.BLACK
                    saveNow()
                },
                label = { Text("Черный") }
            )
            FilterChip(
                selected = baseColor == android.graphics.Color.RED,
                onClick = {
                    baseColor = android.graphics.Color.RED
                    saveNow()
                },
                label = { Text("Красный") }
            )
        }

        Spacer(modifier = Modifier.height(dp16))

        AlexIconButton(
            text = "Запустить сетку",
            outlined = true,
            onClick = {
                runWithFirstLaunchDialog { startOverlayOrRequestPermission() }
            },
        )
    }

    if (showFirstLaunchDialog) {
        WankerConfirmationDialog(
            dialogText = FirstLaunchDialogPrefs.DIALOG_TEXT,
            onDismiss = {
                showFirstLaunchDialog = false
                pendingAction = null
            },
            onConfirm = {
                FirstLaunchDialogPrefs.markShown(context)
                showFirstLaunchDialog = false
                pendingAction?.invoke()
                pendingAction = null
            }
        )
    }
}

@Composable
private fun AppsPage(
    uiState: UiState,
    apps: List<InstalledApp>,
    onAppClick: (String) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        MatrixBackground(100)
        when (uiState) {
            is UiState.Loading -> {
                WankerProgress()
            }

            is UiState.Error -> {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is UiState.Success -> {
                var searchAppsQuery by remember { mutableStateOf("") }

                val filtered = remember(apps, searchAppsQuery) {
                    val q = searchAppsQuery.trim()
                    if (q.isEmpty()) apps
                    else apps.filter { app ->
                        app.appName.contains(q, ignoreCase = true) ||
                                app.packageName.contains(q, ignoreCase = true)
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    VSpacer(24)
                    AlexSearchTextField(
                        value = searchAppsQuery,
                        onValueChange = { searchAppsQuery = it },
                        placeholderText = "Поиск приложений"
                    )

                    LazyColumn(
                        contentPadding = PaddingValues(vertical = dp8)
                    ) {
                        items(filtered) { app ->
                            InstalledAppListItem(
                                app = app,
                                onClick = { onAppClick(app.packageName) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
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
            fallbackTint = HazeTint(surfaceColor.copy(alpha = 0.90f))
        )
    }
    val selectedColor = MaterialTheme.colorScheme.onSurface
    val unselectedColor = selectedColor.copy(alpha = 0.72f)
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

    Box(
        modifier = modifier
            .heightIn(min = dp48)
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = dp48)
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
                surfaceColor.copy(alpha = 0.92f)
            },
            tonalElevation = dp0
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    selected = item.selected,
                    onClick = item.onClick,
                    icon = {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedColor,
                        unselectedIconColor = unselectedColor,
                        indicatorColor = Color.Transparent
                    )
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
