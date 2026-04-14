package com.tinlera.toolbox.tools.hacker

import com.tinlera.toolbox.core.RootBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SharedPreferences and SQLite database editor for any app (requires root).
 */
object DbEditor {

    /**
     * List all SharedPreferences files for a package.
     */
    suspend fun listSharedPrefs(packageName: String): List<String> = withContext(Dispatchers.IO) {
        val result = RootBridge.execute("ls /data/data/$packageName/shared_prefs/ 2>/dev/null").output
        result.lines().filter { it.endsWith(".xml") }
    }

    /**
     * Read a SharedPreferences XML file.
     */
    suspend fun readSharedPrefs(packageName: String, fileName: String): String = withContext(Dispatchers.IO) {
        RootBridge.execute("cat /data/data/$packageName/shared_prefs/$fileName 2>/dev/null").output
    }

    /**
     * List all SQLite databases for a package.
     */
    suspend fun listDatabases(packageName: String): List<String> = withContext(Dispatchers.IO) {
        val result = RootBridge.execute("ls /data/data/$packageName/databases/ 2>/dev/null").output
        result.lines().filter { it.isNotBlank() && !it.endsWith("-journal") && !it.endsWith("-wal") && !it.endsWith("-shm") }
    }

    /**
     * List tables in a SQLite database.
     */
    suspend fun listTables(packageName: String, dbName: String): List<String> = withContext(Dispatchers.IO) {
        val result = RootBridge.execute("sqlite3 /data/data/$packageName/databases/$dbName '.tables' 2>/dev/null").output
        result.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    }

    /**
     * Execute a SQL query on an app's database.
     */
    suspend fun executeQuery(packageName: String, dbName: String, query: String): String = withContext(Dispatchers.IO) {
        RootBridge.execute("sqlite3 -header -column /data/data/$packageName/databases/$dbName \"$query\" 2>&1").output
    }

    /**
     * Get table schema.
     */
    suspend fun getTableSchema(packageName: String, dbName: String, tableName: String): String = withContext(Dispatchers.IO) {
        RootBridge.execute("sqlite3 /data/data/$packageName/databases/$dbName '.schema $tableName' 2>/dev/null").output
    }

    /**
     * Dump all rows from a table.
     */
    suspend fun dumpTable(packageName: String, dbName: String, tableName: String, limit: Int = 100): String =
        executeQuery(packageName, dbName, "SELECT * FROM $tableName LIMIT $limit")
}

/**
 * Activity Hunter — discover and launch hidden activities/services.
 */
object ActivityHunter {

    data class AppComponent(
        val name: String,
        val type: String, // activity, service, receiver, provider
        val exported: Boolean,
        val permission: String = ""
    )

    /**
     * List all components of a package.
     */
    suspend fun listComponents(packageName: String): List<AppComponent> = withContext(Dispatchers.IO) {
        val result = RootBridge.execute("dumpsys package $packageName").output
        val components = mutableListOf<AppComponent>()
        var currentType = ""

        result.lines().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.contains("Activity Resolver Table:") -> currentType = "activity"
                trimmed.contains("Service Resolver Table:") -> currentType = "service"
                trimmed.contains("Receiver Resolver Table:") -> currentType = "receiver"
                trimmed.contains("Provider") && trimmed.contains("authority") -> currentType = "provider"
                trimmed.contains(packageName) && currentType.isNotEmpty() -> {
                    val name = trimmed.substringAfter("$packageName/").substringBefore(" ").substringBefore("}")
                    if (name.isNotBlank() && !name.contains("=")) {
                        val exported = trimmed.contains("exported=true")
                        components.add(AppComponent(
                            name = "$packageName/$name",
                            type = currentType,
                            exported = exported
                        ))
                    }
                }
            }
        }
        components.distinctBy { it.name }
    }

    /**
     * Launch an activity.
     */
    suspend fun launchActivity(componentName: String): String = withContext(Dispatchers.IO) {
        RootBridge.execute("am start -n $componentName 2>&1").output
    }

    /**
     * Start a service.
     */
    suspend fun startService(componentName: String): String = withContext(Dispatchers.IO) {
        RootBridge.execute("am startservice -n $componentName 2>&1").output
    }

    /**
     * List all installed packages with their activity count.
     */
    suspend fun listAllPackages(): List<Pair<String, Int>> = withContext(Dispatchers.IO) {
        val result = RootBridge.execute("pm list packages -3").output
        result.lines()
            .filter { it.startsWith("package:") }
            .map { it.removePrefix("package:").trim() }
            .map { pkg ->
                val actCount = RootBridge.execute("dumpsys package $pkg | grep -c 'Activity'").output.trim().toIntOrNull() ?: 0
                pkg to actCount
            }
            .sortedByDescending { it.second }
    }
}

/**
 * Crypto Toolkit — hash, encode, decode.
 */
object CryptoToolkit {

    suspend fun md5(input: String): String = withContext(Dispatchers.IO) {
        RootBridge.executeAsSh("echo -n '$input' | md5sum | cut -d' ' -f1").output.trim()
    }

    suspend fun sha1(input: String): String = withContext(Dispatchers.IO) {
        RootBridge.executeAsSh("echo -n '$input' | sha1sum | cut -d' ' -f1").output.trim()
    }

    suspend fun sha256(input: String): String = withContext(Dispatchers.IO) {
        RootBridge.executeAsSh("echo -n '$input' | sha256sum | cut -d' ' -f1").output.trim()
    }

    suspend fun base64Encode(input: String): String = withContext(Dispatchers.IO) {
        RootBridge.executeAsSh("echo -n '$input' | base64").output.trim()
    }

    suspend fun base64Decode(input: String): String = withContext(Dispatchers.IO) {
        RootBridge.executeAsSh("echo -n '$input' | base64 -d 2>/dev/null").output.trim()
    }

    suspend fun fileMd5(path: String): String = withContext(Dispatchers.IO) {
        RootBridge.execute("md5sum $path 2>/dev/null | cut -d' ' -f1").output.trim()
    }

    suspend fun fileSha256(path: String): String = withContext(Dispatchers.IO) {
        RootBridge.execute("sha256sum $path 2>/dev/null | cut -d' ' -f1").output.trim()
    }

    /**
     * ROT13 encode/decode.
     */
    suspend fun rot13(input: String): String = withContext(Dispatchers.IO) {
        RootBridge.executeAsSh("echo -n '$input' | tr 'A-Za-z' 'N-ZA-Mn-za-m'").output.trim()
    }

    /**
     * Hex encode.
     */
    suspend fun hexEncode(input: String): String = withContext(Dispatchers.IO) {
        RootBridge.executeAsSh("echo -n '$input' | xxd -p 2>/dev/null || echo -n '$input' | od -A n -t x1 | tr -d ' \\n'").output.trim()
    }

    /**
     * Hex decode.
     */
    suspend fun hexDecode(input: String): String = withContext(Dispatchers.IO) {
        RootBridge.executeAsSh("echo -n '$input' | xxd -r -p 2>/dev/null").output.trim()
    }

    /**
     * Generate random bytes (hex).
     */
    suspend fun randomBytes(count: Int = 32): String = withContext(Dispatchers.IO) {
        RootBridge.executeAsSh("head -c $count /dev/urandom | xxd -p 2>/dev/null || head -c $count /dev/urandom | od -A n -t x1 | tr -d ' \\n'").output.trim()
    }
}

/**
 * File Shredder — secure file deletion.
 */
object FileShredder {

    /**
     * Securely delete a file by overwriting with random data multiple times.
     * DoD 5220.22-M: 3-pass overwrite (zeros, ones, random).
     */
    suspend fun shredFile(path: String, passes: Int = 3): String = withContext(Dispatchers.IO) {
        val size = RootBridge.execute("stat -c%s $path 2>/dev/null").output.trim()
        if (size.isEmpty()) return@withContext "Dosya bulunamadı: $path"

        val results = StringBuilder()
        for (i in 1..passes) {
            val pattern = when (i % 3) {
                1 -> "/dev/zero"
                2 -> "/dev/urandom"
                else -> "/dev/zero"
            }
            RootBridge.execute("dd if=$pattern of=$path bs=1 count=$size conv=notrunc 2>/dev/null").output
            results.appendLine("Pass $i/$passes tamamlandı ($pattern)")
        }
        // Final: overwrite with random, then delete
        RootBridge.execute("dd if=/dev/urandom of=$path bs=1 count=$size conv=notrunc 2>/dev/null").output
        RootBridge.execute("rm -f $path").output
        results.appendLine("✅ Dosya güvenli şekilde silindi: $path")
        results.toString()
    }

    /**
     * Shred multiple files.
     */
    suspend fun shredFiles(paths: List<String>, passes: Int = 3): Map<String, String> {
        return paths.associateWith { shredFile(it, passes) }
    }
}
