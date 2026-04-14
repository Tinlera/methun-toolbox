package com.tinlera.toolbox.tools.shizuku

import com.tinlera.toolbox.core.RootBridge
import com.tinlera.toolbox.core.ShellResult

data class AppPermission(
    val packageName: String,
    val permission: String,
    val mode: String
)

object PermissionManager {

    suspend fun getAppOps(packageName: String): List<AppPermission> {
        val result = RootBridge.execute("appops get $packageName")
        if (result.exitCode != 0) return emptyList()

        return result.output.lines()
            .filter { it.contains(":") }
            .mapNotNull { line ->
                val parts = line.trim().split(":")
                if (parts.size >= 2) {
                    AppPermission(
                        packageName = packageName,
                        permission = parts[0].trim(),
                        mode = parts[1].trim().split(";").firstOrNull()?.trim() ?: "unknown"
                    )
                } else null
            }
    }

    suspend fun setAppOp(packageName: String, op: String, mode: String): ShellResult {
        return RootBridge.execute("appops set $packageName $op $mode")
    }

    suspend fun revokePermission(packageName: String, permission: String): ShellResult {
        return RootBridge.execute("pm revoke $packageName $permission")
    }

    suspend fun grantPermission(packageName: String, permission: String): ShellResult {
        return RootBridge.execute("pm grant $packageName $permission")
    }

    suspend fun listDangerousPermissions(packageName: String): List<String> {
        val result = RootBridge.execute("dumpsys package $packageName | grep 'android.permission' | grep -i 'granted=true'")
        return result.output.lines().map { it.trim() }.filter { it.isNotEmpty() }
    }

    val commonOps = listOf(
        "COARSE_LOCATION" to "Yaklaşık Konum",
        "FINE_LOCATION" to "Hassas Konum",
        "CAMERA" to "Kamera",
        "RECORD_AUDIO" to "Mikrofon",
        "READ_CONTACTS" to "Kişiler (Oku)",
        "READ_CALL_LOG" to "Arama Geçmişi",
        "READ_SMS" to "SMS (Oku)",
        "RUN_IN_BACKGROUND" to "Arka Planda Çalış",
        "RUN_ANY_IN_BACKGROUND" to "Her Zaman Arka Planda",
    )
}
