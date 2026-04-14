package com.tinlera.toolbox.tools.shizuku

import com.tinlera.toolbox.core.RootBridge
import com.tinlera.toolbox.core.ShellResult

object CacheCleaner {

    suspend fun getAppCacheSize(packageName: String): String {
        val result = RootBridge.execute("du -sh /data/data/$packageName/cache 2>/dev/null")
        return result.output.split("\t").firstOrNull()?.trim() ?: "0"
    }

    suspend fun clearAppCache(packageName: String): ShellResult {
        return RootBridge.execute("pm clear --cache-only $packageName")
    }

    suspend fun clearAllCache(): ShellResult {
        return RootBridge.execute("pm trim-caches 999G")
    }

    suspend fun getTotalCacheSize(): String {
        val result = RootBridge.execute("du -sh /data/data/*/cache 2>/dev/null | tail -1")
        return result.output.split("\t").firstOrNull()?.trim() ?: "0"
    }

    suspend fun listLargestCaches(limit: Int = 15): List<Pair<String, String>> {
        val result = RootBridge.execute("du -s /data/data/*/cache 2>/dev/null | sort -rn | head -$limit")
        return result.output.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.trim().split("\t")
                if (parts.size >= 2) {
                    val size = parts[0].trim()
                    val pkg = parts[1].removePrefix("/data/data/").removeSuffix("/cache")
                    val sizeKb = size.toLongOrNull() ?: 0
                    val sizeStr = when {
                        sizeKb > 1024 * 1024 -> "%.1f GB".format(sizeKb / 1024.0 / 1024.0)
                        sizeKb > 1024 -> "%.1f MB".format(sizeKb / 1024.0)
                        else -> "$sizeKb KB"
                    }
                    pkg to sizeStr
                } else null
            }
    }
}
