package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.start

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import android.app.Activity
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
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.AlexIconButton
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.AlexSearchTextField
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.MatrixBackground
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.VSpacer
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp12
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp16
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp40
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp8
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridSettingsStore
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridUserSettings
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.WankerProgress
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp0

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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dp0, vertical = dp8)
            )
        }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dp16),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PixelWanker",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(dp8))

        Text(
            text = "Don’t be a wanker — stop guessing, start measuring",
            style = MaterialTheme.typography.bodyMedium
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

        Spacer(modifier = Modifier.height(dp12))

        Box {
            Button(onClick = { sizeMenuExpanded = true }) {
                Text(text = "Размер: $selectedSize $unit")
            }
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
            },
        )

        Spacer(modifier = Modifier.height(dp8))
        Text(
            text = "После первого запуска система спросит о выдаче разрешения Поверх других приложений. Найдите в списке PixelWanker и выставите тумблер в активный режим",
            style = MaterialTheme.typography.bodySmall
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
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dp8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlexIconButton(
            onClick = { onSelectPage(0) },
            modifier = Modifier
                .weight(1f)
                .height(dp40),
            text = "Сетка"
        )

        AlexIconButton(
            onClick = { onSelectPage(1) },
            modifier = Modifier
                .weight(1f)
                .height(dp40),
            text = "Приложения"
        )
    }
}
