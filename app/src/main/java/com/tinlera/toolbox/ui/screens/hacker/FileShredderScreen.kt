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
import com.tinlera.toolbox.tools.hacker.FileShredder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileShredderScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var filePath by remember { mutableStateOf("") }
    var passes by remember { mutableIntStateOf(3) }
    var result by remember { mutableStateOf("") }
    var isShredding by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💀 File Shredder") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Warning card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B1B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚠️ DİKKAT", fontWeight = FontWeight.Bold, color = Color(0xFFFF6B6B), fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Bu araç dosyaları askeri standartta (DoD 5220.22-M) geri dönüşümsüz olarak siler. " +
                        "Silinen dosyalar HİÇBİR ŞEKİLDE kurtarılamaz!",
                        color = Color(0xFFFFAB91),
                        fontSize = 13.sp
                    )
                }
            }

            OutlinedTextField(
                value = filePath,
                onValueChange = { filePath = it },
                label = { Text("Dosya Yolu") },
                placeholder = { Text("/sdcard/Download/gizli-dosya.txt") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )

            // Pass count selector
            Text("Üzerine Yazma Sayısı:", fontWeight = FontWeight.Bold)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(1 to "Hızlı (1)", 3 to "DoD (3)", 7 to "Gutmann (7)").forEach { (count, label) ->
                    FilterChip(
                        selected = passes == count,
                        onClick = { passes = count },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }

            // Info about passes
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Silme Yöntemi:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("• Pass 1: Sıfırlarla üzerine yaz (/dev/zero)", fontSize = 12.sp)
                    Text("• Pass 2: Rastgele veri yaz (/dev/urandom)", fontSize = 12.sp)
                    Text("• Pass 3+: Sıfır/rastgele döngüsü", fontSize = 12.sp)
                    Text("• Son: Rastgele veri + dosyayı sil", fontSize = 12.sp)
                }
            }

            // Shred button
            Button(
                onClick = { showConfirm = true },
                enabled = filePath.isNotBlank() && !isShredding,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                if (isShredding) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Icon(Icons.Default.Delete, null)
                }
                Spacer(Modifier.width(8.dp))
                Text(if (isShredding) "Siliniyor..." else "💀 GÜVENLİ SİL ($passes pass)")
            }

            if (result.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.contains("✅")) Color(0xFF1B2D1B) else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        result,
                        modifier = Modifier.padding(12.dp),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = if (result.contains("✅")) Color(0xFF00FF41) else Color.Unspecified
                    )
                }
            }
        }
    }

    // Confirmation dialog
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("⚠️ Emin misiniz?") },
            text = {
                Text("\"$filePath\" dosyası $passes pass ile geri dönüşümsüz olarak silinecek. Bu işlem geri alınamaz!")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        scope.launch {
                            isShredding = true
                            result = FileShredder.shredFile(filePath, passes)
                            isShredding = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF6B6B))
                ) {
                    Text("SİL")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("İptal")
                }
            }
        )
    }
}
