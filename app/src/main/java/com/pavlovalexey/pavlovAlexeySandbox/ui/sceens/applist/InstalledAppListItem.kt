package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist

/** Павлов Алексей https://github.com/AlexeyJarlax */

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pavlovalexey.pavlovAlexeySandbox.model.InstalledApp
import com.pavlovalexey.pavlovAlexeySandbox.R
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp12
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp16

@Composable
fun InstalledAppListItem(
    app: InstalledApp,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = dp16, vertical = dp12)
    ) {
        Text(text = app.appName, style = MaterialTheme.typography.titleMedium)
        Text(
            text = app.packageName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        app.versionName?.let {
            Text(
                text = stringResource(R.string.installed_app_version, it),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
