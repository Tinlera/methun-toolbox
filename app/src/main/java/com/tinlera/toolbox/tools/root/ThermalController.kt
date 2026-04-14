package com.tinlera.toolbox.tools.root

import com.tinlera.toolbox.core.RootBridge

data class ThermalInfo(
    val zone: String,
    val type: String,
    val temperature: Float
)

object ThermalController {

    suspend fun getThermalZones(): List<ThermalInfo> {
        val zones = mutableListOf<ThermalInfo>()
        val result = RootBridge.execute("ls /sys/class/thermal/ | grep thermal_zone")
        val zoneNames = result.output.lines().filter { it.isNotBlank() }

        for (zone in zoneNames) {
            val type = RootBridge.execute("cat /sys/class/thermal/$zone/type").output.trim()
            val temp = RootBridge.execute("cat /sys/class/thermal/$zone/temp").output.trim()
            val tempC = (temp.toFloatOrNull() ?: 0f) / 1000f
            zones.add(ThermalInfo(zone, type, tempC))
        }

        return zones.sortedByDescending { it.temperature }
    }

    suspend fun getCpuTemp(): Float {
        val zones = getThermalZones()
        return zones.firstOrNull { it.type.contains("cpu", ignoreCase = true) }?.temperature ?: 0f
    }

    suspend fun getBatteryTemp(): Float {
        val result = RootBridge.execute("cat /sys/class/power_supply/battery/temp")
        return (result.output.trim().toFloatOrNull() ?: 0f) / 10f
    }

    suspend fun getThermalPolicy(): String {
        val result = RootBridge.execute("cat /sys/class/thermal/thermal_zone0/policy 2>/dev/null")
        return result.output.trim().ifBlank { "unknown" }
    }

    suspend fun listThermalPolicies(): List<String> {
        val result = RootBridge.execute("cat /sys/class/thermal/thermal_zone0/available_policies 2>/dev/null")
        return result.output.trim().split(" ").filter { it.isNotBlank() }
    }
}
