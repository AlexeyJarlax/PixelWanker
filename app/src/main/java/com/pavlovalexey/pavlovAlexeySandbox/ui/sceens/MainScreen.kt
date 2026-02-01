package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.pavlovalexey.pavlovAlexeySandbox.navigation.NavGraph
import androidx.navigation.NavHostController
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.WankerConfirmationDialog

private const val FIRST_LAUNCH_PREFS = "pixel_wanker_first_launch_prefs"
private const val KEY_FIRST_LAUNCH_DIALOG_SHOWN = "first_launch_dialog_shown"
private const val FIRST_LAUNCH_DIALOG_TEXT =
    "После первого запуска система спросит о выдаче разрешения Поверх других приложений. " +
    "Найдите в списке PixelWanker и выставите тумблер в активный режим"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    onCloseApp: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(FIRST_LAUNCH_PREFS, Context.MODE_PRIVATE)
    }
    var showFirstLaunchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!prefs.getBoolean(KEY_FIRST_LAUNCH_DIALOG_SHOWN, false)) {
            showFirstLaunchDialog = true
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.statusBars)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {},
        ) { innerPadding ->
            NavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    if (showFirstLaunchDialog) {
        WankerConfirmationDialog(
            dialogText = FIRST_LAUNCH_DIALOG_TEXT,
            onDismiss = {
                prefs.edit().putBoolean(KEY_FIRST_LAUNCH_DIALOG_SHOWN, true).apply()
                showFirstLaunchDialog = false
            },
            onConfirm = {
                prefs.edit().putBoolean(KEY_FIRST_LAUNCH_DIALOG_SHOWN, true).apply()
                showFirstLaunchDialog = false
            }
        )
    }
}
