package com.gfxtool.roblox.domain

import android.content.Context
import android.content.pm.PackageManager

/**
 * Phát hiện phiên bản Roblox được cài trên thiết bị.
 */
object RobloxDetector {

    private val KNOWN_PACKAGES = listOf(
        "com.roblox.client",
        "com.roblox.client2",
    )

    data class RobloxInfo(
        val packageName : String,
        val versionName : String,
        val versionCode : Long,
        val dataDir     : String,
    )

    /**
     * Trả về danh sách các bản Roblox tìm thấy trên thiết bị.
     */
    fun detect(context: Context): List<RobloxInfo> {
        val pm = context.packageManager
        return KNOWN_PACKAGES.mapNotNull { pkg ->
            try {
                val info = pm.getPackageInfo(pkg, 0)
                val appInfo = pm.getApplicationInfo(pkg, 0)
                RobloxInfo(
                    packageName = pkg,
                    versionName = info.versionName ?: "unknown",
                    versionCode = info.longVersionCode,
                    dataDir     = appInfo.dataDir,
                )
            } catch (e: PackageManager.NameNotFoundException) { null }
        }
    }

    /**
     * Lấy package name Roblox chính đang hoạt động.
     */
    fun primaryPackage(context: Context): String? =
        detect(context).firstOrNull()?.packageName

    /**
     * Các đường dẫn config có thể có cho từng package.
     */
    fun configPaths(packageName: String): List<String> = listOf(
        "/sdcard/Android/data/$packageName/files/ClientAppSettings.json",
        "/sdcard/Android/data/$packageName/files/LocalStorage/ClientAppSettings.json",
        "/data/data/$packageName/files/ClientAppSettings.json",
        "/data/data/$packageName/files/LocalStorage/ClientAppSettings.json",
    )
}
