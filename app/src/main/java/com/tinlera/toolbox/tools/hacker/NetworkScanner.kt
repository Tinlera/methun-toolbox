package com.tinlera.toolbox.tools.hacker

import com.tinlera.toolbox.core.RootBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket

data class NetworkDevice(
    val ip: String,
    val mac: String = "",
    val hostname: String = "",
    val openPorts: List<Int> = emptyList(),
    val vendor: String = ""
)

data class PortResult(
    val port: Int,
    val isOpen: Boolean,
    val service: String = ""
)

object NetworkScanner {

    private val commonPorts = mapOf(
        21 to "FTP", 22 to "SSH", 23 to "Telnet", 25 to "SMTP",
        53 to "DNS", 80 to "HTTP", 110 to "POP3", 143 to "IMAP",
        443 to "HTTPS", 445 to "SMB", 993 to "IMAPS", 995 to "POP3S",
        3306 to "MySQL", 5432 to "PostgreSQL", 5555 to "ADB",
        6379 to "Redis", 8080 to "HTTP-Alt", 8443 to "HTTPS-Alt",
        8888 to "HTTP-Proxy", 9090 to "WebSocket", 27017 to "MongoDB"
    )

    suspend fun getLocalIp(): String = withContext(Dispatchers.IO) {
        RootBridge.execute("ip route | grep -oP 'src \\K[0-9.]+' | head -1").output.trim()
    }

    suspend fun getArpTable(): List<NetworkDevice> = withContext(Dispatchers.IO) {
        val result = RootBridge.execute("ip neigh show").output
        result.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val parts = line.split("\\s+".toRegex())
            val ip = parts.firstOrNull() ?: return@mapNotNull null
            val mac = parts.getOrNull(parts.indexOf("lladdr") + 1) ?: ""
            if (ip.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+"))) {
                NetworkDevice(ip = ip, mac = mac)
            } else null
        }
    }

    suspend fun pingSweep(subnet: String): List<String> = withContext(Dispatchers.IO) {
        val result = RootBridge.execute(
            "for i in \$(seq 1 254); do " +
            "(ping -c1 -W1 $subnet.\$i > /dev/null 2>&1 && echo $subnet.\$i) & " +
            "done; wait"
        ).output
        result.lines().filter { it.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")) }
    }

    suspend fun scanPorts(
        targetIp: String,
        ports: List<Int> = commonPorts.keys.toList(),
        timeoutMs: Int = 500
    ): List<PortResult> = withContext(Dispatchers.IO) {
        ports.map { port ->
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(targetIp, port), timeoutMs)
                socket.close()
                PortResult(port, true, commonPorts[port] ?: "Unknown")
            } catch (e: Exception) {
                PortResult(port, false, commonPorts[port] ?: "Unknown")
            }
        }
    }

    suspend fun quickScan(targetIp: String): List<PortResult> =
        scanPorts(targetIp, listOf(21, 22, 80, 443, 3306, 5555, 8080))

    suspend fun getWifiInfo(): Map<String, String> = withContext(Dispatchers.IO) {
        val result = RootBridge.execute("dumpsys wifi | grep -E 'mWifiInfo|SSID|BSSID|Link speed|Frequency|IP'").output
        val info = mutableMapOf<String, String>()
        result.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.contains(":")) {
                val (key, value) = trimmed.split(":", limit = 2)
                info[key.trim()] = value.trim()
            }
        }
        info
    }

    suspend fun getInterfaces(): String = withContext(Dispatchers.IO) {
        RootBridge.execute("ip addr show").output
    }

    suspend fun getRoutes(): String = withContext(Dispatchers.IO) {
        RootBridge.execute("ip route show").output
    }

    suspend fun getConnections(): String = withContext(Dispatchers.IO) {
        RootBridge.execute("ss -tunap 2>/dev/null || netstat -tunap 2>/dev/null || cat /proc/net/tcp").output
    }

    suspend fun dnsLookup(domain: String): String = withContext(Dispatchers.IO) {
        RootBridge.execute("nslookup $domain 2>&1 || getent hosts $domain").output
    }

    suspend fun traceroute(target: String): String = withContext(Dispatchers.IO) {
        RootBridge.execute("traceroute -m 15 $target 2>&1 || tracepath $target 2>&1").output
    }
}
