package com.tinlera.toolbox.ui.screens

import androidx.compose.animation.AnimatedVisibility
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

data class ToolItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val requiresRoot: Boolean = false,
    val route: String? = null,
    val onClick: () -> Unit = {}
)

data class ToolCategory(
    val name: String,
    val icon: ImageVector,
    val tools: List<ToolItem>
)

@Composable
fun ToolsScreen(onNavigate: (String) -> Unit = {}) {
    val categories = remember {
        listOf(
            ToolCategory(
                name = "📦 Uygulama Yönetimi",
                icon = Icons.Filled.Apps,
                tools = listOf(
                    ToolItem("Debloat Manager", "Sistem uygulamalarını kaldır/devre dışı bırak", Icons.Filled.DeleteSweep, route = "tool/debloat"),
                    ToolItem("İzin Yöneticisi", "AppOps kontrolü", Icons.Filled.Security),
                    ToolItem("Sessiz APK Yükleyici", "Onaysız APK yükleme", Icons.Filled.InstallMobile),
                    ToolItem("Cache Temizleyici", "Toplu cache temizleme", Icons.Filled.CleaningServices),
                )
            ),
            ToolCategory(
                name = "⚡ Performans",
                icon = Icons.Filled.Speed,
                tools = listOf(
                    ToolItem("ART Optimizer", "DEX derleyici optimizasyonu", Icons.Filled.Speed, route = "tool/art"),
                    ToolItem("CPU/GPU Governor", "Frekans ve governor kontrolü", Icons.Filled.Memory, requiresRoot = true),
                    ToolItem("Termal Kontrol", "Isı yönetimi", Icons.Filled.Thermostat, requiresRoot = true),
                    ToolItem("Doze Manager", "Agresif pil tasarrufu", Icons.Filled.BatteryChargingFull),
                )
            ),
            ToolCategory(
                name = "🌐 Ağ",
                icon = Icons.Filled.Wifi,
                tools = listOf(
                    ToolItem("DNS Değiştirici", "Private DNS ayarı", Icons.Filled.Dns, route = "tool/dns"),
                    ToolItem("hosts Editörü", "Reklam engelleme / hosts düzenleme", Icons.Filled.Block, requiresRoot = true),
                )
            ),
            ToolCategory(
                name = "🖥️ Ekran",
                icon = Icons.Filled.Smartphone,
                tools = listOf(
                    ToolItem("DPI / Çözünürlük", "Ekran yoğunluğu ve çözünürlük", Icons.Filled.AspectRatio),
                    ToolItem("Refresh Rate", "Yenileme hızı", Icons.Filled.Refresh),
                )
            ),
            ToolCategory(
                name = "🔧 Sistem",
                icon = Icons.Filled.Settings,
                tools = listOf(
                    ToolItem("build.prop Editörü", "Sistem özelliklerini düzenle", Icons.Filled.Edit, requiresRoot = true, route = "tool/buildprop"),
                    ToolItem("SELinux Toggle", "Enforcing/Permissive geçiş", Icons.Filled.Shield, requiresRoot = true),
                    ToolItem("Partition Bilgisi", "Mount durumu ve boyutlar", Icons.Filled.Storage, requiresRoot = true),
                    ToolItem("Magisk/KSU Modülleri", "Modül yönetimi", Icons.Filled.Extension, requiresRoot = true, route = "tool/modules"),
                    ToolItem("System App Yönetimi", "Sistem uygulaması yükle/kaldır", Icons.Filled.AppSettingsAlt, requiresRoot = true),
                    ToolItem("Gizli Ayar Kısayolları", "Gizli ayar sayfalarına erişim", Icons.Filled.SettingsApplications, route = "tool/intents"),
                )
            ),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "🛠️ Araçlar",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Shizuku ve Root araçları",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        categories.forEach { category ->
            ToolCategoryCard(category, onNavigate)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ToolCategoryCard(category: ToolCategory, onNavigate: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        category.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${category.tools.size} araç",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Kapat" else "Aç"
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    category.tools.forEach { tool ->
                        ToolItemRow(tool, onNavigate)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolItemRow(tool: ToolItem, onNavigate: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        onClick = { tool.route?.let { onNavigate(it) } }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                tool.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (tool.requiresRoot) MaterialTheme.colorScheme.error
                       else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (tool.requiresRoot) {
                AssistChip(
                    onClick = {},
                    label = { Text("Root", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.height(24.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }
        }
    }
}
