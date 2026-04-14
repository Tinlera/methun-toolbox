package com.tinlera.toolbox.core

import android.os.Build

data class DeviceInfo(
    val model: String,
    val brand: String,
    val device: String,
    val androidVersion: String,
    val sdkLevel: Int,
    val securityPatch: String,
    val kernel: String,
    val cpuAbi: String,
    val totalRam: String,
    val rootStatus: String,
    val shizukuStatus: String,
    val selinuxStatus: String,
)

object DeviceInfoProvider {

    suspend fun collect(): DeviceInfo {
        val rootAvailable = RootBridge.isRootAvailable()
        val kernel = RootBridge.executeAsSh("uname -r").output
        val selinux = if (rootAvailable) {
            RootBridge.execute("getenforce").output
        } else "Unknown"

        val totalRam = try {
            val memInfo = RootBridge.executeAsSh("cat /proc/meminfo | head -1").output
            val kb = memInfo.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0
            "%.1f GB".format(kb / 1024.0 / 1024.0)
        } catch (e: Exception) { "Unknown" }

        return DeviceInfo(
            model = Build.MODEL,
            brand = Build.BRAND.replaceFirstChar { it.uppercase() },
            device = Build.DEVICE,
            androidVersion = Build.VERSION.RELEASE,
            sdkLevel = Build.VERSION.SDK_INT,
            securityPatch = Build.VERSION.SECURITY_PATCH,
            kernel = kernel,
            cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown",
            totalRam = totalRam,
            rootStatus = if (rootAvailable) "✅ Root" else "❌ No Root",
            shizukuStatus = if (ShizukuManager.isBinderAlive) {
                if (ShizukuManager.hasPermission) "✅ Active" else "⚠️ No Permission"
            } else "❌ Not Running",
            selinuxStatus = selinux
        )
    }
}
