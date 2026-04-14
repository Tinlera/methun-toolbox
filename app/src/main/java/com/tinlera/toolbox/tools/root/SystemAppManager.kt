package com.tinlera.toolbox.tools.root

import com.tinlera.toolbox.core.RootBridge
import com.tinlera.toolbox.core.ShellResult

data class SystemApp(
    val packageName: String,
    val path: String,
    val isPrivApp: Boolean
)

object SystemAppManager {

    suspend fun listSystemApps(): List<SystemApp> {
        val apps = mutableListOf<SystemApp>()

        val sysResult = RootBridge.execute("ls /system/app/ 2>/dev/null")
        sysResult.output.lines().filter { it.isNotBlank() }.forEach { name ->
            apps.add(SystemApp(name, "/system/app/$name", false))
        }

        val privResult = RootBridge.execute("ls /system/priv-app/ 2>/dev/null")
        privResult.output.lines().filter { it.isNotBlank() }.forEach { name ->
            apps.add(SystemApp(name, "/system/priv-app/$name", true))
        }

        return apps.sortedBy { it.packageName }
    }

    suspend fun removeSystemApp(path: String): ShellResult {
        RootBridge.execute("mount -o remount,rw /system 2>/dev/null")
        return RootBridge.execute("rm -rf $path")
    }

    suspend fun backupSystemApp(path: String): ShellResult {
        val name = path.substringAfterLast("/")
        return RootBridge.execute("cp -r $path /sdcard/system_app_backup_$name")
    }

    suspend fun installSystemApp(apkPath: String, privApp: Boolean = false): ShellResult {
        val destDir = if (privApp) "/system/priv-app" else "/system/app"
        val name = apkPath.substringAfterLast("/").removeSuffix(".apk")
        RootBridge.execute("mount -o remount,rw /system 2>/dev/null")
        RootBridge.execute("mkdir -p $destDir/$name")
        val result = RootBridge.execute("cp $apkPath $destDir/$name/$name.apk")
        RootBridge.execute("chmod 644 $destDir/$name/$name.apk")
        RootBridge.execute("chown root:root $destDir/$name/$name.apk")
        return result
    }
}
