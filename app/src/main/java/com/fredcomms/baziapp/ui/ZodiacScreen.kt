package com.fredcomms.baziapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fredcomms.baziapp.logic.HeavenlyStem
import com.fredcomms.baziapp.logic.getTenGods
import com.fredcomms.baziapp.logic.findStem

@Composable
fun ZodiacScreen() {
    var stemInput by remember { mutableStateOf(HeavenlyStem.JIA) }
    var branchInput by remember { mutableStateOf(HeavenlyStem.BING) }
    var result by remember { mutableStateOf("Risultato: ---") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "EaziBaZi Calculator",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )


        Spacer(modifier = Modifier.height(24.dp))

        //campo per Heavenly Stem
        OutlinedTextField(
            value = stemInput,
            onValueChange = { stemInput = it },
            label = { Text("Day Master (Tronco)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = branchInput,
            onValueChange = { branchInput = it },
            label = { Text("Ramo/Mese dell'Anno") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))


        Button(
            onClick = {
                try {
                    val dm = findStem(stemInput)
                    val target = findStem(branchInput)

                    if(dm != null && target != null) {
                        val res = getTenGods(dm, target)
                        result = "Dieci Dei: $res"
                    } else {
                        result = "Errore: Inserisci nomi validi (es. Jia, Bing, etc.)"
                    }
            } catch (e: Exception) {
                    result = "Errore: boh, qualcosa è andato storto"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calcola")
        }

        Spacer(modifier = Modifier.height(32.dp))


        //Area Risultato
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = result,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}