package com.tinlera.toolbox.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinlera.toolbox.core.RootBridge
import com.tinlera.toolbox.core.ShizukuManager
import kotlinx.coroutines.launch

data class TweakItem(
    val name: String,
    val description: String,
    val currentValue: String = "",
    val type: TweakType = TweakType.TOGGLE
)

enum class TweakType { TOGGLE, SLIDER, DROPDOWN }

@Composable
fun TweaksScreen() {
    val scope = rememberCoroutineScope()
    var animScale by remember { mutableStateOf("1.0") }
    var adbEnabled by remember { mutableStateOf(false) }
    var screenTimeout by remember { mutableStateOf("60000") }

    LaunchedEffect(Unit) {
        scope.launch {
            val anim = RootBridge.executeAsSh("settings get global window_animation_scale").output
            animScale = anim.ifBlank { "1.0" }
            val adb = RootBridge.executeAsSh("settings get global adb_enabled").output
            adbEnabled = adb.trim() == "1"
            val timeout = RootBridge.executeAsSh("settings get system screen_off_timeout").output
            screenTimeout = timeout.ifBlank { "60000" }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "🎛️ Tweaks",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Hızlı sistem ayarları",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Animation Speed
        TweakCard(
            title = "⚡ Animasyon Hızı",
            description = "Window, Transition, Animator süresi",
            icon = Icons.Filled.Animation
        ) {
            val options = listOf("Kapalı (0x)" to "0.0", "Hızlı (0.5x)" to "0.5", "Normal (1x)" to "1.0", "Yavaş (2x)" to "2.0")
            options.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = animScale == value,
                        onClick = {
                            animScale = value
                            scope.launch {
                                RootBridge.execute("settings put global window_animation_scale $value")
                                RootBridge.execute("settings put global transition_animation_scale $value")
                                RootBridge.execute("settings put global animator_duration_scale $value")
                            }
                        }
                    )
                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ADB Toggle
        TweakCard(
            title = "🔌 USB Hata Ayıklama",
            description = "ADB bağlantısını aç/kapat",
            icon = Icons.Filled.Usb
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (adbEnabled) "Açık" else "Kapalı",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = adbEnabled,
                    onCheckedChange = { enabled ->
                        adbEnabled = enabled
                        scope.launch {
                            val v = if (enabled) "1" else "0"
                            RootBridge.execute("settings put global adb_enabled $v")
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Screen Timeout
        TweakCard(
            title = "🖥️ Ekran Zaman Aşımı",
            description = "Ekran kapanma süresi",
            icon = Icons.Filled.Timer
        ) {
            val options = listOf(
                "15 saniye" to "15000",
                "30 saniye" to "30000",
                "1 dakika" to "60000",
                "2 dakika" to "120000",
                "5 dakika" to "300000",
                "10 dakika" to "600000",
                "30 dakika" to "1800000",
            )
            options.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = screenTimeout == value,
                        onClick = {
                            screenTimeout = value
                            scope.launch {
                                RootBridge.execute("settings put system screen_off_timeout $value")
                            }
                        }
                    )
                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Mode
        TweakCard(
            title = "📱 Navigasyon Modu",
            description = "Gesture veya 3-buton navigasyon",
            icon = Icons.Filled.SwipeUp
        ) {
            var navMode by remember { mutableStateOf(2) }
            LaunchedEffect(Unit) {
                val result = RootBridge.executeAsSh("settings get secure navigation_mode").output
                navMode = result.trim().toIntOrNull() ?: 2
            }
            val modes = listOf("3-Buton" to 0, "2-Buton" to 1, "Gesture" to 2)
            modes.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = navMode == value,
                        onClick = {
                            navMode = value
                            scope.launch {
                                RootBridge.execute("cmd overlay enable com.android.internal.systemui.navbar.gestural")
                                RootBridge.execute("settings put secure navigation_mode $value")
                            }
                        }
                    )
                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Font Scale
        TweakCard(
            title = "🔤 Font Boyutu",
            description = "Sistem font ölçeği",
            icon = Icons.Filled.TextFields
        ) {
            var fontScale by remember { mutableStateOf(1.0f) }
            LaunchedEffect(Unit) {
                val result = RootBridge.executeAsSh("settings get system font_scale").output
                fontScale = result.trim().toFloatOrNull() ?: 1.0f
            }
            Text("%.0f%%".format(fontScale * 100), style = MaterialTheme.typography.titleMedium)
            Slider(
                value = fontScale,
                onValueChange = { fontScale = it },
                valueRange = 0.8f..1.5f,
                steps = 6,
                onValueChangeFinished = {
                    scope.launch {
                        RootBridge.execute("settings put system font_scale $fontScale")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun TweakCard(
    title: String,
    description: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
