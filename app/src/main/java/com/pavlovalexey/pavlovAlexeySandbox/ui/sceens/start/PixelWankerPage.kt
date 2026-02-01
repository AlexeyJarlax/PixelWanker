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
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridSettingsStore
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridUserSettings
import com.pavlovalexey.pavlovAlexeySandbox.overlay.PixelWankerOverlayService
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.AlexIconButton
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.MatrixBackground
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.SpacerHeight
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.WankerConfirmationDialog
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp12
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp16
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp8
import com.pavlovalexey.pavlovAlexeySandbox.utils.FirstLaunchDialogPrefs
import com.pavlovalexey.pavlovAlexeySandbox.R
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.Cookie
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.Pie
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp60

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
    var isCookieVisible1 by remember { mutableStateOf(true) }
    var isCookieVisible2 by remember { mutableStateOf(true) }
    var isPieVisible1 by remember { mutableStateOf(true) }
    var isPieVisible2 by remember { mutableStateOf(true) }

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

    Box(Modifier.fillMaxSize()) {
        MatrixBackground(100)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dp16)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            SpacerHeight()

            Text(
                text = stringResource(R.string.pixelwanker_tagline),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            SpacerHeight()
            Text(
                text = stringResource(R.string.pixelwanker_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )




            Spacer(modifier = Modifier.height(dp16))

            val unitLabel = stringResource(
                if (unit == "px") R.string.unit_px else R.string.unit_dp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(dp8)) {
                FilterChip(
                    selected = unit == "px",
                    onClick = {
                        unit = "px"
                        saveNow()
                    },
                    label = { Text(stringResource(R.string.unit_px)) }
                )
                FilterChip(
                    selected = unit == "dp",
                    onClick = {
                        unit = "dp"
                        saveNow()
                    },
                    label = { Text(stringResource(R.string.unit_dp)) }
                )
            }

            SpacerHeight()

            Box {
                AlexIconButton(
                    text = stringResource(R.string.size_label, selectedSize, unitLabel),
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
                            text = { Text(stringResource(R.string.size_option, v, unitLabel)) },
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
                    label = { Text(stringResource(R.string.color_white)) }
                )
                FilterChip(
                    selected = baseColor == android.graphics.Color.BLACK,
                    onClick = {
                        baseColor = android.graphics.Color.BLACK
                        saveNow()
                    },
                    label = { Text(stringResource(R.string.color_black)) }
                )
                FilterChip(
                    selected = baseColor == android.graphics.Color.RED,
                    onClick = {
                        baseColor = android.graphics.Color.RED
                        saveNow()
                    },
                    label = { Text(stringResource(R.string.color_red)) }
                )
            }

            SpacerHeight()

            AlexIconButton(
                text = stringResource(R.string.start_grid),
                outlined = true,
                onClick = {
                    runWithFirstLaunchDialog { startOverlayOrRequestPermission() }
                },
            )
            SpacerHeight(60)

            if (isCookieVisible1) {
                Cookie(onClose = { isCookieVisible1 = false })
            }
            SpacerHeight(60)

            if (isPieVisible1) {
                Pie(onClose = { isPieVisible1 = false })
            }
            Text(text = "______________")
            SpacerHeight(60)

            Text(
                text = "Если вы хотите отблагодарить автора приложения, можете сделать это следующим способом:",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(dp12))

            Text(
                text = "Установить и зарегистрироваться в моем приложении для художников. " +
                    "PleinAir - это прекрасный проект, который я всеми силами хочу развить во что-то больше и еще более прекрасное! " +
                    "https://play.google.com/store/apps/details?id=com.pavlovalexey.pleinair_kmp&pcampaignid=web_share",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(dp12))

            Text(
                text = "Зайти в мой профиль и залайкать PixelWanker или PleinAir " +
                    "https://play.google.com/store/apps/dev?id=8406991842366944145&pli=1",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(dp12))

            Text(
                text = "Если вы это сделали, то возьмите печеньку!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (isCookieVisible1) {
                Cookie(onClose = { isCookieVisible1 = false })
            }
            SpacerHeight(60)

            Text(text = "И пирожок с полки!")
            if (isPieVisible1) {
                Pie(onClose = { isPieVisible1 = false })
            }
            Text(text = "______________")
            SpacerHeight(60)

            Text(
                text = "Кажется вы дошли до самого конца... и разблокировали дополнительный цвет для сетки! " +
                    "Да, с двумя цветами на контрасте она будет заметнее в сложных дизайнах.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            SpacerHeight()

            Row(horizontalArrangement = Arrangement.spacedBy(dp8)) {
                FilterChip(
                    selected = extraColor == android.graphics.Color.BLUE,
                    onClick = {},
                    label = {  }
                )
                FilterChip(
                    selected = extraColor == android.graphics.Color.WHITE,
                    onClick = {},
                    label = { Text(stringResource(R.string.color_white)) }
                )
                FilterChip(
                    selected = extraColor == android.graphics.Color.RED,
                    onClick = {},
                    label = { Text(stringResource(R.string.color_red)) }
                )
                FilterChip(
                    selected = extraColor == android.graphics.Color.BLACK,
                    onClick = {},
                    label = { Text(stringResource(R.string.color_black)) }
                )
                FilterChip(
                    selected = extraColor == android.graphics.Color.YELLOW,
                    onClick = {},
                    label = {  }
                )
                FilterChip(
                    selected = extraColor == null,
                    onClick = {},
                    label = { }
                )
            }
        }

        if (showFirstLaunchDialog) {
            WankerConfirmationDialog(
                dialogText = stringResource(R.string.first_launch_dialog_text),
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
}
