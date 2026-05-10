package com.fredcomms.baziapp.ui

import java.util.Calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import com.fredcomms.baziapp.logic.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaZiScreen() {

    val context = LocalContext.current
    //Carico db delle nazioni
    val dbNazioni = remember { CityLoader.loadCitiesByCountry(context) }
    
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
    
    var selectedCountry by remember { mutableStateOf("IT") }
    var baziChart by remember { mutableStateOf<FullBaZiChart?>(null)}
    
    var selectedCountry by remember { mutableStateOf("IT")}
    var expandedCountryDropdown by remember {mutableStateOf(false)}
    val countries = remember { dbNazioni.keys.toList().sorted() }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
            .verticalScroll(rememberScrollState()),
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

        Spacer(modifier = Modifier.height(16.dp))

        //Selettore nazione
        Text(text = "Nazione di Nascita", style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(
            expanded = expandedCountryDropdown,
            onExpandedChange = { expandedCountryDropdown = it},
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ){
            OutlinedTextField(
                value = selectedCountry,
                onValueChange = {},
                readOnly = true,
                label = { Text("Seleziona Nazione") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCountryDropdown) }
            )

            ExposedDropdownMenu(
                expanded = expandedCountryDropdown,
                onDismissRequest = { expandedCountryDropdown = false }
            ){
                countries.forEach { countryCode ->
                    DropdownMenuItem(
                        text = { Text(countryCode) },
                        onClick = {
                            selectedCountry = countryCode
                            expandedCountryDropdown = false
                            citySearchText = ""
                            selectedCity = null
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        //Nuovo sistema ricerca città
        Text(text = "Città di Nascita", style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(
            expanded = expandedCityDropdown && filteredCities.isNotEmpty()
            onExpandedChange = { expandedCityDropdown = it}
        ){
            OutlinedTextField(
                value = citySearchText,
                onValueChange = {
                    citySearchText = it
                    expandedCityDropdown = true
                    selectedCity = null
                },
                label = {Text("Cerca città...") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCityDropdown)}
            )

            ExposedDropdownMenu(
                expanded = expandedCityDropdown && filteredCities.isNotEmpty(),
                onDismissRequest = { expandedCityDropdown = false }
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

        //Tasto Calcola
        Button(
            onClick = {
                selectedCity?.let { city ->
                    baziChart = getFullBaZi(
                        selectedYear.toInt(),
                        selectedMonth.toInt(),
                        selectedDay.toInt(),
                        selectedHour.toInt(),
                        selectedMinute.toInt(),
                        city.ln,
                        selectedCountry
                    )
                }
            },
            enabled = selectedCity != null,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ){
            Text("Calcola Mappa")
        }

        baziChart?.let { chart ->
            Spacer(modifier = Modifier.height(32.dp))

            //4 Pillars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 16.dp)
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ){
                PillarDisplay("Ora", chart.hour, chart.day.stem)
                PillarDisplay("Giorno", chart.day, chart.day.stem)
                PillarDisplay("Mese", chart.month, chart.day.stem)
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