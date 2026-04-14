package com.tinlera.toolbox.ui.screens.hacker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinlera.toolbox.tools.hacker.ApkAnalyzer
import com.tinlera.toolbox.tools.hacker.ApkInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkAnalyzerScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var packageName by remember { mutableStateOf("") }
    var apkInfo by remember { mutableStateOf<ApkInfo?>(null) }
    var trackers by remember { mutableStateOf<List<String>>(emptyList()) }
    var exported by remember { mutableStateOf<List<String>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔍 APK Analyzer") },
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
                                error = ""
                                try {
                                    apkInfo = ApkAnalyzer.analyzePackage(packageName)
                                    trackers = ApkAnalyzer.detectTrackers(
                                        apkInfo?.permissions ?: emptyList(),
                                        apkInfo?.activities ?: emptyList()
                                    )
                                    exported = ApkAnalyzer.getExportedActivities(packageName)
                                } catch (e: Exception) {
                                    error = e.message ?: "Hata oluştu"
                                }
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.Search, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Analiz Et")
                }
            }

            if (error.isNotEmpty()) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            apkInfo?.let { info ->
                // Basic Info
                SectionCard("📦 Paket Bilgisi") {
                    InfoRow("Paket", info.packageName)
                    InfoRow("Versiyon", "${info.versionName} (${info.versionCode})")
                    InfoRow("Min SDK", info.minSdk)
                    InfoRow("Target SDK", info.targetSdk)
                }

                // Permissions
                if (info.permissions.isNotEmpty()) {
                    SectionCard("🔐 İzinler (${info.permissions.size})") {
                        info.permissions.forEach { perm ->
                            val isDangerous = perm.contains("CAMERA") || perm.contains("LOCATION") ||
                                perm.contains("CONTACTS") || perm.contains("RECORD_AUDIO") ||
                                perm.contains("READ_SMS") || perm.contains("CALL_LOG")
                            Text(
                                "${if (isDangerous) "⚠️" else "✅"} $perm",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = if (isDangerous) Color(0xFFFF6B6B) else Color.Unspecified
                            )
                        }
                    }
                }

                // Trackers
                if (trackers.isNotEmpty()) {
                    SectionCard("🕵️ Tracker Tespit (${trackers.size})") {
                        trackers.forEach { tracker ->
                            Text("🚨 $tracker", color = Color(0xFFFF6B6B), fontSize = 13.sp)
                        }
                    }
                }

                // Activities
                if (info.activities.isNotEmpty()) {
                    SectionCard("📱 Activity'ler (${info.activities.size})") {
                        info.activities.take(20).forEach { act ->
                            Text(act, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }
                        if (info.activities.size > 20) {
                            Text("... ve ${info.activities.size - 20} daha", color = Color.Gray)
                        }
                    }
                }

                // Exported Activities
                if (exported.isNotEmpty()) {
                    SectionCard("🔓 Export Edilen Activity'ler (${exported.size})") {
                        exported.forEach { act ->
                            Text("🚪 $act", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }
                    }
                }

                // Native Libs
                if (info.nativeLibs.isNotEmpty()) {
                    SectionCard("📚 Native Kütüphaneler (${info.nativeLibs.size})") {
                        info.nativeLibs.forEach { lib ->
                            Text("🔧 $lib", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }
                    }
                }
            }

            // String search
            Spacer(Modifier.height(8.dp))
            Text("🔍 String Arama", fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Aranacak metin") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = {
                    if (packageName.isNotBlank() && searchQuery.isNotBlank()) {
                        scope.launch {
                            searchResults = ApkAnalyzer.searchStrings(packageName, searchQuery)
                        }
                    }
                }) {
                    Icon(Icons.Default.Search, "Ara")
                }
            }

            if (searchResults.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0D1117))
                        .padding(12.dp)
                ) {
                    SelectionContainer {
                        Column {
                            searchResults.forEach { line ->
                                Text(line, color = Color(0xFF00FF41), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text("$label: ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(value, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
    }
}
