package com.tinlera.toolbox.tools.shizuku

import com.tinlera.toolbox.core.RootBridge
import com.tinlera.toolbox.core.ShellResult

object ArtOptimizer {

    enum class CompileMode(val flag: String, val label: String) {
        SPEED("speed", "Speed — Tam AOT derleme"),
        SPEED_PROFILE("speed-profile", "Speed Profile — Profil bazlı"),
        EVERYTHING("everything", "Everything — Tümünü derle"),
        VERIFY("verify", "Verify — Sadece doğrulama"),
        QUICKEN("quicken", "Quicken — Hızlı optimize"),
    }

    suspend fun compilePackage(packageName: String, mode: CompileMode): ShellResult {
        return RootBridge.execute("cmd package compile -m ${mode.flag} -f $packageName")
    }

    suspend fun compileAllPackages(mode: CompileMode): ShellResult {
        return RootBridge.execute("cmd package compile -m ${mode.flag} -f -a")
    }

    suspend fun triggerBgDexopt(): ShellResult {
        return RootBridge.execute("cmd package bg-dexopt-job")
    }

    suspend fun clearProfileData(packageName: String): ShellResult {
        return RootBridge.execute("cmd package compile --reset $packageName")
    }

    suspend fun clearAllProfileData(): ShellResult {
        return RootBridge.execute("cmd package compile --reset -a")
    }

    suspend fun getDexoptStatus(): String {
        val result = RootBridge.executeAsSh("dumpsys package dexopt | head -30")
        return result.output
    }
}
