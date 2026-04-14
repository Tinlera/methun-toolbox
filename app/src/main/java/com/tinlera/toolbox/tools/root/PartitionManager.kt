package com.tinlera.toolbox.tools.root

import com.tinlera.toolbox.core.RootBridge

data class PartitionInfo(
    val device: String,
    val mountPoint: String,
    val type: String,
    val size: String,
    val used: String,
    val available: String,
    val usagePercent: String
)

object PartitionManager {

    suspend fun listMountedPartitions(): List<PartitionInfo> {
        val result = RootBridge.execute("df -h 2>/dev/null")
        if (result.exitCode != 0) return emptyList()

        return result.output.lines()
            .drop(1)
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.trim().split(Regex("\\s+"))
                if (parts.size >= 6) {
                    PartitionInfo(
                        device = parts[0],
                        size = parts[1],
                        used = parts[2],
                        available = parts[3],
                        usagePercent = parts[4],
                        mountPoint = parts[5],
                        type = ""
                    )
                } else null
            }
    }

    suspend fun getPartitionTable(): String {
        val result = RootBridge.execute("cat /proc/partitions")
        return result.output
    }

    suspend fun getMountInfo(): String {
        val result = RootBridge.execute("mount | head -30")
        return result.output
    }

    suspend fun getBlockDevices(): String {
        val result = RootBridge.execute("ls -la /dev/block/by-name/ 2>/dev/null")
        return result.output
    }

    suspend fun getStorageInfo(): Pair<String, String> {
        val internal = RootBridge.execute("df -h /data | tail -1").output
        val system = RootBridge.execute("df -h /system | tail -1").output
        return internal to system
    }
}
