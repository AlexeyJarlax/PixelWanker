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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pavlovalexey.pavlovAlexeySandbox.overlay.PixelWankerOverlayService
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.UiState
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.AlexIconButton
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.MatrixBackground
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.VSpacer
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp16
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp8

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    navController: NavHostController,
    viewModel: AppDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val details by viewModel.details.collectAsState()
    val context = LocalContext.current
    var pendingGridPackage by remember { mutableStateOf<String?>(null) }
    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val packageName = pendingGridPackage
        if (packageName != null && Settings.canDrawOverlays(context)) {
            PixelWankerOverlayService.start(context)
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                context.startActivity(intent)
            }
        }
        pendingGridPackage = null
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
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
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
                                    text = "Открыть приложение"
                                )

                                AlexIconButton(
                                    onClick = {
                                        if (Settings.canDrawOverlays(context)) {
                                            PixelWankerOverlayService.start(context)
                                            val intent =
                                                context.packageManager.getLaunchIntentForPackage(
                                                    app.packageName
                                                )
                                            if (intent != null) {
                                                context.startActivity(intent)
                                            }
                                        } else {
                                            pendingGridPackage = app.packageName
                                            val intent = Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:${context.packageName}")
                                            )
                                            overlayPermissionLauncher.launch(intent)
                                        }
                                    },
                                    isFillMaxWidth = false,
                                    outlined = true,
                                    text = "Открыть с сеткой"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
