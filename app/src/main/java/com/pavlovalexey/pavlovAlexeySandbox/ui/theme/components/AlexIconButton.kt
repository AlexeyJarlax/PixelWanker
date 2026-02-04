package com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components

import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp0
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp1
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp48
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp6
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp8

@Composable
fun AlexIconButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    painter: Painter? = null,
    buttonColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    isFillMaxWidth: Boolean = false,
    outlined: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val baseModifier = modifier
        .heightIn(min = dp48)
        .background(buttonColor)

    val buttonModifier = if (isFillMaxWidth) {
        baseModifier.fillMaxWidth()
    } else {
        baseModifier
    }

    val contentPadding = if (isFillMaxWidth) {
        PaddingValues(dp0)
    } else {
        ButtonDefaults.ContentPadding
    }

    val hasIcon = icon != null || painter != null

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = buttonModifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(dp6),
        border = if (outlined) BorderStroke(dp1, contentColor) else null,
        contentPadding = contentPadding
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when {
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor
                )

                painter != null -> Icon(
                    painter = painter,
                    contentDescription = null,
                    tint = contentColor
                )
            }

            if (hasIcon) {
                Spacer(Modifier.width(dp8))
            }

            Text(
                text = text,
                color = contentColor
            )
        }
    }
}