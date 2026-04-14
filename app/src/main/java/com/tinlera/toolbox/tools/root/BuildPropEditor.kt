package com.tinlera.toolbox.tools.root

import com.tinlera.toolbox.core.RootBridge
import com.tinlera.toolbox.core.ShellResult

data class PropEntry(val key: String, val value: String)

object BuildPropEditor {

    suspend fun readAll(): List<PropEntry> {
        val result = RootBridge.execute("cat /system/build.prop")
        if (result.exitCode != 0) return emptyList()

        return result.output.lines()
            .filter { it.contains("=") && !it.startsWith("#") }
            .map { line ->
                val parts = line.split("=", limit = 2)
                PropEntry(parts[0].trim(), parts.getOrElse(1) { "" }.trim())
            }
            .sortedBy { it.key }
    }

    suspend fun getProp(key: String): String {
        return RootBridge.getprop(key)
    }

    suspend fun setProp(key: String, value: String): ShellResult {
        RootBridge.execute("setprop $key $value")
        return modifyBuildProp(key, value)
    }

    private suspend fun modifyBuildProp(key: String, value: String): ShellResult {
        RootBridge.execute("mount -o remount,rw /system 2>/dev/null")
        val exists = RootBridge.execute("grep -c '^$key=' /system/build.prop").output.trim()
        return if (exists != "0") {
            RootBridge.execute("sed -i 's|^$key=.*|$key=$value|' /system/build.prop")
        } else {
            RootBridge.execute("echo '$key=$value' >> /system/build.prop")
        }
    }

    suspend fun deleteProp(key: String): ShellResult {
        RootBridge.execute("mount -o remount,rw /system 2>/dev/null")
        return RootBridge.execute("sed -i '/^$key=/d' /system/build.prop")
    }

    suspend fun backup(): ShellResult {
        return RootBridge.execute("cp /system/build.prop /sdcard/build.prop.backup")
    }

    suspend fun restore(): ShellResult {
        RootBridge.execute("mount -o remount,rw /system 2>/dev/null")
        return RootBridge.execute("cp /sdcard/build.prop.backup /system/build.prop")
    }
}
