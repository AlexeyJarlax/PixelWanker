package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.start

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.pavlovalexey.pavlovAlexeySandbox.R
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridSettingsStore
import com.pavlovalexey.pavlovAlexeySandbox.overlay.GridUserSettings
import com.pavlovalexey.pavlovAlexeySandbox.overlay.PixelWankerOverlayService
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.AlexIconButton
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.Cookie
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.MatrixBackground
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.Pie
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.SpacerHeight
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.WankerConfirmationDialog
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp12
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp16
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp40
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp8
import com.pavlovalexey.pavlovAlexeySandbox.utils.FirstLaunchDialogPrefs
import java.util.Locale
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp156
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp24

/** Павлов Алексей https://github.com/AlexeyJarlax */

@Composable
fun PixelWankerPage() {
    val context = LocalContext.current
    val activity = context as? Activity
    val sizes = remember { listOf(12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 40, 60) }
    val saved = remember { GridSettingsStore.loadOrDefault(context) }
    var unit by remember { mutableStateOf(saved.unit) }
    var selectedSize by remember { mutableStateOf(saved.cellValue) }
    var baseColor by remember { mutableStateOf(saved.baseColor) }
    var extraColor by remember { mutableStateOf(saved.extraColor) } // ✅ NEW
    var sizeMenuExpanded by remember { mutableStateOf(false) }
    var pendingStart by remember { mutableStateOf(false) }
    var showFirstLaunchDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var isCookieVisible1 by remember { mutableStateOf(true) }
    var isCookieVisible2 by remember { mutableStateOf(true) }
    var isPieVisible1 by remember { mutableStateOf(true) }
    var isPieVisible2 by remember { mutableStateOf(true) }
    var showTelegramStarsDialog by remember { mutableStateOf(false) }
    val cloudtipsUrl = stringResource(R.string.cloudtips_url)
    val pleinairPlayUrl = stringResource(R.string.pleinair_play_url)
    val telegramChannelUrl = stringResource(R.string.telegram_channel_url)
    val telegramChannelTitle = stringResource(R.string.telegram_channel_title)

    val tipEligibleCountries = remember {
        setOf("RU", "BY", "TJ", "UZ", "TM", "KZ")
    }
    val showRussianTipsBlock =
        Locale.getDefault().country.uppercase(Locale.ROOT) in tipEligibleCountries

    fun saveNow() {
        GridSettingsStore.save(
            context = context,
            settings = GridUserSettings(
                cellValue = selectedSize,
                unit = unit,
                baseColor = baseColor,
                extraColor = extraColor
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
                baseColor = baseColor,
                extraColor = extraColor
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
                baseColor = baseColor,
                extraColor = extraColor
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

    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    Box(Modifier.fillMaxSize()) {
//        MatrixBackground(100)
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
                    colors = chipColorsFor(android.graphics.Color.WHITE),
                    label = { Text(stringResource(R.string.color_white)) }
                )
                FilterChip(
                    selected = baseColor == android.graphics.Color.GREEN,
                    onClick = {
                        baseColor = android.graphics.Color.GREEN
                        saveNow()
                    },
                    colors = chipColorsFor(android.graphics.Color.GREEN),
                    label = { Text(stringResource(R.string.color_green)) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(dp8)) {
                FilterChip(
                    selected = baseColor == android.graphics.Color.BLACK,
                    onClick = {
                        baseColor = android.graphics.Color.BLACK
                        saveNow()
                    },
                    colors = chipColorsFor(android.graphics.Color.BLACK),
                    label = { Text(stringResource(R.string.color_black)) }
                )
                FilterChip(
                    selected = baseColor == android.graphics.Color.RED,
                    onClick = {
                        baseColor = android.graphics.Color.RED
                        saveNow()
                    },
                    colors = chipColorsFor(android.graphics.Color.RED),
                    label = { Text(stringResource(R.string.color_red)) }
                )
            }

            SpacerHeight()

            AlexIconButton(
                text = stringResource(R.string.start_grid),
                isFillMaxWidth = false,
                outlined = true,
                onClick = { runWithFirstLaunchDialog { startOverlayOrRequestPermission() } },
            )
            SpacerHeight(60)

            if (isCookieVisible1) {
                Cookie(onClose = { isCookieVisible1 = false })
                SpacerHeight(60)
            }

//            if (isPieVisible1) {
//                Pie(onClose = { isPieVisible1 = false })
//                SpacerHeight(60)
//            }

            Text(
                text = stringResource(R.string.thanks_prompt),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            SpacerHeight(60)

            Text(
                text = stringResource(R.string.telegram_donations_title),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            SpacerHeight()

            Text(
                text = stringResource(R.string.telegram_donations_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            SpacerHeight(8)
            AlexIconButton(
                text = stringResource(R.string.telegram_open_channel_button),
                outlined = true,
                isFillMaxWidth = false,
                onClick = { showTelegramStarsDialog = true },
            )
            SpacerHeight(60)

            if (showRussianTipsBlock) {
                Text(
                    text = stringResource(R.string.tips_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                SpacerHeight(8)
                Box(
                    modifier = Modifier
                        .width(156.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray.copy(alpha = 0.5f))
                        .clickable { openUrl(cloudtipsUrl) }
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_icon_cloudtips_logo),
                        contentDescription = stringResource(R.string.tips_content_description),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    )
                }
                SpacerHeight(60)
            }

            Image(
                painter = painterResource(R.drawable.ic_google),
                modifier = Modifier
                    .size(dp40)
                    .clickable {
                        openUrl(pleinairPlayUrl)
                    },
                contentDescription = stringResource(R.string.google_play_content_description)
            )
            SpacerHeight(8)
            Text(
                text = stringResource(R.string.pleinair_install_prompt),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.CenterVertically)
            )
            Text(
                text = stringResource(R.string.pleinair_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            SpacerHeight(60)


            if (isCookieVisible2) {
                Text(
                    text = stringResource(R.string.cookie_reward_text),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Cookie(onClose = { isCookieVisible2 = false })
                SpacerHeight(60)
            }

            if (isPieVisible2) {
                Text(text = stringResource(R.string.pie_reward_text))
                Pie(onClose = { isPieVisible2 = false })
                SpacerHeight(60)
            }

            Text(
                text = stringResource(R.string.extra_color_unlock_text),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            SpacerHeight()

            FilterChip(
                selected = extraColor == null,
                onClick = {
                    extraColor = null
                    saveNow()
                },
                label = { Text(stringResource(R.string.no_second_color)) }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dp8)
            ) {
                FilterChip(
                    selected = extraColor == android.graphics.Color.WHITE,
                    onClick = {
                        extraColor = android.graphics.Color.WHITE
                        saveNow()
                    },
                    modifier = Modifier.weight(1f),
                    colors = chipColorsFor(android.graphics.Color.WHITE),
                    label = { Text(stringResource(R.string.color_white)) }
                )

                FilterChip(
                    selected = extraColor == android.graphics.Color.BLACK,
                    onClick = {
                        extraColor = android.graphics.Color.BLACK
                        saveNow()
                    },
                    modifier = Modifier.weight(1f),
                    colors = chipColorsFor(android.graphics.Color.BLACK),
                    label = { Text(stringResource(R.string.color_black)) }
                )

                FilterChip(
                    selected = extraColor == android.graphics.Color.RED,
                    onClick = {
                        extraColor = android.graphics.Color.RED
                        saveNow()
                    },
                    modifier = Modifier.weight(1f),
                    colors = chipColorsFor(android.graphics.Color.RED),
                    label = { Text(stringResource(R.string.color_red)) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dp8)
            ) {
                FilterChip(
                    selected = extraColor == android.graphics.Color.GREEN,
                    onClick = {
                        extraColor = android.graphics.Color.GREEN
                        saveNow()
                    },
                    modifier = Modifier.weight(1f),
                    colors = chipColorsFor(android.graphics.Color.GREEN),
                    label = { Text(stringResource(R.string.color_green)) }
                )
                FilterChip(
                    selected = extraColor == android.graphics.Color.YELLOW,
                    onClick = {
                        extraColor = android.graphics.Color.YELLOW
                        saveNow()
                    },
                    modifier = Modifier.weight(1f),
                    colors = chipColorsFor(android.graphics.Color.YELLOW),
                    label = { Text(stringResource(R.string.color_yellow)) }
                )

                FilterChip(
                    selected = extraColor == android.graphics.Color.BLUE,
                    onClick = {
                        extraColor = android.graphics.Color.BLUE
                        saveNow()
                    },
                    modifier = Modifier.weight(1f),
                    colors = chipColorsFor(android.graphics.Color.BLUE),
                    label = { Text(stringResource(R.string.color_blue)) }
                )
            }
            SpacerHeight()

            AlexIconButton(
                text = stringResource(R.string.start_grid),
                isFillMaxWidth = false,
                outlined = true,
                onClick = { runWithFirstLaunchDialog { startOverlayOrRequestPermission() } },
            )
            SpacerHeight(60)
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

        if (showTelegramStarsDialog) {
            WankerConfirmationDialog(
                title = stringResource(R.string.telegram_stars_dialog_title),
                dialogText = stringResource(
                    R.string.telegram_stars_dialog_text,
                    telegramChannelTitle
                ),
                confirmText = stringResource(R.string.telegram_stars_confirm),
                dismissText = stringResource(R.string.telegram_stars_dismiss),
                onDismiss = { showTelegramStarsDialog = false },
                onConfirm = {
                    showTelegramStarsDialog = false
                    openUrl(telegramChannelUrl)
                }
            )
        }
    }
}

@Composable
private fun chipColorsFor(colorInt: Int) = run {
    val base = Color(colorInt)
    val content = if (base.luminance() < 0.45f) Color.White else Color.Black

    FilterChipDefaults.filterChipColors(
        // когда НЕ выбрано
        containerColor = base.copy(alpha = 0.22f),
        labelColor = content,

        // когда ВЫБРАНО
        selectedContainerColor = base,
        selectedLabelColor = content
    )
}
