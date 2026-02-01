package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.start

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pavlovalexey.pavlovAlexeySandbox.model.InstalledApp
import com.pavlovalexey.pavlovAlexeySandbox.R
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.UiState
import com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist.InstalledAppListItem
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.AlexSearchTextField
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.MatrixBackground
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.VSpacer
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.WankerProgress
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp8

/** Павлов Алексей https://github.com/AlexeyJarlax */

@Composable
fun AppsPage(
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
                    text = stringResource(uiState.messageResId),
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
                        placeholderText = stringResource(R.string.search_apps_placeholder)
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
