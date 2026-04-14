package com.tinlera.toolbox.ui.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinlera.toolbox.tools.shizuku.IntentManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntentsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        topBar = { TopAppBar(title = { Text("🔗 Gizli Ayarlar") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Geri") } }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Gizli ayar sayfalarına doğrudan erişim", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            IntentManager.shortcuts.forEach { shortcut ->
                ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = {
                    try { context.startActivity(IntentManager.createIntent(shortcut)) } catch (_: Exception) {}
                }) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.OpenInNew, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(shortcut.name, fontWeight = FontWeight.Medium)
                            Text(shortcut.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}
