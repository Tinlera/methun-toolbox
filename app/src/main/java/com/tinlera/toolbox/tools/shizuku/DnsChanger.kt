package com.tinlera.toolbox.tools.shizuku

import com.tinlera.toolbox.core.RootBridge
import com.tinlera.toolbox.core.ShellResult

data class DnsPreset(
    val name: String,
    val hostname: String,
    val description: String
)

object DnsChanger {

    val presets = listOf(
        DnsPreset("Cloudflare", "one.one.one.one", "Hızlı ve gizlilik odaklı"),
        DnsPreset("Google", "dns.google", "Google Public DNS"),
        DnsPreset("AdGuard", "dns.adguard-dns.com", "Reklam engellemeli"),
        DnsPreset("AdGuard Family", "family.adguard-dns.com", "Aile korumalı + reklam engellemeli"),
        DnsPreset("NextDNS", "dns.nextdns.io", "Özelleştirilebilir filtre"),
        DnsPreset("Quad9", "dns.quad9.net", "Güvenlik odaklı"),
        DnsPreset("Mullvad", "dns.mullvad.net", "Gizlilik odaklı, İsveç"),
    )

    suspend fun getCurrentDns(): String {
        val result = RootBridge.executeAsSh("settings get global private_dns_specifier")
        val mode = RootBridge.executeAsSh("settings get global private_dns_mode").output.trim()
        return when (mode) {
            "hostname" -> result.output.trim().ifBlank { "Ayarlanmamış" }
            "off" -> "Kapalı"
            "opportunistic" -> "Otomatik"
            else -> mode
        }
    }

    suspend fun getDnsMode(): String {
        return RootBridge.executeAsSh("settings get global private_dns_mode").output.trim()
    }

    suspend fun setPrivateDns(hostname: String): ShellResult {
        RootBridge.execute("settings put global private_dns_mode hostname")
        return RootBridge.execute("settings put global private_dns_specifier $hostname")
    }

    suspend fun setCustomDns(hostname: String): ShellResult = setPrivateDns(hostname)

    suspend fun disableDns(): ShellResult {
        return RootBridge.execute("settings put global private_dns_mode off")
    }

    suspend fun setAutoDns(): ShellResult {
        return RootBridge.execute("settings put global private_dns_mode opportunistic")
    }
}
