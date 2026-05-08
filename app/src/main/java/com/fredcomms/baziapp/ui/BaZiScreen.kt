package com.fredcomms.baziapp.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import com.fredcomms.baziapp.logic.HeavenlyStem
import com.fredcomms.baziapp.logic.getTenGods
import com.fredcomms.baziapp.logic.findStem
import com.fredcomms.baziapp.logic.getBaZiProfile
import com.fredcomms.baziapp.logic.Element
import com.fredcomms.baziapp.logic.getElementColor

@Composable
fun BaZiScreen() {
    var day by remember { mutableStateOf("1") }
    var month by remember { mutableStateOf("1") }
    var year by remember { mutableStateOf("1990") }
    var hour by remember { mutableStateOf("12") }
    var result by remember { mutableStateOf("Inserisci i dati e calcola il tuo BaZi")}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "EaziBaZi Calculator 2.0",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            OutlinedTextField(
                value = day,
                onValueChange = { day = it },
                label = { Text("DD") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = month,
                onValueChange = { month = it },
                label = { Text("MM") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("YYYY") },
                modifier = Modifier.weight(1.5f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            OutlinedTextField(
                value = hour,
                onValueChange = { hour = it },
                label = { Text("Hour (0-23)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Button(
            onClick = {
                result = getBaZiProfile(
                    year.toIntOrNull() ?: 1990,
                    month.toIntOrNull() ?: 1,
                    day.toIntOrNull() ?: 1,
                    hour.toIntOrNull() ?: 12,
                    0
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate BaZi")
        }

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

@Composable
fun PillarDisplay(stem: HeavenlyStem?, branch: EarthlyBranch?){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        //Tronco Celeste (Sopra)
        Card(
            modifier = Modifier.size(120.dp),
        )
    }
}