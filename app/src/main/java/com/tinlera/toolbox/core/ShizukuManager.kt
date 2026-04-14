package com.tinlera.toolbox.core

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

object ShizukuManager {

    private var binderReceivedListener: Shizuku.OnBinderReceivedListener? = null
    private var binderDeadListener: Shizuku.OnBinderDeadListener? = null
    private var permissionResultListener: Shizuku.OnRequestPermissionResultListener? = null

    var isBinderAlive = false
        private set
    var hasPermission = false
        private set

    fun init(onStateChanged: (alive: Boolean, granted: Boolean) -> Unit) {
        binderReceivedListener = Shizuku.OnBinderReceivedListener {
            isBinderAlive = true
            hasPermission = checkPermission()
            onStateChanged(isBinderAlive, hasPermission)
        }
        binderDeadListener = Shizuku.OnBinderDeadListener {
            isBinderAlive = false
            hasPermission = false
            onStateChanged(isBinderAlive, hasPermission)
        }
        permissionResultListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            hasPermission = grantResult == PackageManager.PERMISSION_GRANTED
            onStateChanged(isBinderAlive, hasPermission)
        }

        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener!!)
        Shizuku.addBinderDeadListener(binderDeadListener!!)
        Shizuku.addRequestPermissionResultListener(permissionResultListener!!)
    }

    fun destroy() {
        binderReceivedListener?.let { Shizuku.removeBinderReceivedListener(it) }
        binderDeadListener?.let { Shizuku.removeBinderDeadListener(it) }
        permissionResultListener?.let { Shizuku.removeRequestPermissionResultListener(it) }
    }

    fun checkPermission(): Boolean {
        return try {
            if (!Shizuku.pingBinder()) return false
            if (Shizuku.isPreV11()) false
            else Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun requestPermission(requestCode: Int) {
        if (!Shizuku.isPreV11()) {
            Shizuku.requestPermission(requestCode)
        }
    }

    fun isInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun executeCommand(command: String): ShellResult {
        return try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()
            process.waitFor()
            val exitCode = process.exitValue()
            ShellResult(exitCode, stdout.trim(), stderr.trim())
        } catch (e: Exception) {
            ShellResult(-1, "", e.message ?: "Shizuku error")
        }
    }
}
