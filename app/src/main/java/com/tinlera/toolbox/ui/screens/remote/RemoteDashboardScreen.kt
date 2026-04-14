package com.tinlera.toolbox.ui.screens.remote

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinlera.toolbox.remote.AdbClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteDashboardScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val adbClient = remember { AdbClient(context) }
    val device = adbClient.getCurrentDevice()

    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("📱 Ekran", "💻 Shell", "📂 Dosyalar", "📦 Uygulamalar", "ℹ️ Bilgi")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Remote Control", fontSize = 18.sp)
                        Text(
                            device?.let { "${it.model} (${it.address})" } ?: "Bağlı değil",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            adbClient.disconnectWifi(device?.address?.substringBefore(":") ?: "", device?.address?.substringAfter(":")?.toIntOrNull() ?: 5555)
                            onBack()
                        }
                    }) {
                        Icon(Icons.Filled.LinkOff, "Bağlantıyı Kes", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tab Row
            ScrollableTabRow(selectedTabIndex = activeTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = activeTab == index, onClick = { activeTab = index }) {
                        Text(title, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                    }
                }
            }

            // Tab Content
            when (activeTab) {
                0 -> ScreenMirrorTab(adbClient)
                1 -> RemoteShellTab(adbClient)
                2 -> RemoteFilesTab(adbClient)
                3 -> RemoteAppsTab(adbClient)
                4 -> RemoteInfoTab(adbClient)
            }
        }
    }
}

@Composable
fun ScreenMirrorTab(adbClient: AdbClient) {
    val scope = rememberCoroutineScope()
    var screenshotBytes by remember { mutableStateOf<ByteArray?>(null) }
    var isStreaming by remember { mutableStateOf(false) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var remoteWidth by remember { mutableIntStateOf(1080) }
    var remoteHeight by remember { mutableIntStateOf(2400) }

    // Get remote screen size
    LaunchedEffect(Unit) {
        val result = adbClient.remoteShell("wm size")
        if (result.success) {
            val match = Regex("(\\d+)x(\\d+)").find(result.stdout)
            if (match != null) {
                remoteWidth = match.groupValues[1].toInt()
                remoteHeight = match.groupValues[2].toInt()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                isStreaming = !isStreaming
                if (isStreaming) {
                    scope.launch {
                        while (isStreaming && isActive) {
                            screenshotBytes = adbClient.remoteScreencap()
                            delay(500) // ~2 FPS
                        }
                    }
                }
            }) {
                Icon(if (isStreaming) Icons.Filled.Stop else Icons.Filled.PlayArrow, null)
                Spacer(Modifier.width(4.dp))
                Text(if (isStreaming) "Durdur" else "Canlı İzle")
            }

            OutlinedButton(onClick = {
                scope.launch { screenshotBytes = adbClient.remoteScreencap() }
            }) {
                Icon(Icons.Filled.Screenshot, null)
                Spacer(Modifier.width(4.dp))
                Text("Ekran Görüntüsü")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilledTonalButton(onClick = { scope.launch { adbClient.remoteKeyEvent(4) } }) {
                Icon(Icons.Filled.ArrowBack, "Back")
            }
            FilledTonalButton(onClick = { scope.launch { adbClient.remoteKeyEvent(3) } }) {
                Icon(Icons.Filled.Home, "Home")
            }
            FilledTonalButton(onClick = { scope.launch { adbClient.remoteKeyEvent(187) } }) {
                Icon(Icons.Filled.Layers, "Recents")
            }
            FilledTonalButton(onClick = { scope.launch { adbClient.remoteKeyEvent(26) } }) {
                Icon(Icons.Filled.Power, "Power")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Screen display with touch support
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .background(Color.Black)
                .onSizeChanged { imageSize = it }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (imageSize.width > 0 && imageSize.height > 0) {
                            val x = (offset.x / imageSize.width * remoteWidth).toInt()
                            val y = (offset.y / imageSize.height * remoteHeight).toInt()
                            scope.launch { adbClient.remoteTap(x, y) }
                            // Auto refresh after tap
                            scope.launch {
                                delay(300)
                                screenshotBytes = adbClient.remoteScreencap()
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val bytes = screenshotBytes
            if (bytes != null) {
                val bitmap = remember(bytes) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Remote Screen",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.ScreenshotMonitor, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Text("Ekran görüntüsü almak için butona basın", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun RemoteShellTab(adbClient: AdbClient) {
    val scope = rememberCoroutineScope()
    var command by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("$ Komut girin ve çalıştırın...\n") }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        // Terminal output
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0D1117))
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = output,
                color = Color(0xFF00FF41), // Matrix green
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        // Quick commands
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("ls", "top -n1", "df -h", "ps -A", "getprop").forEach { cmd ->
                SuggestionChip(
                    onClick = {
                        scope.launch {
                            output += "\n\$ $cmd\n"
                            val result = adbClient.remoteShell(cmd)
                            output += if (result.success) result.stdout else "Error: ${result.stderr}"
                        }
                    },
                    label = { Text(cmd, fontSize = 11.sp) }
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Command input
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("$", color = Color(0xFF00FF41), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Komut girin...") },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {
                if (command.isNotBlank()) {
                    val cmd = command
                    command = ""
                    scope.launch {
                        output += "\n\$ $cmd\n"
                        val result = adbClient.remoteShell(cmd)
                        output += if (result.success) result.stdout else "Error: ${result.stderr}"
                    }
                }
            }) {
                Icon(Icons.Filled.Send, "Çalıştır", tint = Color(0xFF00FF41))
            }
        }
    }
}

@Composable
fun RemoteFilesTab(adbClient: AdbClient) {
    val scope = rememberCoroutineScope()
    var currentPath by remember { mutableStateOf("/sdcard") }
    var files by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(currentPath) {
        loading = true
        files = adbClient.remoteListFiles(currentPath)
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        // Path bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Folder, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(currentPath, fontFamily = FontFamily.Monospace, fontSize = 14.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    if (currentPath != "/") {
                        currentPath = currentPath.substringBeforeLast("/").ifEmpty { "/" }
                    }
                }) {
                    Icon(Icons.Filled.ArrowUpward, "Üst Dizin")
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            files.forEach { line ->
                if (line.isBlank()) return@forEach
                Surface(
                    onClick = {
                        // If it looks like a directory (starts with 'd')
                        val name = line.split("\\s+".toRegex()).lastOrNull() ?: return@Surface
                        if (name == "." || name == "..") return@Surface
                        if (line.startsWith("d")) {
                            currentPath = "$currentPath/$name".replace("//", "/")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (line.startsWith("d")) Icons.Filled.Folder
                            else if (line.startsWith("l")) Icons.Filled.Link
                            else Icons.Filled.InsertDriveFile,
                            null,
                            modifier = Modifier.size(20.dp),
                            tint = if (line.startsWith("d")) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            line,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun RemoteAppsTab(adbClient: AdbClient) {
    val scope = rememberCoroutineScope()
    var packages by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var includeSystem by remember { mutableStateOf(false) }

    LaunchedEffect(includeSystem) {
        loading = true
        packages = adbClient.remoteListPackages(includeSystem)
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${packages.size} uygulama",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text("Sistem", fontSize = 12.sp)
            Switch(checked = includeSystem, onCheckedChange = { includeSystem = it })
        }

        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Spacer(Modifier.height(8.dp))

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            packages.forEach { pkg ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent
                ) {
                    Row(modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Android, null, modifier = Modifier.size(20.dp), tint = Color(0xFF4CAF50))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            pkg,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                scope.launch { adbClient.remoteUninstall(pkg) }
                                packages = packages.filter { it != pkg }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Filled.Delete, "Kaldır", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun RemoteInfoTab(adbClient: AdbClient) {
    var info by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val props = mapOf(
            "Model" to "ro.product.model",
            "Marka" to "ro.product.brand",
            "Cihaz" to "ro.product.device",
            "Android" to "ro.build.version.release",
            "SDK" to "ro.build.version.sdk",
            "Güvenlik Yaması" to "ro.build.version.security_patch",
            "Build" to "ro.build.display.id",
            "Parmak İzi" to "ro.build.fingerprint",
            "Bootloader" to "ro.bootloader",
            "Donanım" to "ro.hardware",
            "CPU ABI" to "ro.product.cpu.abi",
            "Seri No" to "ro.serialno"
        )
        val result = mutableMapOf<String, String>()
        props.forEach { (label, prop) ->
            result[label] = adbClient.remoteGetProp(prop)
        }

        // Battery info
        val battResult = adbClient.remoteShell("dumpsys battery")
        if (battResult.success) {
            val level = Regex("level: (\\d+)").find(battResult.stdout)?.groupValues?.get(1)
            val temp = Regex("temperature: (\\d+)").find(battResult.stdout)?.groupValues?.get(1)
            if (level != null) result["Batarya"] = "$level%"
            if (temp != null) result["Batarya Sıcaklığı"] = "${temp.toInt() / 10.0}°C"
        }

        // Storage
        val dfResult = adbClient.remoteShell("df -h /data | tail -1")
        if (dfResult.success) result["Depolama (/data)"] = dfResult.stdout.trim()

        // IP
        val ipResult = adbClient.remoteShell("ip route | grep -oP 'src \\K[0-9.]+'")
        if (ipResult.success) result["IP Adresi"] = ipResult.stdout.trim()

        // Uptime
        val uptimeResult = adbClient.remoteShell("uptime -p")
        if (uptimeResult.success) result["Çalışma Süresi"] = uptimeResult.stdout.trim()

        info = result
        loading = false
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text("📊 Cihaz Bilgileri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            info.forEach { (label, value) ->
                if (value.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(
                            label,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(140.dp),
                            fontSize = 14.sp
                        )
                        Text(
                            value,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
