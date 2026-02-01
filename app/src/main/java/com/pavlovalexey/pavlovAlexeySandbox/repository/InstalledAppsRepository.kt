package com.pavlovalexey.pavlovAlexeySandbox.repository

/** Павлов Алексей https://github.com/AlexeyJarlax */

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.pavlovalexey.pavlovAlexeySandbox.model.AppDetails
import com.pavlovalexey.pavlovAlexeySandbox.model.InstalledApp
import java.io.File
import java.security.MessageDigest

interface InstalledAppsRepository {
    suspend fun getInstalledApps(): List<InstalledApp>
    suspend fun getAppDetails(packageName: String): AppDetails
}

class InstalledAppsRepositoryImpl(
    private val context: Context
) : InstalledAppsRepository {

    private val pm: PackageManager = context.packageManager

    override suspend fun getInstalledApps(): List<InstalledApp> {
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        return apps
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .map { appInfo ->
                val pkgInfo =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pm.getPackageInfo(
                            appInfo.packageName,
                            PackageManager.PackageInfoFlags.of(0)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        pm.getPackageInfo(appInfo.packageName, 0)
                    }

                InstalledApp(
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    packageName = appInfo.packageName,
                    versionName = pkgInfo.versionName
                )
            }
            .sortedBy { it.appName.lowercase() }
    }

    override suspend fun getAppDetails(packageName: String): AppDetails {
        val pkgInfo =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }

        val appInfo = pkgInfo.applicationInfo
            ?: throw IllegalStateException("applicationInfo is null for package $packageName")

        val appName = pm.getApplicationLabel(appInfo).toString()
        val versionName = pkgInfo.versionName
        val versionCode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pkgInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pkgInfo.versionCode.toLong()
            }

        val apkPath = appInfo.publicSourceDir ?: appInfo.sourceDir

        val apkFile = File(apkPath)
        val apkSizeBytes = runCatching { apkFile.length() }.getOrNull()

        val checksum = calculateSha256(apkPath)

        return AppDetails(
            appName = appName,
            packageName = packageName,
            versionName = versionName,
            versionCode = versionCode,
            apkChecksumSha256 = checksum,
            apkSizeBytes = apkSizeBytes
        )
    }

    private fun calculateSha256(path: String): String? {
        return try {
            val file = File(path)
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var read = input.read(buffer)
                while (read != -1) {
                    digest.update(buffer, 0, read)
                    read = input.read(buffer)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            null
        }
    }
}
