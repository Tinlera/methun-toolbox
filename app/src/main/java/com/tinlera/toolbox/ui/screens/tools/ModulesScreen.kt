package com.tinlera.toolbox.ui.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinlera.toolbox.tools.root.ModuleManager
import com.tinlera.toolbox.tools.root.ModuleInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesScreen(onBack: () -> Unit) {
    var modules by remember { mutableStateOf<List<ModuleInfo>>(emptyList()) }
    var rootType by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { rootType = ModuleManager.getRootType(); modules = ModuleManager.listModules(); loading = false }

    Scaffold(
        topBar = { TopAppBar(title = { Text("🧩 Modüller") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Geri") } }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ElevatedCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AdminPanelSettings, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column { Text("Root Türü", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(rootType.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.weight(1f)); Text("${modules.size} modül")
                }
            }
            if (loading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            else if (modules.isEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Modül yok", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(modules, key = { it.id }) { module ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(module.name, fontWeight = FontWeight.Bold)
                                    Text("v${module.version}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (module.description.isNotBlank()) Text(module.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = module.enabled, onCheckedChange = { enable ->
                                    scope.launch {
                                        if (enable) ModuleManager.enableModule(module.id) else ModuleManager.disableModule(module.id)
                                        modules = ModuleManager.listModules()
                                        snackbarHostState.showSnackbar(if (enable) "${module.name} aktif (reboot gerekli)" else "${module.name} devre dışı (reboot gerekli)")
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}
