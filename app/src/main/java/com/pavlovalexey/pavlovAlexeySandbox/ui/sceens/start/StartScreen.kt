package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.start

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pavlovalexey.pavlovAlexeySandbox.navigation.StartBottomBar
import com.pavlovalexey.pavlovAlexeySandbox.repository.InstalledAppsRepositoryImpl
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist.InstalledAppsViewModel
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist.InstalledAppsViewModelFactory
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.LocalHazeState
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp0
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp8
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch
import com.pavlovalexey.pavlovAlexeySandbox.R
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.WankerConfirmationDialog
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StartScreen(
    onAppClick: (String) -> Unit,
    appsViewModel: InstalledAppsViewModel? = null,
) {
    val context = LocalContext.current
    val repository = remember(context) { InstalledAppsRepositoryImpl(context.applicationContext) }
    val factory = remember(repository) { InstalledAppsViewModelFactory(repository) }
    val resolvedViewModel = appsViewModel ?: viewModel(factory = factory)
    val appsUiState by resolvedViewModel.uiState.collectAsState()
    val apps by resolvedViewModel.apps.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val activity = context as? Activity
    val hazeState = remember { HazeState() }
    var showAboutDialog by remember { mutableStateOf(false) }
    val bottomBarHeight = 60.dp
    val bottomBarOuterPadding = dp8

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
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

            StartBottomBar(
                currentPage = pagerState.currentPage,
                onSelectPage = { page ->
                    scope.launch { pagerState.animateScrollToPage(page) }
                },
                onShowAbout = { showAboutDialog = true },
                onExit = { activity?.finish() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = dp0, vertical = bottomBarOuterPadding),
                barHeight = bottomBarHeight
            )
        }
    }

    if (showAboutDialog) {
        WankerConfirmationDialog(
            onDismiss = { showAboutDialog = false },
            dialogText = stringResource(R.string.about_app_text),
            onConfirm = { showAboutDialog = false },
            confirmText = "No more wanking",
            dismissText = "Back to wanking",
        )
    }
}
