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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinlera.toolbox.tools.shizuku.ArtOptimizer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtScreen(onBack: () -> Unit) {
    var isRunning by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    var dexoptInfo by remember { mutableStateOf("Yükleniyor...") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { dexoptInfo = ArtOptimizer.getDexoptStatus() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚡ ART Optimizer") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Geri") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tüm Uygulamaları Optimize Et", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Birkaç dakika sürebilir", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    ArtOptimizer.CompileMode.entries.forEach { mode ->
                        OutlinedButton(
                            onClick = {
                                if (!isRunning) scope.launch {
                                    isRunning = true; statusText = "${mode.label} derleniyor..."
                                    val r = ArtOptimizer.compileAllPackages(mode)
                                    statusText = if (r.exitCode == 0) "✅ Tamamlandı!" else "❌ ${r.error}"
                                    isRunning = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), enabled = !isRunning
                        ) { Icon(Icons.Filled.Speed, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(mode.label) }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Arkaplan Dexopt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        scope.launch { isRunning = true; statusText = "bg-dexopt-job..."; val r = ArtOptimizer.triggerBgDexopt(); statusText = if (r.exitCode == 0) "✅ Dexopt OK" else "❌ ${r.error}"; isRunning = false }
                    }, enabled = !isRunning, modifier = Modifier.fillMaxWidth()) { Text("bg-dexopt-job Tetikle") }
                }
            }
            Spacer(Modifier.height(12.dp))
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Profilleri Sıfırla", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = {
                        scope.launch { isRunning = true; statusText = "Sıfırlanıyor..."; val r = ArtOptimizer.clearAllProfileData(); statusText = if (r.exitCode == 0) "✅ Sıfırlandı" else "❌ ${r.error}"; isRunning = false }
                    }, enabled = !isRunning, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Tüm Profilleri Sıfırla") }
                }
            }
            if (statusText.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (isRunning) { CircularProgressIndicator(Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)) }
                        Text(statusText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Dexopt Durumu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(dexoptInfo, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}
