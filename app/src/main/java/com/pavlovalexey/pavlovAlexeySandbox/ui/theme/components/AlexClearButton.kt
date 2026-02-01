package com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.pavlovalexey.pavlovAlexeySandbox.R
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp0
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp12
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp16

@Composable
fun AlexClearButton(
    onClear: () -> Unit,
    verticalOffset: Dp = dp0
) {
    IconButton(
        onClick = { onClear() },
        modifier = Modifier
            .padding(start = dp0, end = dp12)
            .offset(y = verticalOffset)
            .size(dp16)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.clear),
            modifier = Modifier.Companion.size(dp16)
        )
    }
}
