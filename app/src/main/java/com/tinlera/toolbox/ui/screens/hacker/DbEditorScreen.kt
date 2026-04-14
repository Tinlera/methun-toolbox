package com.tinlera.toolbox.ui.screens.hacker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinlera.toolbox.tools.hacker.DbEditor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DbEditorScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var packageName by remember { mutableStateOf("") }
    var sharedPrefs by remember { mutableStateOf<List<String>>(emptyList()) }
    var databases by remember { mutableStateOf<List<String>>(emptyList()) }
    var tables by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedDb by remember { mutableStateOf("") }
    var prefsContent by remember { mutableStateOf("") }
    var queryResult by remember { mutableStateOf("") }
    var customQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var activeTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🗄️ DB / SharedPrefs Editor") },
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

            Button(
                onClick = {
                    if (packageName.isNotBlank()) {
                        scope.launch {
                            isLoading = true
                            sharedPrefs = DbEditor.listSharedPrefs(packageName)
                            databases = DbEditor.listDatabases(packageName)
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Search, null)
                Spacer(Modifier.width(8.dp))
                Text("Tara")
            }

            // Tab switch
            TabRow(selectedTabIndex = activeTab) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                    Text("SharedPrefs", modifier = Modifier.padding(12.dp))
                }
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                    Text("SQLite DB", modifier = Modifier.padding(12.dp))
                }
            }

            when (activeTab) {
                0 -> {
                    // SharedPreferences
                    if (sharedPrefs.isEmpty() && !isLoading) {
                        Text("Henüz taranmadı veya SharedPrefs bulunamadı", color = Color.Gray)
                    }
                    sharedPrefs.forEach { file ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        prefsContent = DbEditor.readSharedPrefs(packageName, file)
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                "📄 $file",
                                modifier = Modifier.padding(12.dp),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (prefsContent.isNotEmpty()) {
                        Text("İçerik:", fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0D1117))
                                .padding(12.dp)
                        ) {
                            SelectionContainer {
                                Text(
                                    prefsContent,
                                    color = Color(0xFF00FF41),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // SQLite Databases
                    if (databases.isEmpty() && !isLoading) {
                        Text("Henüz taranmadı veya veritabanı bulunamadı", color = Color.Gray)
                    }
                    databases.forEach { db ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedDb = db
                                    scope.launch {
                                        tables = DbEditor.listTables(packageName, db)
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedDb == db) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                "🗃️ $db",
                                modifier = Modifier.padding(12.dp),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (tables.isNotEmpty()) {
                        Text("Tablolar:", fontWeight = FontWeight.Bold)
                        tables.forEach { table ->
                            AssistChip(
                                onClick = {
                                    scope.launch {
                                        queryResult = DbEditor.dumpTable(packageName, selectedDb, table, 50)
                                    }
                                },
                                label = { Text(table) },
                                leadingIcon = { Text("📋") }
                            )
                        }
                    }

                    // Custom query
                    if (selectedDb.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("SQL Sorgusu:", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = customQuery,
                            onValueChange = { customQuery = it },
                            label = { Text("SQL") },
                            placeholder = { Text("SELECT * FROM table_name LIMIT 10") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        )
                        Button(onClick = {
                            if (customQuery.isNotBlank()) {
                                scope.launch {
                                    queryResult = DbEditor.executeQuery(packageName, selectedDb, customQuery)
                                }
                            }
                        }) {
                            Text("Çalıştır")
                        }
                    }

                    if (queryResult.isNotEmpty()) {
                        Text("Sonuç:", fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0D1117))
                                .padding(12.dp)
                        ) {
                            SelectionContainer {
                                Text(
                                    queryResult,
                                    color = Color(0xFF58A6FF),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
