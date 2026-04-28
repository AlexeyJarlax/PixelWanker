package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist

import org.junit.Assert.assertEquals
import org.junit.Test

class AppDetailFormatterTest {

    @Test
    fun `converts bytes to megabytes`() {
        assertEquals(1.0, formatApkSizeInMb(1024L * 1024L), 0.000001)
        assertEquals(2.5, formatApkSizeInMb((2.5 * 1024 * 1024).toLong()), 0.01)
    }
}
