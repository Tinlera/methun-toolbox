package com.tinlera.toolbox.ui.screens.hacker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinlera.toolbox.core.RootBridge
import kotlinx.coroutines.launch

/**
 * Hacker Terminal — Matrix-style terminal with green text on black background.
 * Supports shell commands, logcat, dmesg, and quick shortcuts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackerTerminalScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var command by remember { mutableStateOf("") }
    var output by remember { mutableStateOf(buildString {
        appendLine("╔══════════════════════════════════════════╗")
        appendLine("║        METHUN HACKER TERMINAL v1.0       ║")
        appendLine("║     Root Shell • Logcat • System Info     ║")
        appendLine("╚══════════════════════════════════════════╝")
        appendLine()
        appendLine("[system] Terminal hazır. Komut girin.")
        appendLine("[system] 'help' yazarak komutları görün.")
        appendLine()
    }) }
    var useRoot by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    val helpText = """
        |[help] Kullanılabilir komutlar:
        |  logcat <n>      — Son n satır logcat
        |  dmesg            — Kernel mesajları
        |  ps               — Çalışan süreçler
        |  top              — CPU kullanımı
        |  netstat          — Ağ bağlantıları
        |  ifconfig         — Ağ arayüzleri
        |  getprop <key>    — Sistem özelliği oku
        |  dumpsys <servis> — Servis dump
        |  pm list packages — Paket listesi
        |  id               — Kullanıcı bilgisi
        |  uname -a         — Kernel bilgisi
        |  cat /proc/cpuinfo — CPU bilgisi
        |  free -h          — RAM kullanımı
        |  df -h            — Disk kullanımı
        |  clear            — Ekranı temizle
    """.trimMargin()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💀", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Hacker Terminal", fontSize = 16.sp, color = Color(0xFF00FF41))
                            Text(
                                if (useRoot) "root@localhost" else "shell@localhost",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF58A6FF)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Geri") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1117)),
                actions = {
                    // Root toggle
                    IconButton(onClick = { useRoot = !useRoot }) {
                        Icon(
                            Icons.Filled.AdminPanelSettings, "Root",
                            tint = if (useRoot) Color(0xFFFF6B6B) else Color.Gray
                        )
                    }
                    // Clear
                    IconButton(onClick = { output = "" }) {
                        Icon(Icons.Filled.DeleteSweep, "Temizle", tint = Color(0xFF58A6FF))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFF0D1117))) {
            // Terminal output
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = output,
                    color = Color(0xFF00FF41),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            // Auto-scroll to bottom
            LaunchedEffect(output) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }

            // Quick command chips
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val quickCmds = listOf("id", "uname -a", "logcat -d -t 50", "dmesg | tail", "ps -A | head -30", "df -h")
                quickCmds.forEach { cmd ->
                    SuggestionChip(
                        onClick = {
                            scope.launch {
                                val result = if (useRoot) RootBridge.execute(cmd).output else RootBridge.executeAsSh(cmd).output
                                output += "\n\u001b[31m➜\u001b[0m $cmd\n$result\n"
                            }
                        },
                        label = { Text(cmd.take(12), fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF161B22),
                            labelColor = Color(0xFF58A6FF)
                        )
                    )
                }
            }

            // Command input row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161B22))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (useRoot) "root#" else "$",
                    color = if (useRoot) Color(0xFFFF6B6B) else Color(0xFF00FF41),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = command,
                    onValueChange = { command = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Komut girin...", color = Color.Gray) },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF00FF41),
                        fontSize = 14.sp
                    ),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        cursorColor = Color(0xFF00FF41),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                IconButton(onClick = {
                    if (command.isNotBlank()) {
                        val cmd = command
                        command = ""
                        scope.launch {
                            when {
                                cmd == "clear" -> output = ""
                                cmd == "help" -> output += helpText + "\n"
                                cmd.startsWith("logcat") -> {
                                    val n = cmd.removePrefix("logcat").trim().toIntOrNull() ?: 50
                                    val result = if (useRoot) RootBridge.execute("logcat -d -t $n").output else RootBridge.executeAsSh("logcat -d -t $n").output
                                    output += "\n➜ $cmd\n$result\n"
                                }
                                else -> {
                                    val result = if (useRoot) RootBridge.execute(cmd).output else RootBridge.executeAsSh(cmd).output
                                    output += "\n➜ $cmd\n$result\n"
                                }
                            }
                        }
                    }
                }) {
                    Icon(Icons.Filled.PlayArrow, "Çalıştır", tint = Color(0xFF00FF41))
                }
            }
        }
    }
}
