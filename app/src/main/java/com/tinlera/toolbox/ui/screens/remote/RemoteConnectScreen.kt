package com.tinlera.toolbox.ui.screens.remote

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinlera.toolbox.remote.AdbClient
import com.tinlera.toolbox.remote.ConnectionType
import com.tinlera.toolbox.remote.RemoteDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteConnectScreen(onBack: () -> Unit, onConnected: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val adbClient = remember { AdbClient(context) }

    var adbAvailable by remember { mutableStateOf<Boolean?>(null) }
    var devices by remember { mutableStateOf<List<RemoteDevice>>(emptyList()) }
    var scanning by remember { mutableStateOf(false) }
    var wifiIp by remember { mutableStateOf("") }
    var wifiPort by remember { mutableStateOf("5555") }
    var statusMessage by remember { mutableStateOf("") }
    var isConnecting by remember { mutableStateOf(false) }
    var scanResults by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        adbAvailable = adbClient.isAdbAvailable()
        if (adbAvailable == true) {
            adbClient.startAdbServer()
            devices = adbClient.listDevices()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📱 Remote Connect") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Geri")
                    }
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
            // ADB Status
            when (adbAvailable) {
                null -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("ADB kontrol ediliyor...")
                }
                false -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("ADB bulunamadı", fontWeight = FontWeight.Bold)
                                Text("Root erişimi ile ADB binary gerekli", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                true -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1B5E20).copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50))
                            Spacer(Modifier.width(12.dp))
                            Text("ADB hazır", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // WiFi ADB Connection
            Text(
                "🌐 WiFi ADB Bağlantısı",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = wifiIp,
                onValueChange = { wifiIp = it },
                label = { Text("IP Adresi") },
                placeholder = { Text("192.168.1.100") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = wifiPort,
                    onValueChange = { wifiPort = it },
                    label = { Text("Port") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        if (wifiIp.isNotBlank()) {
                            isConnecting = true
                            scope.launch {
                                val result = adbClient.connectWifi(wifiIp, wifiPort.toIntOrNull() ?: 5555)
                                statusMessage = if (result.stdout.contains("connected")) {
                                    "✅ Bağlantı başarılı!"
                                } else {
                                    "❌ ${result.stdout} ${result.stderr}"
                                }
                                devices = adbClient.listDevices()
                                isConnecting = false
                                if (result.stdout.contains("connected")) {
                                    delay(500)
                                    onConnected()
                                }
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically),
                    enabled = wifiIp.isNotBlank() && !isConnecting && adbAvailable == true
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Link, null)
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("Bağlan")
                }
            }

            // Network Scan
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    scanning = true
                    scope.launch {
                        scanResults = adbClient.scanNetwork()
                        scanning = false
                        if (scanResults.isEmpty()) {
                            statusMessage = "Ağda ADB cihazı bulunamadı"
                        }
                    }
                },
                enabled = !scanning && adbAvailable == true,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (scanning) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Ağ taranıyor...")
                } else {
                    Icon(Icons.Filled.WifiFind, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ağı Tara (Port 5555)")
                }
            }

            // Scan Results
            if (scanResults.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("🔍 Bulunan Cihazlar:", fontWeight = FontWeight.Bold)
                scanResults.forEach { ip ->
                    Surface(
                        onClick = {
                            wifiIp = ip
                            scope.launch {
                                isConnecting = true
                                val result = adbClient.connectWifi(ip)
                                statusMessage = if (result.stdout.contains("connected")) "✅ $ip bağlandı!" else "❌ Bağlantı hatası"
                                devices = adbClient.listDevices()
                                isConnecting = false
                                if (result.stdout.contains("connected")) {
                                    delay(500)
                                    onConnected()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PhoneAndroid, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(ip, fontFamily = FontFamily.Monospace)
                            Spacer(Modifier.weight(1f))
                            Text("Bağlan →", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        }
                    }
                }
            }

            // USB OTG Section
            Spacer(Modifier.height(24.dp))
            Text(
                "🔌 USB OTG Bağlantısı",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            val usbDevices = remember { adbClient.getUsbDevices() }
            if (usbDevices.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.UsbOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("USB cihaz algılanmadı")
                            Text(
                                "OTG kablo ile hedef telefonu bağlayın\nHedef telefonda USB Debugging açık olmalı",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                usbDevices.forEach { usb ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Usb, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("USB: ${usb.productName ?: "Bilinmeyen"}", fontWeight = FontWeight.Bold)
                                Text("VID:${usb.vendorId} PID:${usb.productId}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Connected Devices
            if (devices.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "✅ Bağlı Cihazlar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                devices.forEach { device ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20).copy(alpha = 0.15f)),
                        onClick = {
                            adbClient.selectDevice(device.id)
                            onConnected()
                        }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (device.connectionType == ConnectionType.WIFI_ADB) Icons.Filled.Wifi else Icons.Filled.Usb,
                                null, tint = Color(0xFF4CAF50)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(device.model, fontWeight = FontWeight.Bold)
                                Text(device.address, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                if (device.androidVersion.isNotEmpty()) {
                                    Text("Android ${device.androidVersion}", fontSize = 12.sp)
                                }
                            }
                            Icon(Icons.Filled.ChevronRight, null)
                        }
                    }
                }
            }

            // Status
            if (statusMessage.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    statusMessage,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = if (statusMessage.startsWith("✅")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }

            // Instructions
            Spacer(Modifier.height(24.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📋 Nasıl Bağlanılır?", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "WiFi ADB:\n" +
                        "1. Hedef telefonda: Ayarlar → Geliştirici Seçenekleri → Kablosuz Hata Ayıklama\n" +
                        "2. Veya PC'den: adb tcpip 5555\n" +
                        "3. Hedef telefonun IP adresini girin ve Bağlan'a basın\n\n" +
                        "USB OTG:\n" +
                        "1. Hedef telefonda USB Debugging açın\n" +
                        "2. OTG kablo ile iki telefonu bağlayın\n" +
                        "3. Hedef telefonda 'Bu bilgisayara güven' onayı verin",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
