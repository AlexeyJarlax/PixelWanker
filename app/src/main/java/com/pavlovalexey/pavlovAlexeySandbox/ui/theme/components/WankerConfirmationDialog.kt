package com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp14
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp18
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp2

@Composable
fun WankerConfirmationDialog(
    dialogText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String? = null,
    confirmText: String = "Вперёд!",
    dismissText: String = "Назад",
    confirmEnabled: Boolean = true,
    dismissEnabled: Boolean = true,
    showDismissIcon: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    titleContent: (@Composable () -> Unit)? = null,
    textContent: (@Composable () -> Unit)? = null,
    actionsContent: (@Composable () -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(
                    width = dp2,
                    color = color,
                    shape = RoundedCornerShape(dp14)
                ),
            shape = RoundedCornerShape(dp14),
//            elevation = CardDefaults.cardElevation(defaultElevation = dp8),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Column(
                modifier = Modifier.padding(dp18),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                titleContent?.invoke() ?: title?.let {
                    Text(text = it)
                    SpacerHeight()
                }

                textContent?.invoke() ?: Text(
                    text = dialogText,
                    modifier = Modifier.padding(bottom = dp18)
                )

                if (actionsContent != null) {
                    actionsContent()
                } else {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = onDismiss, enabled = dismissEnabled) {
                            if (showDismissIcon) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = dismissText,
                                    tint = color
                                )
                            }
                            Text(text = dismissText, color = color)
                        }

                        SpacerHeight()

                        TextButton(onClick = onConfirm, enabled = confirmEnabled) {
                            Text(text = confirmText, color = color)
                        }
                    }
                }
            }
        }
    }
}
