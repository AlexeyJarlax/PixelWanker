package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist

/** Павлов Алексей https://github.com/AlexeyJarlax */

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.navigation.NavHostController
import androidx.navigation.NavBackStackEntry
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridSettingsStore
import com.pavlovalexey.pavlovAlexeySandbox.overlay.PixelWankerOverlayService
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.UiState
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.AlexIconButton
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.MatrixBackground
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.VSpacer
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.WankerConfirmationDialog
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.WankerProgress
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp16
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp8
import com.pavlovalexey.pavlovAlexeySandbox.utils.FirstLaunchDialogPrefs
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pavlovalexey.pavlovAlexeySandbox.repository.InstalledAppsRepositoryImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry,
    viewModel: AppDetailViewModel? = null,
) {
    val context = LocalContext.current
    val repository = remember(context) { InstalledAppsRepositoryImpl(context.applicationContext) }
    val factory = remember(backStackEntry, repository) {
        AppDetailViewModelFactory(repository, backStackEntry, backStackEntry.arguments)
    }
    val resolvedViewModel =
        viewModel ?: viewModel(viewModelStoreOwner = backStackEntry, factory = factory)
    val uiState by resolvedViewModel.uiState.collectAsState()
    val details by resolvedViewModel.details.collectAsState()
    var pendingGridPackage by remember { mutableStateOf<String?>(null) }
    var showFirstLaunchDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val gridSettings = GridSettingsStore.loadOrDefault(context)
    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val packageName = pendingGridPackage
        if (packageName != null && Settings.canDrawOverlays(context)) {
            PixelWankerOverlayService.start(
                context = context,
                cellValue = gridSettings.cellValue,
                unit = gridSettings.unit,
                baseColor = gridSettings.baseColor
            )
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                context.startActivity(intent)
            }
        }
        pendingGridPackage = null
    }

    fun runWithFirstLaunchDialog(action: () -> Unit) {
        if (FirstLaunchDialogPrefs.shouldShow(context)) {
            pendingAction = action
            showFirstLaunchDialog = true
        } else {
            action()
        }
    }

    fun openAppWithGrid(appPackageName: String) {
        if (Settings.canDrawOverlays(context)) {
            PixelWankerOverlayService.start(
                context = context,
                cellValue = gridSettings.cellValue,
                unit = gridSettings.unit,
                baseColor = gridSettings.baseColor
            )
            val intent = context.packageManager.getLaunchIntentForPackage(appPackageName)
            if (intent != null) {
                context.startActivity(intent)
            }
        } else {
            pendingGridPackage = appPackageName
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(details?.appName ?: "Информация о приложении") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(dp16)
        ) {
            MatrixBackground(100)
            when (uiState) {
                is UiState.Loading -> {
                    WankerProgress()
                }

                is UiState.Error -> {
                    Text(
                        text = (uiState as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UiState.Success -> {
                    details?.let { app ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(dp8)
                        ) {
                            Text("Название: ${app.appName}")
                            HorizontalDivider()
                            Text("Имя пакета: ${app.packageName}")
                            HorizontalDivider()
                            Text("versionName: ${app.versionName ?: "—"}")
                            HorizontalDivider()
                            Text("versionCode: ${app.versionCode ?: 0}")
                            HorizontalDivider()
                            app.apkSizeBytes?.let { bytes ->
                                val mb = bytes.toDouble() / (1024 * 1024)
                                Text("Размер APK: ${"%.2f".format(mb)} МБ")
                                HorizontalDivider()
                            }
                            VSpacer()
                            Text(text = "Контрольная сумма APK по SHA-256:")
                            Text(text = app.apkChecksumSha256 ?: "Не удалось посчитать")
                            VSpacer(24)

                            Row(horizontalArrangement = Arrangement.spacedBy(dp8)) {
                                AlexIconButton(
                                    onClick = {
                                        val intent =
                                            context.packageManager.getLaunchIntentForPackage(
                                                app.packageName
                                            )
                                        if (intent != null) {
                                            context.startActivity(intent)
                                        }
                                    },
                                    isFillMaxWidth = false,
                                    outlined = true,
                                    text = "Открыть\nприложение"
                                )

                                AlexIconButton(
                                    onClick = {
                                        runWithFirstLaunchDialog {
                                            openAppWithGrid(app.packageName)
                                        }
                                    },
                                    isFillMaxWidth = false,
                                    outlined = true,
                                    text = "Открыть\nс сеткой"
                                )
                            }
                        }
                    }
                }
            }
        }
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
