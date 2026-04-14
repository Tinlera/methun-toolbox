package com.tinlera.toolbox.tools.shizuku

import android.content.Intent
import android.net.Uri
import android.provider.Settings

data class IntentShortcut(
    val name: String,
    val description: String,
    val action: String,
    val extra: String? = null,
    val uri: String? = null
)

object IntentManager {

    val shortcuts = listOf(
        IntentShortcut("Geliştirici Seçenekleri", "Developer options", "android.settings.APPLICATION_DEVELOPMENT_SETTINGS"),
        IntentShortcut("Pil Optimizasyonu", "Battery optimization", "android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS"),
        IntentShortcut("Kullanım İstatistikleri", "Data usage", "android.settings.DATA_USAGE_SETTINGS"),
        IntentShortcut("Uygulama Bilgisi", "Tüm uygulamalar", "android.settings.APPLICATION_SETTINGS"),
        IntentShortcut("Erişilebilirlik", "Accessibility", "android.settings.ACCESSIBILITY_SETTINGS"),
        IntentShortcut("Tarih ve Saat", "Date & time", "android.settings.DATE_SETTINGS"),
        IntentShortcut("Ekran Ayarları", "Display settings", "android.settings.DISPLAY_SETTINGS"),
        IntentShortcut("Ses Ayarları", "Sound settings", "android.settings.SOUND_SETTINGS"),
        IntentShortcut("Bağlantı Ayarları", "Connectivity", "android.settings.WIRELESS_SETTINGS"),
        IntentShortcut("Konum Ayarları", "Location", "android.settings.LOCATION_SOURCE_SETTINGS"),
        IntentShortcut("Güvenlik Ayarları", "Security", "android.settings.SECURITY_SETTINGS"),
        IntentShortcut("Depolama", "Storage", "android.settings.INTERNAL_STORAGE_SETTINGS"),
        IntentShortcut("SIM Kart", "SIM settings", "android.settings.NETWORK_OPERATOR_SETTINGS"),
        IntentShortcut("NFC Ayarları", "NFC", "android.settings.NFC_SETTINGS"),
        IntentShortcut("VPN Ayarları", "VPN", "android.settings.VPN_SETTINGS"),
        IntentShortcut("Gizli Test Menüsü", "*#*#4636#*#*", "android.intent.action.DIAL", uri = "tel:*#*#4636#*#*"),
        IntentShortcut("Cihaz Hakkında", "About phone", "android.settings.DEVICE_INFO_SETTINGS"),
        IntentShortcut("Input Method", "Klavye ayarları", "android.settings.INPUT_METHOD_SETTINGS"),
    )

    fun createIntent(shortcut: IntentShortcut): Intent {
        return if (shortcut.uri != null) {
            Intent(shortcut.action, Uri.parse(shortcut.uri))
        } else {
            Intent(shortcut.action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }
}
