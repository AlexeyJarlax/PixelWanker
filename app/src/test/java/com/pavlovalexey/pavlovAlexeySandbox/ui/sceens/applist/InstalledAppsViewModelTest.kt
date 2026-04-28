package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist

import com.pavlovalexey.pavlovAlexeySandbox.model.AppDetails
import com.pavlovalexey.pavlovAlexeySandbox.model.InstalledApp
import com.pavlovalexey.pavlovAlexeySandbox.repository.InstalledAppsRepository
import com.pavlovalexey.pavlovAlexeySandbox.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InstalledAppsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `filters by app name and package ignoring case`() = runTest {
        val apps = listOf(
            InstalledApp("Telegram", "org.telegram.messenger", "10"),
            InstalledApp("GitHub", "com.github.android", "1"),
            InstalledApp("Maps", "com.google.maps", "2")
        )
        val viewModel = InstalledAppsViewModel(FakeRepository(apps))

        advanceUntilIdle()
        viewModel.onSearchQueryChange("git")
        advanceUntilIdle()

        assertEquals(listOf("GitHub"), viewModel.filteredApps.value.map { it.appName })

        viewModel.onSearchQueryChange("TELEGRAM")
        advanceUntilIdle()
        assertEquals(listOf("Telegram"), viewModel.filteredApps.value.map { it.appName })

        viewModel.onSearchQueryChange("google")
        advanceUntilIdle()
        assertEquals(listOf("Maps"), viewModel.filteredApps.value.map { it.appName })
    }

    @Test
    fun `returns all apps when query is blank with spaces`() = runTest {
        val apps = listOf(
            InstalledApp("One", "a.one", "1"),
            InstalledApp("Two", "a.two", "1")
        )
        val viewModel = InstalledAppsViewModel(FakeRepository(apps))

        advanceUntilIdle()
        viewModel.onSearchQueryChange("   ")
        advanceUntilIdle()

        assertEquals(2, viewModel.filteredApps.value.size)
        assertTrue(viewModel.filteredApps.value.containsAll(apps))
    }

    private class FakeRepository(
        private val installedApps: List<InstalledApp>
    ) : InstalledAppsRepository {
        override suspend fun getInstalledApps(): List<InstalledApp> = installedApps

        override suspend fun getAppDetails(packageName: String): AppDetails {
            error("not used")
        }
    }
}
