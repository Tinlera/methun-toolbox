package com.tinlera.toolbox.tools.shizuku

import com.tinlera.toolbox.core.RootBridge
import com.tinlera.toolbox.core.ShellResult

object ApkInstaller {

    suspend fun install(apkPath: String): ShellResult {
        return RootBridge.execute("pm install -r -g $apkPath")
    }

    suspend fun installWithOptions(
        apkPath: String,
        grantPermissions: Boolean = true,
        replace: Boolean = true,
        downgrade: Boolean = false
    ): ShellResult {
        val flags = buildString {
            if (replace) append("-r ")
            if (grantPermissions) append("-g ")
            if (downgrade) append("-d ")
        }
        return RootBridge.execute("pm install $flags$apkPath")
    }

    suspend fun installSplit(apkDir: String): ShellResult {
        val sessionResult = RootBridge.execute("pm install-create -r")
        val sessionId = Regex("\\[(\\d+)]").find(sessionResult.output)?.groupValues?.get(1)
            ?: return ShellResult(-1, "", "Session oluşturulamadı")

        val listResult = RootBridge.execute("ls $apkDir/*.apk")
        val apks = listResult.output.lines().filter { it.endsWith(".apk") }

        apks.forEachIndexed { index, apk ->
            val size = RootBridge.execute("stat -c%s $apk").output.trim()
            RootBridge.execute("pm install-write -S $size $sessionId $index $apk")
        }

        return RootBridge.execute("pm install-commit $sessionId")
    }

    suspend fun uninstall(packageName: String): ShellResult {
        return RootBridge.execute("pm uninstall $packageName")
    }
}
