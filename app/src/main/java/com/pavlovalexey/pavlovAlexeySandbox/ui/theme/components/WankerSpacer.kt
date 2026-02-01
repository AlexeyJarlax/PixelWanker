package com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components

/** Павлов Алексей https://github.com/AlexeyJarlax */

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SpacerWidth(size: Int = 18) {
    Spacer(Modifier.width(size.dp))
}

//Spacer(modifier = Modifier.weight(1f))


@Composable
fun SpacerHeight(size: Int = 18) {
    Spacer(Modifier.height(size.dp))
}

//Spacer(modifier = Modifier.height(1f))