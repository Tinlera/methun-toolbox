package com.tinlera.toolbox.ui.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinlera.toolbox.tools.shizuku.DnsChanger
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnsScreen(onBack: () -> Unit) {
    var currentDns by remember { mutableStateOf("Yükleniyor...") }
    var dnsMode by remember { mutableStateOf("") }
    var customDns by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { currentDns = DnsChanger.getCurrentDns(); dnsMode = DnsChanger.getDnsMode() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("🌐 DNS Değiştirici") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Geri") } }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Aktif DNS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(currentDns, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    Text("Mod: $dnsMode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Hazır DNS Sunucuları", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            DnsChanger.presets.forEach { preset ->
                ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = {
                    scope.launch {
                        DnsChanger.setPrivateDns(preset.hostname); currentDns = preset.hostname; dnsMode = "hostname"
                        snackbarHostState.showSnackbar("✅ ${preset.name} ayarlandı")
                    }
                }) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Dns, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(preset.name, fontWeight = FontWeight.Medium)
                            Text(preset.hostname, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(preset.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (currentDns == preset.hostname) Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Özel DNS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = customDns, onValueChange = { customDns = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("dns.example.com") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        if (customDns.isNotBlank()) scope.launch {
                            DnsChanger.setCustomDns(customDns); currentDns = customDns; dnsMode = "hostname"
                            snackbarHostState.showSnackbar("✅ Özel DNS ayarlandı")
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Uygula") }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { scope.launch { DnsChanger.setAutoDns(); currentDns = "Otomatik"; dnsMode = "opportunistic" } }, modifier = Modifier.weight(1f)) { Text("Otomatik") }
                OutlinedButton(onClick = { scope.launch { DnsChanger.disableDns(); currentDns = "Kapalı"; dnsMode = "off" } }, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Kapat") }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}
