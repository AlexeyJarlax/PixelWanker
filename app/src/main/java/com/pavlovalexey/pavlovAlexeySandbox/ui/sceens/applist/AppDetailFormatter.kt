package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist

private const val BYTES_IN_MB = 1024.0 * 1024.0

fun formatApkSizeInMb(bytes: Long): Double = bytes / BYTES_IN_MB
