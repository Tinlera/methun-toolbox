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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinlera.toolbox.core.DeviceInfo
import com.tinlera.toolbox.core.DeviceInfoProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var deviceInfo by remember { mutableStateOf<DeviceInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            deviceInfo = DeviceInfoProvider.collect()
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Methun Toolbox",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Android Araç Kutusu",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Device Info Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                deviceInfo?.let { info ->
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📱 Cihaz Bilgisi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow("Model", "${info.brand} ${info.model}")
                        InfoRow("Codename", info.device)
                        InfoRow("Android", "${info.androidVersion} (SDK ${info.sdkLevel})")
                        InfoRow("Güvenlik Yaması", info.securityPatch)
                        InfoRow("Kernel", info.kernel)
                        InfoRow("CPU", info.cpuAbi)
                        InfoRow("RAM", info.totalRam)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        InfoRow("Root", info.rootStatus)
                        InfoRow("Shizuku", info.shizukuStatus)
                        InfoRow("SELinux", info.selinuxStatus)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Access Grid
        Text(
            text = "⚡ Hızlı Erişim",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        val quickItems = listOf(
            Triple("Debloat", Icons.Filled.DeleteSweep, "Bloatware temizle"),
            Triple("ART", Icons.Filled.Speed, "Dexopt optimize"),
            Triple("DNS", Icons.Filled.Dns, "DNS değiştir"),
            Triple("Termal", Icons.Filled.Thermostat, "Isı kontrolü"),
            Triple("Governor", Icons.Filled.Memory, "CPU/GPU kontrol"),
            Triple("build.prop", Icons.Filled.Edit, "Sistem özellikleri"),
        )

        quickItems.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (title, icon, desc) ->
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        onClick = { /* TODO: navigate */ }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = title,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                // Fill empty slots
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
