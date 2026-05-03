package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.applist

import androidx.lifecycle.SavedStateHandle
import com.pavlovalexey.pavlovAlexeySandbox.model.AppDetails
import com.pavlovalexey.pavlovAlexeySandbox.model.InstalledApp
import com.pavlovalexey.pavlovAlexeySandbox.repository.InstalledAppsRepository
import com.pavlovalexey.pavlovAlexeySandbox.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `shows first launch dialog when requested`() = runTest {
        val vm = createViewModel()

        vm.onOpenWithGridClick(shouldShowFirstLaunchDialog = true)

        assertTrue(vm.screenState.value.showFirstLaunchDialog)
        assertEquals(TEST_PACKAGE, vm.screenState.value.pendingGridPackage)
    }

    @Test
    fun `emits open with grid effect when no dialog required`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val effectDeferred = async(start = CoroutineStart.UNDISPATCHED) { vm.effects.first() }
        vm.onOpenWithGridClick(shouldShowFirstLaunchDialog = false)

        assertEquals(AppDetailEffect.OpenWithGrid(TEST_PACKAGE), effectDeferred.await())
    }

    @Test
    fun `confirm dialog emits pending package and clears state`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onOpenWithGridClick(shouldShowFirstLaunchDialog = true)

        val effectDeferred = async(start = CoroutineStart.UNDISPATCHED) { vm.effects.first() }
        vm.onFirstLaunchDialogConfirm()

        assertEquals(AppDetailEffect.OpenWithGrid(TEST_PACKAGE), effectDeferred.await())
        assertFalse(vm.screenState.value.showFirstLaunchDialog)
        assertEquals(null, vm.screenState.value.pendingGridPackage)
    }

    private fun createViewModel(): AppDetailViewModel {
        val repository = object : InstalledAppsRepository {
            override suspend fun getInstalledApps(): List<InstalledApp> = emptyList()

            override suspend fun getAppDetails(packageName: String): AppDetails =
                AppDetails(
                    appName = "Demo",
                    packageName = packageName,
                    versionName = "1.0",
                    versionCode = 1,
                    apkChecksumSha256 = null,
                    apkSizeBytes = null
                )
        }

        return AppDetailViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("packageName" to TEST_PACKAGE))
        )
    }

    private companion object {
        const val TEST_PACKAGE = "com.example.demo"
    }
}
