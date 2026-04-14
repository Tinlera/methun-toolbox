package com.tinlera.toolbox.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class ShellResult(
    val exitCode: Int,
    val output: String,
    val error: String
)

object RootBridge {

    suspend fun execute(command: String): ShellResult = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText()
            val exitCode = process.waitFor()
            ShellResult(exitCode, stdout.trim(), stderr.trim())
        } catch (e: Exception) {
            ShellResult(-1, "", e.message ?: "Unknown error")
        }
    }

    suspend fun isRootAvailable(): Boolean {
        val result = execute("id")
        return result.exitCode == 0 && result.output.contains("uid=0")
    }

    suspend fun executeAsSh(command: String): ShellResult = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText()
            val exitCode = process.waitFor()
            ShellResult(exitCode, stdout.trim(), stderr.trim())
        } catch (e: Exception) {
            ShellResult(-1, "", e.message ?: "Unknown error")
        }
    }

    suspend fun getprop(key: String): String {
        val result = executeAsSh("getprop $key")
        return result.output
    }
}
