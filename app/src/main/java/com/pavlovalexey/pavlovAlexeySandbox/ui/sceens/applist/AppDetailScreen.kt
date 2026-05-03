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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.graphics.Color
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
import com.pavlovalexey.pavlovAlexeySandbox.R
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
    val screenState by resolvedViewModel.screenState.collectAsState()
    var pendingGridPackage by remember { mutableStateOf<String?>(null) }
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
                baseColor = gridSettings.baseColor,
                extraColor = gridSettings.extraColor
            )
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                context.startActivity(intent)
            }
        }
        pendingGridPackage = null
    }

    fun openAppWithGrid(appPackageName: String) {
        if (Settings.canDrawOverlays(context)) {
            PixelWankerOverlayService.start(
                context = context,
                cellValue = gridSettings.cellValue,
                unit = gridSettings.unit,
                baseColor = gridSettings.baseColor,
                extraColor = gridSettings.extraColor
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

    LaunchedEffect(resolvedViewModel) {
        resolvedViewModel.effects.collect { effect ->
            when (effect) {
                is AppDetailEffect.OpenApp -> {
                    context.packageManager.getLaunchIntentForPackage(effect.packageName)
                        ?.let(context::startActivity)
                }

                is AppDetailEffect.OpenWithGrid -> openAppWithGrid(effect.packageName)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(details?.appName ?: stringResource(R.string.app_detail_title_default))
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(dp16)
        ) {
            MatrixBackground(40)
            when (uiState) {
                is UiState.Loading -> {
                    WankerProgress()
                }

                is UiState.Error -> {
                    Text(
                        text = stringResource((uiState as UiState.Error).messageResId),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UiState.Success -> {
                    details?.let { app ->
                        val versionName = app.versionName
                            ?: stringResource(R.string.app_detail_not_available)
                        val versionCode = app.versionCode ?: 0L
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(dp8)
                        ) {
                            Text(
                                stringResource(
                                    R.string.app_detail_label_name,
                                    app.appName
                                )
                            )
                            HorizontalDivider()
                            Text(
                                stringResource(
                                    R.string.app_detail_label_package,
                                    app.packageName
                                )
                            )
                            HorizontalDivider()
                            Text(
                                stringResource(
                                    R.string.app_detail_label_version_name,
                                    versionName
                                )
                            )
                            HorizontalDivider()
                            Text(
                                stringResource(
                                    R.string.app_detail_label_version_code,
                                    versionCode
                                )
                            )
                            HorizontalDivider()
                            app.apkSizeBytes?.let { bytes ->
                                val mb = formatApkSizeInMb(bytes)
                                Text(stringResource(R.string.app_detail_apk_size, mb))
                                HorizontalDivider()
                            }
                            VSpacer()
                            Text(text = stringResource(R.string.app_detail_checksum_label))
                            Text(
                                text = app.apkChecksumSha256
                                    ?: stringResource(R.string.app_detail_checksum_unavailable)
                            )
                            VSpacer(24)

                            Row(horizontalArrangement = Arrangement.spacedBy(dp8)) {
                                AlexIconButton(
                                    onClick = resolvedViewModel::onOpenAppClick,
                                    outlined = true,
                                    text = stringResource(R.string.app_detail_open_app)
                                )

                                AlexIconButton(
                                    onClick = {
                                        resolvedViewModel.onOpenWithGridClick(
                                            shouldShowFirstLaunchDialog = FirstLaunchDialogPrefs.shouldShow(context)
                                        )
                                    },
                                    outlined = true,
                                    text = stringResource(R.string.app_detail_open_with_grid)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (screenState.showFirstLaunchDialog) {
        WankerConfirmationDialog(
            dialogText = stringResource(R.string.first_launch_dialog_text),
            onDismiss = {
                resolvedViewModel.onFirstLaunchDialogDismiss()
            },
            onConfirm = {
                FirstLaunchDialogPrefs.markShown(context)
                resolvedViewModel.onFirstLaunchDialogConfirm()
            }
        )
    }
}
