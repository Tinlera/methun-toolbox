package com.tinlera.toolbox.ui.screens.tools

import androidx.compose.animation.animateColorAsState
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
import com.tinlera.toolbox.tools.shizuku.AppInfo
import com.tinlera.toolbox.tools.shizuku.DebloatManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebloatScreen(onBack: () -> Unit) {
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showDisabledOnly by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showDisabledOnly) {
        loading = true
        apps = if (showDisabledOnly) DebloatManager.listDisabledApps()
               else DebloatManager.listSystemApps()
        loading = false
    }

    val filteredApps = apps.filter {
        searchQuery.isBlank() || it.packageName.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📦 Debloat Manager") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Geri") }
                },
                actions = {
                    FilterChip(
                        selected = showDisabledOnly,
                        onClick = { showDisabledOnly = !showDisabledOnly },
                        label = { Text("Devre Dışı") },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Uygulama ara...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true
            )
            Text("${filteredApps.size} uygulama",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        val bgColor by animateColorAsState(
                            if (app.isEnabled) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f), label = "bg"
                        )
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = bgColor)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(app.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(app.packageName, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (app.isEnabled) {
                                    IconButton(onClick = {
                                        scope.launch {
                                            val r = DebloatManager.disableApp(app.packageName)
                                            snackbarHostState.showSnackbar(
                                                if (r.exitCode == 0) "${app.packageName} devre dışı" else "Hata: ${r.error}")
                                            apps = if (showDisabledOnly) DebloatManager.listDisabledApps() else DebloatManager.listSystemApps()
                                        }
                                    }) { Icon(Icons.Filled.Block, "Devre Dışı", tint = MaterialTheme.colorScheme.error) }
                                } else {
                                    IconButton(onClick = {
                                        scope.launch {
                                            val r = DebloatManager.enableApp(app.packageName)
                                            snackbarHostState.showSnackbar(
                                                if (r.exitCode == 0) "${app.packageName} etkinleştirildi" else "Hata: ${r.error}")
                                            apps = if (showDisabledOnly) DebloatManager.listDisabledApps() else DebloatManager.listSystemApps()
                                        }
                                    }) { Icon(Icons.Filled.CheckCircle, "Etkinleştir", tint = MaterialTheme.colorScheme.primary) }
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        val r = DebloatManager.uninstallForUser(app.packageName)
                                        snackbarHostState.showSnackbar(
                                            if (r.exitCode == 0) "${app.packageName} kaldırıldı" else "Hata: ${r.error}")
                                        apps = if (showDisabledOnly) DebloatManager.listDisabledApps() else DebloatManager.listSystemApps()
                                    }
                                }) { Icon(Icons.Filled.Delete, "Kaldır", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
            }
        }
    }
}
