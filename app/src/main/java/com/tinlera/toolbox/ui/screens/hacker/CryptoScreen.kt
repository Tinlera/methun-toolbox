package com.tinlera.toolbox.ui.screens.hacker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.tinlera.toolbox.tools.hacker.CryptoToolkit
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CryptoScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var selectedAlgo by remember { mutableStateOf("MD5") }

    val algorithms = listOf("MD5", "SHA-1", "SHA-256", "Base64 Encode", "Base64 Decode", "Hex Encode", "Hex Decode", "ROT13", "Random 32B")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔐 Crypto Toolkit") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Geri") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Girdi") },
                placeholder = { Text("Metin veya dosya yolu girin...") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )

            Spacer(Modifier.height(16.dp))

            // Algorithm chips
            Text("Algoritma Seçin:", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                algorithms.forEach { algo ->
                    FilterChip(
                        selected = selectedAlgo == algo,
                        onClick = { selectedAlgo = algo },
                        label = { Text(algo, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00FF41).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFF00FF41)
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        output = when (selectedAlgo) {
                            "MD5" -> CryptoToolkit.md5(input)
                            "SHA-1" -> CryptoToolkit.sha1(input)
                            "SHA-256" -> CryptoToolkit.sha256(input)
                            "Base64 Encode" -> CryptoToolkit.base64Encode(input)
                            "Base64 Decode" -> CryptoToolkit.base64Decode(input)
                            "Hex Encode" -> CryptoToolkit.hexEncode(input)
                            "Hex Decode" -> CryptoToolkit.hexDecode(input)
                            "ROT13" -> CryptoToolkit.rot13(input)
                            "Random 32B" -> CryptoToolkit.randomBytes(32)
                            else -> "Bilinmeyen algoritma"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
            ) {
                Icon(Icons.Filled.Lock, null)
                Spacer(Modifier.width(8.dp))
                Text("Hesapla")
            }

            // Output
            if (output.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Sonuç:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0D1117))
                        .padding(16.dp)
                ) {
                    SelectionContainer {
                        Text(
                            output,
                            color = Color(0xFF00FF41),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // File hash section
            Spacer(Modifier.height(24.dp))
            Text("📄 Dosya Hash", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            var filePath by remember { mutableStateOf("") }
            var fileHash by remember { mutableStateOf("") }

            OutlinedTextField(
                value = filePath,
                onValueChange = { filePath = it },
                label = { Text("Dosya Yolu") },
                placeholder = { Text("/sdcard/Download/file.apk") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    scope.launch { fileHash = "MD5: ${CryptoToolkit.fileMd5(filePath)}" }
                }) { Text("MD5") }
                OutlinedButton(onClick = {
                    scope.launch { fileHash = "SHA-256: ${CryptoToolkit.fileSha256(filePath)}" }
                }) { Text("SHA-256") }
            }

            if (fileHash.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF0D1117)).padding(12.dp)
                ) {
                    SelectionContainer {
                        Text(fileHash, color = Color(0xFF58A6FF), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
