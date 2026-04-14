package com.tinlera.toolbox.tools.root

import com.tinlera.toolbox.core.RootBridge
import com.tinlera.toolbox.core.ShellResult

object HostsEditor {

    private const val HOSTS_PATH = "/system/etc/hosts"

    suspend fun read(): String {
        val result = RootBridge.execute("cat $HOSTS_PATH")
        return result.output
    }

    suspend fun write(content: String): ShellResult {
        RootBridge.execute("mount -o remount,rw /system 2>/dev/null")
        return RootBridge.execute("echo '$content' > $HOSTS_PATH")
    }

    suspend fun addEntry(ip: String, hostname: String): ShellResult {
        RootBridge.execute("mount -o remount,rw /system 2>/dev/null")
        return RootBridge.execute("echo '$ip $hostname' >> $HOSTS_PATH")
    }

    suspend fun blockDomain(domain: String): ShellResult {
        return addEntry("127.0.0.1", domain)
    }

    suspend fun removeEntry(hostname: String): ShellResult {
        RootBridge.execute("mount -o remount,rw /system 2>/dev/null")
        return RootBridge.execute("sed -i '/$hostname/d' $HOSTS_PATH")
    }

    suspend fun backup(): ShellResult {
        return RootBridge.execute("cp $HOSTS_PATH /sdcard/hosts.backup")
    }

    suspend fun restore(): ShellResult {
        RootBridge.execute("mount -o remount,rw /system 2>/dev/null")
        return RootBridge.execute("cp /sdcard/hosts.backup $HOSTS_PATH")
    }

    suspend fun reset(): ShellResult {
        RootBridge.execute("mount -o remount,rw /system 2>/dev/null")
        return RootBridge.execute("echo '127.0.0.1 localhost\n::1 localhost' > $HOSTS_PATH")
    }

    suspend fun getEntryCount(): Int {
        val result = RootBridge.execute("grep -c '' $HOSTS_PATH")
        return result.output.trim().toIntOrNull() ?: 0
    }

    val adBlockLists = listOf(
        "StevenBlack" to "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts",
        "AdAway" to "https://adaway.org/hosts.txt",
        "Energized Basic" to "https://block.energized.pro/basic/formats/hosts",
    )
}
