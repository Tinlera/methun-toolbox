package com.tinlera.toolbox.tools.root

import com.tinlera.toolbox.core.RootBridge
import com.tinlera.toolbox.core.ShellResult

object SelinuxManager {

    suspend fun getStatus(): String {
        val result = RootBridge.execute("getenforce")
        return result.output.trim()
    }

    suspend fun setEnforcing(): ShellResult {
        return RootBridge.execute("setenforce 1")
    }

    suspend fun setPermissive(): ShellResult {
        return RootBridge.execute("setenforce 0")
    }

    suspend fun toggle(): ShellResult {
        val current = getStatus()
        return if (current.equals("Enforcing", ignoreCase = true)) {
            setPermissive()
        } else {
            setEnforcing()
        }
    }

    suspend fun isEnforcing(): Boolean {
        return getStatus().equals("Enforcing", ignoreCase = true)
    }
}
