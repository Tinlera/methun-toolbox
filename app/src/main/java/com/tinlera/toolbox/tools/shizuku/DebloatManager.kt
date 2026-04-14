package com.tinlera.toolbox.tools.shizuku

import com.tinlera.toolbox.core.RootBridge
import com.tinlera.toolbox.core.ShellResult

data class AppInfo(
    val packageName: String,
    val label: String,
    val isEnabled: Boolean,
    val isSystem: Boolean
)

object DebloatManager {

    suspend fun listSystemApps(): List<AppInfo> {
        val result = RootBridge.executeAsSh("pm list packages -s")
        if (result.exitCode != 0) return emptyList()

        return result.output.lines()
            .filter { it.startsWith("package:") }
            .map { line ->
                val pkg = line.removePrefix("package:")
                val enabled = isPackageEnabled(pkg)
                AppInfo(pkg, pkg.substringAfterLast('.'), enabled, true)
            }
            .sortedBy { it.packageName }
    }

    suspend fun listDisabledApps(): List<AppInfo> {
        val result = RootBridge.executeAsSh("pm list packages -d")
        if (result.exitCode != 0) return emptyList()

        return result.output.lines()
            .filter { it.startsWith("package:") }
            .map { line ->
                val pkg = line.removePrefix("package:")
                AppInfo(pkg, pkg.substringAfterLast('.'), false, true)
            }
            .sortedBy { it.packageName }
    }

    private suspend fun isPackageEnabled(pkg: String): Boolean {
        val result = RootBridge.executeAsSh("pm list packages -e | grep $pkg")
        return result.output.contains(pkg)
    }

    suspend fun disableApp(packageName: String): ShellResult {
        return RootBridge.execute("pm disable-user --user 0 $packageName")
    }

    suspend fun enableApp(packageName: String): ShellResult {
        return RootBridge.execute("pm enable $packageName")
    }

    suspend fun uninstallForUser(packageName: String): ShellResult {
        return RootBridge.execute("pm uninstall -k --user 0 $packageName")
    }

    suspend fun reinstallForUser(packageName: String): ShellResult {
        return RootBridge.execute("pm install-existing --user 0 $packageName")
    }
}
