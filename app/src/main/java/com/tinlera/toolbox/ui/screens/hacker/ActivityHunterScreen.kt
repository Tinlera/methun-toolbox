package com.tinlera.toolbox.ui.screens.hacker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinlera.toolbox.tools.hacker.ActivityHunter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityHunterScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var packageName by remember { mutableStateOf("") }
    var components by remember { mutableStateOf<List<ActivityHunter.AppComponent>>(emptyList()) }
    var filterType by remember { mutableStateOf("all") }
    var launchResult by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showExportedOnly by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎯 Activity Hunter") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = packageName,
                onValueChange = { packageName = it },
                label = { Text("Paket Adı") },
                placeholder = { Text("com.example.app") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (packageName.isNotBlank()) {
                            scope.launch {
                                isLoading = true
                                components = ActivityHunter.listComponents(packageName)
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.Search, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Keşfet")
                }
            }

            if (components.isNotEmpty()) {
                // Filter chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("all" to "Tümü", "activity" to "Activity", "service" to "Service", "receiver" to "Receiver").forEach { (type, label) ->
                        FilterChip(
                            selected = filterType == type,
                            onClick = { filterType = type },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showExportedOnly, onCheckedChange = { showExportedOnly = it })
                    Text("Sadece exported", fontSize = 13.sp)
                }

                val filtered = components.filter { comp ->
                    (filterType == "all" || comp.type == filterType) &&
                    (!showExportedOnly || comp.exported)
                }

                Text("${filtered.size} bileşen bulundu", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                filtered.forEach { comp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (comp.type) {
                                "activity" -> MaterialTheme.colorScheme.primaryContainer
                                "service" -> MaterialTheme.colorScheme.secondaryContainer
                                "receiver" -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        comp.name.substringAfterLast("/"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "${comp.type.uppercase()} ${if (comp.exported) "🔓" else "🔒"}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                if (comp.type == "activity") {
                                    IconButton(onClick = {
                                        scope.launch {
                                            launchResult = ActivityHunter.launchActivity(comp.name)
                                        }
                                    }) {
                                        Icon(Icons.Default.PlayArrow, "Başlat", tint = Color(0xFF00C853))
                                    }
                                } else if (comp.type == "service") {
                                    IconButton(onClick = {
                                        scope.launch {
                                            launchResult = ActivityHunter.startService(comp.name)
                                        }
                                    }) {
                                        Icon(Icons.Default.PlayArrow, "Başlat", tint = Color(0xFF2979FF))
                                    }
                                }
                            }
                            Text(
                                comp.name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            if (launchResult.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        "Sonuç: $launchResult",
                        modifier = Modifier.padding(12.dp),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
