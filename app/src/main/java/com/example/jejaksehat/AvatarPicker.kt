package com.example.jejaksehat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val cuteAvatars = listOf(
    "👩‍⚕️" to "Dokter Ceria", "👨‍⚕️" to "Dokter Hebat",
    "👧" to "Peri Kecil", "👦" to "Jagoan Sehat",
    "🧑‍🔬" to "Ilmuwan Sehat", "🐰" to "Kelinci Lucu",
    "🐱" to "Kucing Imut", "🦄" to "Unicorn Ceria"
)

@Composable
fun AvatarPicker(current: String, select: (String) -> Unit, cancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = cancel,
        title = { Text("Pilih avatar lucu ✨") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                cuteAvatars.chunked(4).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        row.forEach { (avatar, label) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { select(avatar) }.padding(4.dp)
                            ) {
                                Text(avatar, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.size(52.dp))
                                Text(label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(cancel) { Text("Tutup") } }
    )
}
