package com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components

/** Павлов Алексей https://github.com/AlexeyJarlax */

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp8

@Composable
fun HSpacer(size: Int = 16) {
    Spacer(Modifier.width(size.dp))
}

@Composable
fun VSpacer(size: Int = 16) {
    Spacer(Modifier.height(size.dp))
}

@Composable
fun Pie(onClose: () -> Unit) {
    Spacer(modifier = Modifier.height(dp8))
    Text(
        text = "\uD83E\uDD67",
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClose() }
    )
}

@Composable
fun Cookie(onClose: () -> Unit) {
    Spacer(modifier = Modifier.height(dp8))
    Text(
        text = "\uD83C\uDF6A",
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClose() }
    )
}






