package com.tinlera.toolbox.tools.root

import com.tinlera.toolbox.core.RootBridge
import com.tinlera.toolbox.core.ShellResult

data class CpuInfo(
    val core: Int,
    val currentFreq: String,
    val minFreq: String,
    val maxFreq: String,
    val governor: String,
    val availableGovernors: List<String>
)

object GovernorManager {

    suspend fun getCpuInfo(): List<CpuInfo> {
        val coreCount = RootBridge.execute("nproc").output.trim().toIntOrNull() ?: 0
        val cpus = mutableListOf<CpuInfo>()

        for (i in 0 until coreCount) {
            val base = "/sys/devices/system/cpu/cpu$i/cpufreq"
            val current = RootBridge.execute("cat $base/scaling_cur_freq 2>/dev/null").output.trim()
            val min = RootBridge.execute("cat $base/scaling_min_freq 2>/dev/null").output.trim()
            val max = RootBridge.execute("cat $base/scaling_max_freq 2>/dev/null").output.trim()
            val gov = RootBridge.execute("cat $base/scaling_governor 2>/dev/null").output.trim()
            val avail = RootBridge.execute("cat $base/scaling_available_governors 2>/dev/null").output.trim()

            cpus.add(CpuInfo(
                core = i,
                currentFreq = formatFreq(current),
                minFreq = formatFreq(min),
                maxFreq = formatFreq(max),
                governor = gov,
                availableGovernors = avail.split(" ").filter { it.isNotBlank() }
            ))
        }
        return cpus
    }

    suspend fun setGovernor(core: Int, governor: String): ShellResult {
        return RootBridge.execute("echo $governor > /sys/devices/system/cpu/cpu$core/cpufreq/scaling_governor")
    }

    suspend fun setAllGovernors(governor: String): ShellResult {
        val coreCount = RootBridge.execute("nproc").output.trim().toIntOrNull() ?: 0
        var lastResult = ShellResult(0, "", "")
        for (i in 0 until coreCount) {
            lastResult = setGovernor(i, governor)
        }
        return lastResult
    }

    suspend fun getGpuGovernor(): String {
        val paths = listOf(
            "/sys/class/devfreq/13000000.mali/governor",
            "/sys/class/devfreq/gpufreq/governor",
            "/sys/devices/platform/mali/devfreq/devfreq0/governor"
        )
        for (path in paths) {
            val result = RootBridge.execute("cat $path 2>/dev/null")
            if (result.exitCode == 0 && result.output.isNotBlank()) return result.output.trim()
        }
        return "unknown"
    }

    suspend fun getAvailableGpuGovernors(): List<String> {
        val paths = listOf(
            "/sys/class/devfreq/13000000.mali/available_governors",
            "/sys/class/devfreq/gpufreq/available_governors"
        )
        for (path in paths) {
            val result = RootBridge.execute("cat $path 2>/dev/null")
            if (result.exitCode == 0 && result.output.isNotBlank()) {
                return result.output.trim().split(" ").filter { it.isNotBlank() }
            }
        }
        return emptyList()
    }

    private fun formatFreq(khz: String): String {
        val freq = khz.toLongOrNull() ?: return khz
        return if (freq > 1000000) "%.2f GHz".format(freq / 1000000.0)
        else "%.0f MHz".format(freq / 1000.0)
    }
}
