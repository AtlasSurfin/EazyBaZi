package com.fredcomms.baziapp.ui

import java.util.Calendar
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
    

    var citySearchText by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf<CityData?>(null) }
    var expandedCityDropdown by remember { mutableStateOf(false) }

    val filteredCities = remember(citySearchText, selectedCountry){
        val allCitiesInCountry = dbNazioni[selectedCountry] ?: emptyList()
        if(citySearchText.length >= 2){
            allCitiesInCountry.filter { it.n.contains(citySearchText, ignoreCase = true) }.take(10)
        }else {
            emptyList()
        }
    }

    ExposedDropdownMenuBox(
        expanded = expandedCityDropdown && filteredCities.isNotEmpty(),
        onExpandedChange = { expandedCityDropdown = it}
    ){
        OutlinedTextField(
            value = citySearchText,
            onValueChange = {
                citySearchText = it
                expandedCityDropdown = true
            },
            label = { Text("Cerca Città") },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCityDropdown) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )


        ExposedDropdownMenu(
            expanded = expandedCityDropdown && filteredCities.isNotEmpty(),
            onDismissRequest = { expandedCityDropdown = false }
        ){
            filteredCities.forEach { city ->
                DropdownMenuItem(
                    text = { Text(city.n ) },
                    onClick = {
                        selectedCity = city
                        citySearchText = city.n
                        expandedCityDropdown = false
                    }
                )
            }
        }
    }

    var baziChart by remember { mutableStateOf<FullBaZiChart?>(null)}

    var citySearchText by remember { mutableStateOf("")}
    var selectedCity by remember { mutableStateOf<CityData?>(null)}
    var expandedCityDropdown by remember { mutableStateOf(false)}

    //Filtro città in tempo reale
    val filteredCities = remember(citySearchText, selectedCountry){
        val allCitiesInCountry = dbNazioni[selectedCountry] ?: emptyList()
        if (citySearchText.length >= 2){
            allCitiesInCountry.filter { it.n.contains(citySearchText, ignoreCase = true) }.take(10)
        } else {
            emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
        horizontalAlignment = Alignment.CenterHorizontally
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
                val lon = selectedCity?.ln ?: 12.49
                    baziChart = getFullBaZi(
                        selectedYear.toInt(),
                        selectedMonth.toInt(),
                        selectedDay.toInt(),
                        selectedHour.toInt(),
                        selectedMinute.toInt(),
                        lon,
                        selectedCountry        
                    )
                },
                enabled = selectedCity != null,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Calculate BaZi Birth Chart")
        }

        if(selectedCity != null) {
            Text(
                text = "Coordinate: ${selectedCity?.ln}° Longitudine",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        baziChart?.let { chart ->
            Spacer(modifier = Modifier.height(24.dp))


            Row(
                modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ){
                PillarDisplay("Ora", chart.hour, chart.day.stem)
                PillarDisplay("Giorno", chart.month, chart.day.stem)
                PillarDisplay("Mese", chart.day, chart.day.stem)
                PillarDisplay("Anno", chart.year, chart.day.stem)
            }
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
        expanded = expandedCityDropdown && filteredCities.isNotEmpty(),
        onExpandedChange = { expanded = expandedCityDropdown = it },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ){
        OutlinedTextField(
            value = citySearchText,
            onValueChange = {
                citySearchText = it
                expandedCityDropdown = true
                selectedCity = null
            },
            label = { Text("Cerca Città") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCityDropdown) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ){
            filteredCities.forEach { city -> 
                DropdownMenuItem(
                    text = { Text(city.n) },
                    onClick = {
                        selectedCity = city
                        citySearchText = city.n
                        expandedCityDropdown = false
                    }
                )
            }
        }
    }
}

@Composable
fun PillarDisplay(label: String, pillar: Pillar, dayMaster: HeavenlyStem?){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ){
        //Etichetta del Pilastro
        Text(text = label, style = MaterialTheme.typography.labelMedium)

        //Calcolo e visualizzazione del Ten God
        val tenGod = if (dayMaster != null && pillar.stem != null){
            getTenGodName(dayMaster.element, pillar.stem.element, dayMaster.polarity, pillar.stem.polarity)
        }else ""

        Text(
            text = tenGod,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.height(16.dp)
        )


        Spacer(modifier = Modifier.height(4.dp))

        //Tronco Celeste (Heavenly Stem)
        BaZiCard(pillar.stem?.chinese, pillar.stem?.name, pillar.stem?.element)

        Spacer(modifier = Modifier.height(8.dp))

        //Ramo Terrestre (Earthly Branch)
        BaZiCard(pillar.branch?.chinese, pillar.branch?.name, pillar.branch?.element)
    }
}

@Composable
fun BaZiCard(chinese: String?, subText: String?, element: Element?){
    Card(
        modifier = Modifier.size(90.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(element?.let { getElementColor(it) } ?: 0xFFE0E0E0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ){
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text = chinese ?: "?", style = MaterialTheme.typography.headlineMedium)
            Text(text = subText ?: "", style = MaterialTheme.typography.bodySmall)
        }
    }
}