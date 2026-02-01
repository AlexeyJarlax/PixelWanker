package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.start

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import android.app.Activity
import com.pavlovalexey.pavlovAlexeySandbox.overlay.PixelWankerOverlayService
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlovalexey.pavlovAlexeySandbox.model.InstalledApp
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.UiState
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist.InstalledAppsViewModel
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist.InstalledAppListItem
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.AlexIconButton
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.AlexSearchTextField
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.MatrixBackground
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.VSpacer
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp16
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp40
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp8

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StartScreen(
    onAppClick: (String) -> Unit,
    appsViewModel: InstalledAppsViewModel = hiltViewModel()
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
                    .padding(horizontal = dp16, vertical = dp8)
            )
        }
    }
}

@Composable
private fun PixelWankerPage() {
    val context = LocalContext.current
    val activity = context as? Activity
    var pendingStart by remember { mutableStateOf(false) }
    val availableSizes = remember { listOf(12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 40, 60) }
    var selectedSize by remember { mutableStateOf(PixelWankerOverlayService.DEFAULT_GRID_SIZE) }
    var selectedUnit by remember { mutableStateOf(PixelWankerOverlayService.GridUnit.PX) }
    val colorOptions = remember {
        listOf(
            GridColorOption("Черный", Color.Black, PixelWankerOverlayService.GridColor.BLACK),
            GridColorOption("Белый", Color.White, PixelWankerOverlayService.GridColor.WHITE),
            GridColorOption("Красный", Color.Red, PixelWankerOverlayService.GridColor.RED)
        )
    }
    var selectedColor by remember { mutableStateOf(colorOptions[1]) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (pendingStart && Settings.canDrawOverlays(context)) {
            PixelWankerOverlayService.start(
                context = context,
                gridSize = selectedSize,
                gridUnit = selectedUnit,
                gridColor = selectedColor.value
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
            text = "Сетка (PixelWanker)",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(dp8))
        Text(
            text = "Запускает поверх других приложений сетку с выбранным шагом и цветом. " +
                "В центре появляется кнопка с крестиком для закрытия.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(dp16))
        Text(
            text = "Параметры сетки",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(dp8))
        UnitSelector(
            selectedUnit = selectedUnit,
            onUnitSelected = { selectedUnit = it }
        )
        Spacer(modifier = Modifier.height(dp8))
        SizeSelector(
            label = "Размер ячейки",
            selectedValue = selectedSize,
            options = availableSizes,
            unitLabel = if (selectedUnit == PixelWankerOverlayService.GridUnit.PX) "px" else "dp",
            onSelected = { selectedSize = it }
        )
        Spacer(modifier = Modifier.height(dp8))
        ColorSelector(
            options = colorOptions,
            selectedOption = selectedColor,
            onSelected = { selectedColor = it }
        )
        Spacer(modifier = Modifier.height(dp16))
        Button(
            onClick = {
                if (Settings.canDrawOverlays(context)) {
                    PixelWankerOverlayService.start(
                        context = context,
                        gridSize = selectedSize,
                        gridUnit = selectedUnit,
                        gridColor = selectedColor.value
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
        ) {
            Text(text = "Показать сетку поверх всего")
        }
        Spacer(modifier = Modifier.height(dp8))
        Text(
            text = "После запуска приложение свернётся, а сетка останется поверх других экранов.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun UnitSelector(
    selectedUnit: PixelWankerOverlayService.GridUnit,
    onUnitSelected: (PixelWankerOverlayService.GridUnit) -> Unit
) {
    Text(text = "Единицы измерения")
    Spacer(modifier = Modifier.height(dp8))
    Row(horizontalArrangement = Arrangement.spacedBy(dp8)) {
        UnitButton(
            label = "px",
            isSelected = selectedUnit == PixelWankerOverlayService.GridUnit.PX,
            onClick = { onUnitSelected(PixelWankerOverlayService.GridUnit.PX) }
        )
        UnitButton(
            label = "dp",
            isSelected = selectedUnit == PixelWankerOverlayService.GridUnit.DP,
            onClick = { onUnitSelected(PixelWankerOverlayService.GridUnit.DP) }
        )
    }
}

@Composable
private fun UnitButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    if (isSelected) {
        Button(onClick = onClick) {
            Text(text = label)
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Text(text = label)
        }
    }
}

@Composable
private fun SizeSelector(
    label: String,
    selectedValue: Int,
    options: List<Int>,
    unitLabel: String,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Text(text = label)
    Spacer(modifier = Modifier.height(dp8))
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(text = "$selectedValue $unitLabel")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = "$option $unitLabel") },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ColorSelector(
    options: List<GridColorOption>,
    selectedOption: GridColorOption,
    onSelected: (GridColorOption) -> Unit
) {
    Text(text = "Цвет линий")
    Spacer(modifier = Modifier.height(dp8))
    Row(horizontalArrangement = Arrangement.spacedBy(dp8)) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            val buttonColors = if (isSelected) {
                ButtonDefaults.buttonColors(containerColor = option.previewColor)
            } else {
                ButtonDefaults.outlinedButtonColors()
            }
            val contentColor = if (option.previewColor == Color.White) {
                Color.Black
            } else {
                Color.White
            }
            if (isSelected) {
                Button(
                    onClick = { onSelected(option) },
                    colors = buttonColors
                ) {
                    Text(text = option.label, color = contentColor)
                }
            } else {
                OutlinedButton(
                    onClick = { onSelected(option) },
                    colors = buttonColors
                ) {
                    Text(text = option.label)
                }
            }
        }
    }
}

private data class GridColorOption(
    val label: String,
    val previewColor: Color,
    val value: PixelWankerOverlayService.GridColor
)

@Composable
private fun AppsPage(
    uiState: UiState,
    apps: List<InstalledApp>,
    onAppClick: (String) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        MatrixBackground(100)
        when (uiState) {
            is UiState.Loading -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
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
    modifier: Modifier = Modifier
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
