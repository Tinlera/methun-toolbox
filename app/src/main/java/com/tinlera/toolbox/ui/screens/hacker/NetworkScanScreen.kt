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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinlera.toolbox.tools.hacker.NetworkDevice
import com.tinlera.toolbox.tools.hacker.NetworkScanner
import com.tinlera.toolbox.tools.hacker.PortResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScanScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var localIp by remember { mutableStateOf("...") }
    var arpDevices by remember { mutableStateOf<List<NetworkDevice>>(emptyList()) }
    var liveHosts by remember { mutableStateOf<List<String>>(emptyList()) }
    var portResults by remember { mutableStateOf<List<PortResult>>(emptyList()) }
    var scanTarget by remember { mutableStateOf("") }
    var scanning by remember { mutableStateOf(false) }
    var activeConnections by remember { mutableStateOf("") }
    var wifiInfo by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var statusText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        localIp = NetworkScanner.getLocalIp()
        arpDevices = NetworkScanner.getArpTable()
        wifiInfo = NetworkScanner.getWifiInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔍 Ağ Tarayıcı") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Geri") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Local IP info
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📡 Yerel Ağ", color = Color(0xFF00FF41), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("IP: $localIp", color = Color(0xFF00FF41), fontFamily = FontFamily.Monospace)
                    wifiInfo.forEach { (key, value) ->
                        Text("$key: $value", color = Color(0xFF58A6FF), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Ping Sweep
            Button(
                onClick = {
                    scanning = true
                    statusText = "Ağ taranıyor..."
                    scope.launch {
                        val subnet = localIp.substringBeforeLast(".")
                        liveHosts = NetworkScanner.pingSweep(subnet)
                        arpDevices = NetworkScanner.getArpTable()
                        statusText = "${liveHosts.size} aktif cihaz bulundu"
                        scanning = false
                    }
                },
                enabled = !scanning && localIp != "...",
                modifier = Modifier.fillMaxWidth()
            ) {
                if (scanning) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                }
                Icon(Icons.Filled.Radar, null)
                Spacer(Modifier.width(8.dp))
                Text("Ağı Tara (Ping Sweep)")
            }

            if (statusText.isNotEmpty()) {
                Text(statusText, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(vertical = 4.dp))
            }

            // ARP Table / Discovered Devices
            if (arpDevices.isNotEmpty() || liveHosts.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("📋 Bulunan Cihazlar (${arpDevices.size})", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                arpDevices.forEach { device ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                        onClick = { scanTarget = device.ip }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Devices, null, modifier = Modifier.size(20.dp), tint = Color(0xFF4CAF50))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(device.ip, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                if (device.mac.isNotEmpty()) {
                                    Text("MAC: ${device.mac}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text("Tara →", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Port Scanner
            Spacer(Modifier.height(20.dp))
            Text("🔓 Port Tarayıcı", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = scanTarget,
                    onValueChange = { scanTarget = it },
                    label = { Text("Hedef IP") },
                    placeholder = { Text("192.168.1.1") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                )
                Button(
                    onClick = {
                        scanning = true
                        scope.launch {
                            portResults = NetworkScanner.scanPorts(scanTarget)
                            scanning = false
                        }
                    },
                    enabled = scanTarget.isNotBlank() && !scanning,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("Tara")
                }
            }

            // Port Results
            if (portResults.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                val openPorts = portResults.filter { it.isOpen }

                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${openPorts.size} açık port / ${portResults.size} taranan", color = Color(0xFF00FF41), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        openPorts.forEach { port ->
                            Row {
                                Text(
                                    "${port.port}",
                                    color = Color(0xFFFF6B6B),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(60.dp)
                                )
                                Text(
                                    "OPEN",
                                    color = Color(0xFF00FF41),
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.width(60.dp)
                                )
                                Text(
                                    port.service,
                                    color = Color(0xFF58A6FF),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        if (openPorts.isEmpty()) {
                            Text("Açık port bulunamadı", color = Color.Gray, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Active Connections
            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = {
                    scope.launch {
                        activeConnections = NetworkScanner.getConnections()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Cable, null)
                Spacer(Modifier.width(8.dp))
                Text("Aktif Bağlantıları Göster")
            }

            if (activeConnections.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0D1117))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(activeConnections, color = Color(0xFF00FF41), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                }
            }
        }
    }
}
