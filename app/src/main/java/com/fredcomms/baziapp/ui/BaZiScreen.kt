package com.fredcomms.baziapp.ui

import java.util.Calendar
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import com.fredcomms.baziapp.logic.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaZiScreen() {
    var years = (1900..2070).map { it.toString() }.reversed()
    var months = (1..12).map { it.toString()}
    var days = (1..31).map { it.toString()}
    var hours = (0..23).map { it.toString().padStart(2, '0')}
    var minutes = (0..55 step 5).map { it.toString().padStart(2, '0')}

    var currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
    var selectedDay by remember { mutableStateOf("1") }
    var selectedMonth by remember { mutableStateOf("1") }
    var selectedYear by remember { mutableStateOf(currentYear)}
    var selectedHour by remember { mutableStateOf("12") }
    var selectedMinute by remember { mutableStateOf("00") }

    var baziData by remember { mutableStateOf<BaZiResult?>(null)}
    

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "EaziBaZi Calculator 2.5",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        //RIGA DATA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            BaZiDropdown("Day", days, selectedDay, { selectedDay = it }, Modifier.weight(1f))
            BaZiDropdown("Month", months, selectedMonth, { selectedMonth = it }, Modifier.weight(1f))
            BaZiDropdown("Year", years, selectedYear, { selectedYear = it }, Modifier.weight(1.5f))
        }

        //RIGA ORA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            BaZiDropdown("Hour", hours, selectedHour, { selectedHour = it }, Modifier.weight(1f))
            BaZiDropdown("Minute", minutes, selectedMinute, { selectedMinute = it }, Modifier.weight(1f))
        }

        Button(
            onClick = {
                baziData = getBaZiData(
                    selectedYear.toInt(),
                    selectedMonth.toInt(),
                    selectedDay.toInt(),
                    selectedHour.toInt(),
                    selectedMinute.toInt()
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate BaZi Pillar")
        }

        baziData?.let { data ->
            PillarDisplay(data.stem, data.branch)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaZiDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
){
    var expanded by remember { mutableStateOf(false)}

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ){
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false}
        ){
            options.forEach {selectionOption ->
                DropdownMenuItem(
                    text = { Text(text = selectionOption)},
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun PillarDisplay(stem: HeavenlyStem?, branch: EarthlyBranch?){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        //Heavenly Stem (Sopra)
        Card(
            modifier = Modifier.size(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(
                    stem?.element?.let { getElementColor(it) } ?: 0xFF9E9E9E
                )
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ){
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(text = stem?.chinese ?: "?", style = MaterialTheme.typography.displayMedium)
                Text(text = stem?.name ?: "", style = MaterialTheme.typography.titleMedium)
                Text(text = stem?.element?.name ?: "", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        //Earthly Branch (Sotto)
        Card(
            modifier = Modifier.size(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(
                    branch?.element?.let { getElementColor(it) } ?: 0xFF9E9E9E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ){
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(text = branch?.chinese ?: "?", style = MaterialTheme.typography.displayMedium)
                Text(text = branch?.pinyin ?: "", style = MaterialTheme.typography.titleMedium)
                Text(text = branch?.zodiac ?: "", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}