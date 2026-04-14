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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinlera.toolbox.tools.root.BuildPropEditor
import com.tinlera.toolbox.tools.root.PropEntry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildPropScreen(onBack: () -> Unit) {
    var props by remember { mutableStateOf<List<PropEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var editingProp by remember { mutableStateOf<PropEntry?>(null) }
    var editValue by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { props = BuildPropEditor.readAll(); loading = false }

    val filteredProps = props.filter {
        searchQuery.isBlank() || it.key.contains(searchQuery, true) || it.value.contains(searchQuery, true)
    }

    if (editingProp != null) {
        AlertDialog(onDismissRequest = { editingProp = null }, title = { Text("Düzenle") },
            text = { Column { Text(editingProp!!.key, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = editValue, onValueChange = { editValue = it }, modifier = Modifier.fillMaxWidth(), singleLine = true) } },
            confirmButton = { Button(onClick = { scope.launch { BuildPropEditor.setProp(editingProp!!.key, editValue); props = BuildPropEditor.readAll(); snackbarHostState.showSnackbar("✅ Güncellendi"); editingProp = null } }) { Text("Kaydet") } },
            dismissButton = { TextButton(onClick = { editingProp = null }) { Text("İptal") } })
    }
    if (showAddDialog) {
        AlertDialog(onDismissRequest = { showAddDialog = false }, title = { Text("Yeni Özellik") },
            text = { Column { OutlinedTextField(value = newKey, onValueChange = { newKey = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Anahtar") }, singleLine = true); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = newValue, onValueChange = { newValue = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Değer") }, singleLine = true) } },
            confirmButton = { Button(onClick = { scope.launch { BuildPropEditor.setProp(newKey, newValue); props = BuildPropEditor.readAll(); snackbarHostState.showSnackbar("✅ Eklendi"); showAddDialog = false; newKey = ""; newValue = "" } }) { Text("Ekle") } },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("İptal") } })
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("🔧 build.prop") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Geri") } },
            actions = {
                IconButton(onClick = { scope.launch { BuildPropEditor.backup(); snackbarHostState.showSnackbar("✅ Yedeklendi") } }) { Icon(Icons.Filled.Save, "Yedekle") }
                IconButton(onClick = { showAddDialog = true }) { Icon(Icons.Filled.Add, "Ekle") }
            }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), placeholder = { Text("Ara...") }, leadingIcon = { Icon(Icons.Filled.Search, null) }, singleLine = true)
            Text("${filteredProps.size} özellik", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (loading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(filteredProps, key = { it.key }) { prop ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth(), onClick = { editingProp = prop; editValue = prop.value }) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(prop.key, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text(prop.value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { scope.launch { BuildPropEditor.deleteProp(prop.key); props = BuildPropEditor.readAll(); snackbarHostState.showSnackbar("Silindi") } }) {
                                    Icon(Icons.Filled.Delete, "Sil", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
