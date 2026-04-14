package com.tinlera.toolbox.remote

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ADB connection types supported by Remote Phone Control.
 */
enum class ConnectionType {
    USB_OTG,    // USB cable via OTG
    WIFI_ADB    // Wireless ADB over network
}

/**
 * Represents a connected or discovered remote device.
 */
data class RemoteDevice(
    val id: String,
    val name: String,
    val model: String = "Unknown",
    val androidVersion: String = "",
    val connectionType: ConnectionType,
    val address: String = "",  // IP:port for WiFi, USB serial for OTG
    val isConnected: Boolean = false
)

/**
 * Result wrapper for ADB command execution on remote device.
 */
data class RemoteCommandResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
) {
    val success: Boolean get() = exitCode == 0
}

/**
 * Core ADB client that manages connections and command execution
 * to remote Android devices via USB OTG or WiFi ADB.
 *
 * Uses the device's root access to run ADB commands locally,
 * targeting the connected remote device.
 */
class AdbClient(private val context: Context) {

    private var currentDevice: RemoteDevice? = null
    private val connectedDevices = mutableListOf<RemoteDevice>()

    /**
     * Check if ADB binary is available on the device.
     */
    suspend fun isAdbAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "which adb"))
            val result = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            result.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get list of USB devices connected via OTG.
     */
    fun getUsbDevices(): List<UsbDevice> {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        return usbManager.deviceList.values.toList()
    }

    /**
     * Start ADB server with root privileges.
     */
    suspend fun startAdbServer(): RemoteCommandResult = executeRootAdb("start-server")

    /**
     * Stop ADB server.
     */
    suspend fun stopAdbServer(): RemoteCommandResult = executeRootAdb("kill-server")

    /**
     * Connect to a remote device via WiFi ADB.
     * @param ip IP address of the target device
     * @param port ADB port (default 5555)
     */
    suspend fun connectWifi(ip: String, port: Int = 5555): RemoteCommandResult {
        val result = executeRootAdb("connect $ip:$port")
        if (result.success || result.stdout.contains("connected")) {
            val device = RemoteDevice(
                id = "$ip:$port",
                name = "WiFi Device",
                connectionType = ConnectionType.WIFI_ADB,
                address = "$ip:$port",
                isConnected = true
            )
            connectedDevices.removeAll { it.id == device.id }
            connectedDevices.add(device)
            if (currentDevice == null) currentDevice = device

            // Fetch device info
            refreshDeviceInfo(device)
        }
        return result
    }

    /**
     * Disconnect a WiFi ADB device.
     */
    suspend fun disconnectWifi(ip: String, port: Int = 5555): RemoteCommandResult {
        val result = executeRootAdb("disconnect $ip:$port")
        connectedDevices.removeAll { it.id == "$ip:$port" }
        if (currentDevice?.id == "$ip:$port") {
            currentDevice = connectedDevices.firstOrNull()
        }
        return result
    }

    /**
     * List all connected ADB devices.
     */
    suspend fun listDevices(): List<RemoteDevice> = withContext(Dispatchers.IO) {
        val result = executeRootAdb("devices -l")
        if (!result.success) return@withContext emptyList()

        val devices = mutableListOf<RemoteDevice>()
        result.stdout.lines().drop(1).forEach { line ->
            if (line.isBlank() || !line.contains("device")) return@forEach
            val parts = line.trim().split("\\s+".toRegex())
            if (parts.size >= 2 && parts[1] == "device") {
                val address = parts[0]
                val model = parts.find { it.startsWith("model:") }
                    ?.removePrefix("model:") ?: "Unknown"

                val type = if (address.contains(":")) ConnectionType.WIFI_ADB else ConnectionType.USB_OTG
                devices.add(
                    RemoteDevice(
                        id = address,
                        name = model,
                        model = model,
                        connectionType = type,
                        address = address,
                        isConnected = true
                    )
                )
            }
        }
        connectedDevices.clear()
        connectedDevices.addAll(devices)
        devices
    }

    /**
     * Execute a shell command on the remote device.
     */
    suspend fun remoteShell(command: String): RemoteCommandResult {
        val target = currentDevice ?: return RemoteCommandResult(-1, "", "No device connected")
        return executeRootAdb("-s ${target.address} shell $command")
    }

    /**
     * Take a screenshot from the remote device.
     * Returns raw PNG bytes.
     */
    suspend fun remoteScreencap(): ByteArray? = withContext(Dispatchers.IO) {
        val target = currentDevice ?: return@withContext null
        try {
            val process = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "adb -s ${target.address} exec-out screencap -p")
            )
            val bytes = process.inputStream.readBytes()
            process.waitFor()
            if (bytes.isNotEmpty()) bytes else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Send tap event to the remote device.
     */
    suspend fun remoteTap(x: Int, y: Int): RemoteCommandResult =
        remoteShell("input tap $x $y")

    /**
     * Send swipe event to the remote device.
     */
    suspend fun remoteSwipe(x1: Int, y1: Int, x2: Int, y2: Int, durationMs: Int = 300): RemoteCommandResult =
        remoteShell("input swipe $x1 $y1 $x2 $y2 $durationMs")

    /**
     * Send key event to the remote device.
     */
    suspend fun remoteKeyEvent(keyCode: Int): RemoteCommandResult =
        remoteShell("input keyevent $keyCode")

    /**
     * Send text input to the remote device.
     */
    suspend fun remoteInputText(text: String): RemoteCommandResult {
        val escaped = text.replace(" ", "%s").replace("'", "\\'")
        return remoteShell("input text '$escaped'")
    }

    /**
     * Push a file to the remote device.
     */
    suspend fun pushFile(localPath: String, remotePath: String): RemoteCommandResult {
        val target = currentDevice ?: return RemoteCommandResult(-1, "", "No device connected")
        return executeRootAdb("-s ${target.address} push $localPath $remotePath")
    }

    /**
     * Pull a file from the remote device.
     */
    suspend fun pullFile(remotePath: String, localPath: String): RemoteCommandResult {
        val target = currentDevice ?: return RemoteCommandResult(-1, "", "No device connected")
        return executeRootAdb("-s ${target.address} pull $remotePath $localPath")
    }

    /**
     * Install APK on the remote device.
     */
    suspend fun remoteInstallApk(apkPath: String): RemoteCommandResult {
        val target = currentDevice ?: return RemoteCommandResult(-1, "", "No device connected")
        return executeRootAdb("-s ${target.address} install -r $apkPath")
    }

    /**
     * Uninstall package from the remote device.
     */
    suspend fun remoteUninstall(packageName: String): RemoteCommandResult =
        remoteShell("pm uninstall $packageName")

    /**
     * List installed packages on the remote device.
     */
    suspend fun remoteListPackages(includeSystem: Boolean = false): List<String> {
        val flag = if (includeSystem) "" else "-3"
        val result = remoteShell("pm list packages $flag")
        return if (result.success) {
            result.stdout.lines()
                .filter { it.startsWith("package:") }
                .map { it.removePrefix("package:").trim() }
                .sorted()
        } else emptyList()
    }

    /**
     * Get remote device detailed info.
     */
    suspend fun remoteGetProp(prop: String): String {
        val result = remoteShell("getprop $prop")
        return if (result.success) result.stdout.trim() else ""
    }

    /**
     * List files on the remote device.
     */
    suspend fun remoteListFiles(path: String): List<String> {
        val result = remoteShell("ls -la $path")
        return if (result.success) result.stdout.lines().filter { it.isNotBlank() } else emptyList()
    }

    /**
     * Get remote logcat stream (last N lines).
     */
    suspend fun remoteLogcat(lines: Int = 100, filter: String = ""): String {
        val cmd = if (filter.isNotEmpty()) {
            "logcat -d -t $lines | grep -i '$filter'"
        } else {
            "logcat -d -t $lines"
        }
        val result = remoteShell(cmd)
        return if (result.success) result.stdout else result.stderr
    }

    /**
     * Select which connected device to control.
     */
    fun selectDevice(deviceId: String) {
        currentDevice = connectedDevices.find { it.id == deviceId }
    }

    fun getCurrentDevice(): RemoteDevice? = currentDevice

    fun getConnectedDevices(): List<RemoteDevice> = connectedDevices.toList()

    /**
     * Scan local network for potential ADB devices (port 5555).
     */
    suspend fun scanNetwork(): List<String> = withContext(Dispatchers.IO) {
        val result = executeRootCommand(
            "ip route | grep -oP 'src \\K[0-9.]+' | head -1"
        )
        val localIp = result.stdout.trim()
        if (localIp.isEmpty()) return@withContext emptyList()

        val subnet = localIp.substringBeforeLast(".")
        val found = mutableListOf<String>()

        // Quick parallel ping sweep + port check
        val scanResult = executeRootCommand(
            "for i in \$(seq 1 254); do " +
            "(timeout 0.3 bash -c \"echo >/dev/tcp/$subnet/\$i/5555\" 2>/dev/null && echo \"$subnet.\$i\") & " +
            "done; wait"
        )

        if (scanResult.success) {
            found.addAll(scanResult.stdout.lines().filter { it.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")) })
        }
        found
    }

    private suspend fun refreshDeviceInfo(device: RemoteDevice) {
        val model = remoteGetProp("ro.product.model")
        val version = remoteGetProp("ro.build.version.release")
        val idx = connectedDevices.indexOfFirst { it.id == device.id }
        if (idx >= 0) {
            connectedDevices[idx] = device.copy(
                model = model.ifEmpty { device.model },
                androidVersion = version,
                name = model.ifEmpty { device.name }
            )
            if (currentDevice?.id == device.id) {
                currentDevice = connectedDevices[idx]
            }
        }
    }

    private suspend fun executeRootAdb(args: String): RemoteCommandResult =
        executeRootCommand("adb $args")

    private suspend fun executeRootCommand(command: String): RemoteCommandResult =
        withContext(Dispatchers.IO) {
            try {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
                val stdout = process.inputStream.bufferedReader().readText()
                val stderr = process.errorStream.bufferedReader().readText()
                val exitCode = process.waitFor()
                RemoteCommandResult(exitCode, stdout, stderr)
            } catch (e: Exception) {
                RemoteCommandResult(-1, "", e.message ?: "Unknown error")
            }
        }
}
