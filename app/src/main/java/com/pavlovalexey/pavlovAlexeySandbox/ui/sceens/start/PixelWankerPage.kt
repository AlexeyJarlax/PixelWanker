package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.start

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridSettingsStore
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridUserSettings
import com.pavlovalexey.pavlovAlexeySandbox.overlay.PixelWankerOverlayService
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.AlexIconButton
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.SpacerHeight
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.WankerConfirmationDialog
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp12
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp16
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp8
import com.pavlovalexey.pavlovAlexeySandbox.utils.FirstLaunchDialogPrefs

/** Павлов Алексей https://github.com/AlexeyJarlax */

@Composable
fun PixelWankerPage() {
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