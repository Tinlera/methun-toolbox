package com.tinlera.toolbox.tools.root

import com.tinlera.toolbox.core.RootBridge
import com.tinlera.toolbox.core.ShellResult

data class ModuleInfo(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val enabled: Boolean
)

object ModuleManager {

    private suspend fun detectRootType(): String {
        val ksu = RootBridge.execute("ls /data/adb/ksu 2>/dev/null")
        if (ksu.exitCode == 0 && ksu.output.isNotBlank()) return "kernelsu"

        val magisk = RootBridge.execute("ls /data/adb/magisk 2>/dev/null")
        if (magisk.exitCode == 0 && magisk.output.isNotBlank()) return "magisk"

        return "unknown"
    }

    suspend fun getRootType(): String = detectRootType()

    suspend fun listModules(): List<ModuleInfo> {
        val modulesDir = "/data/adb/modules"
        val result = RootBridge.execute("ls $modulesDir 2>/dev/null")
        if (result.exitCode != 0) return emptyList()

        return result.output.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { moduleId ->
                val propResult = RootBridge.execute("cat $modulesDir/$moduleId/module.prop 2>/dev/null")
                if (propResult.exitCode != 0) return@mapNotNull null

                val props = propResult.output.lines().associate { line ->
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) parts[0].trim() to parts[1].trim()
                    else "" to ""
                }

                val disabled = RootBridge.execute("test -f $modulesDir/$moduleId/disable && echo 1 || echo 0")
                val isEnabled = disabled.output.trim() != "1"

                ModuleInfo(
                    id = moduleId,
                    name = props["name"] ?: moduleId,
                    version = props["version"] ?: "unknown",
                    description = props["description"] ?: "",
                    enabled = isEnabled
                )
            }
    }

    suspend fun enableModule(moduleId: String): ShellResult {
        return RootBridge.execute("rm -f /data/adb/modules/$moduleId/disable")
    }

    suspend fun disableModule(moduleId: String): ShellResult {
        return RootBridge.execute("touch /data/adb/modules/$moduleId/disable")
    }

    suspend fun removeModule(moduleId: String): ShellResult {
        return RootBridge.execute("touch /data/adb/modules/$moduleId/remove")
    }
}
