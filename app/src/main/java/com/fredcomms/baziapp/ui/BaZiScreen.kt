package com.fredcomms.baziapp.ui

import com.fredcomms.baziapp.logic.*
import java.util.Calendar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

enum class AppTab {CHART, GRAPH, LEARN}
enum class ChartStep {INPUT, LOADING, RESULTS}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaZiScreen() {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(AppTab.CHART) }
    var currentChartStep by remember { mutableStateOf(ChartStep.INPUT) }

    val backgroundColor = Color(0xFF121212)
    val navbarContainerColor = Color(0xFF1E1E1E)
    val accentColor = Color(0xFF10B981)


    var years = remember { (1920..2040).map { it.toString() }.reversed() }
    var months = remember { (1..12).map { it.toString()} }
    var days = remember { (1..31).map { it.toString()} }
    var hours = remember { (0..23).map { it.toString().padStart(2, '0')} }
    var minutes = remember { (0..55 step 5).map { it.toString().padStart(2, '0')} }

    var currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()

    var selectedDay by remember { mutableStateOf("1") }
    var selectedMonth by remember { mutableStateOf("1") }
    var selectedYear by remember { mutableStateOf(currentYear)}
    var selectedHour by remember { mutableStateOf("12") }
    var selectedMinute by remember { mutableStateOf("00") }

    var citySearchText by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf<CityData?>(null) }

    var baziChart by remember { mutableStateOf<FullBaZiChart?>(null)}
    var gpsError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp)
        ){
            when (currentTab) {
                AppTab.CHART -> {
                    //Logica per Input, Loading, Result
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Text(
                            text = "EazyBaZi Calculator 3.0",
                            style = MaterialTheme.typography.headlineLarge,
                            color = accentColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        when(currentChartStep){
                            ChartStep.INPUT -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ){
                                    BaZiDropdown("Giorno", days, selectedDay, { selectedDay = it }, Modifier.weight(1f))
                                    BaZiDropdown("Mese", months, selectedMonth, { selectedMonth = it }, Modifier.weight(1f))
                                    BaZiDropdown("Anno", years, selectedYear, { selectedYear = it }, Modifier.weight(1.5f))
                                }


                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ){
                                    BaZiDropdown("Ora", hours, selectedHour, { selectedHour = it }, Modifier.weight(1f))
                                    BaZiDropdown("Minuti", minutes, selectedMinute, { selectedMinute = it }, Modifier.weight(1f))
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(text = "Luogo di Nascita", style = MaterialTheme.typography.labelMedium, color = Color.Gray)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    OutlinedTextField(
                                        value = citySearchText,
                                        onValueChange = { typedText -> 
                                            citySearchText = typedText
                                            if(typedText.isEmpty()) selectedCity = null
                                        },
                                        label = { Text("Cerca Città...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            IconButton(onClick = {
                                                if(citySearchText.length >= 2){
                                                    getCoordinatesFromName(context, citySearchText) { city ->
                                                        if(city != null){
                                                            selectedCity = city
                                                            citySearchText = city.n
                                                        }
                                                    }
                                                }
                                            }) {
                                                Text ("🔍")
                                            }
                                        }
                                    )
                                }

                                Button(
                                    onClick = {
                                        selectedCity?.let { city ->
                                            currentChartStep = ChartStep.LOADING
                                            coroutineScope.launch {
                                                baziChart = getFullBaZi(
                                                    selectedYear.toInt(),
                                                    selectedMonth.toInt(),
                                                    selectedDay.toInt(),
                                                    selectedHour.toInt(),
                                                    selectedMinute.toInt(),
                                                    city.ln
                                                )
                                                delay(1500) //1.5 sec di caricamento
                                                currentChartStep = ChartStep.RESULTS
                                            }
                                        }
                                    },
                                    enabled = selectedCity != null,
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                                ){
                                    Text("Calcola Mappa", color = Color.Black)
                                }
                            }

                            ChartStep.LOADING -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(300.dp),
                                    contentAlignment = Alignment.Center
                                ){
                                    Column(horizontalAlignment = Alignment.CenterHorizontally){
                                        CircularProgressIndicator(color = accentColor)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("Calculating Birth Chart ...", color = Color.White)
                                    }
                                }
                            }

                            ChartStep.RESULTS -> {
                                baziChart?.let { chart ->
                                    Spacer(modifier = Modifier.height(32.dp))

                                    //4 Pillars
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ){
                                        PillarDisplay("Ora", chart.hour, chart.day.stem)
                                        PillarDisplay("Giorno", chart.day, chart.day.stem)
                                        PillarDisplay("Mese", chart.month, chart.day.stem)
                                        PillarDisplay("Anno", chart.year, chart.day.stem)
                                    }


                                    Spacer(modifier = Modifier.height(32.dp))

                                    OutlinedButton(
                                        onClick = { currentChartStep = ChartStep.INPUT },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                                        modifier = Modifier.fillMaxWidth()
                                    ){
                                        Text("Calcola Nuova Mappa 🔄")
                                    }
                                }
                            }
                        }
                    }
                }
                AppTab.GRAPH -> {
                    Text("Scheda GRAPH attiva", color = Color.White, modifier = Modifier.align(Alignment.Center))
                }
                AppTab.LEARN -> {
                    Text("Scheda LEARN attiva", color = Color.White, modifier = Modifier.align(Alignment.Center))
                }
            }
        }

        //Navbar
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .height(68.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = navbarContainerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ){
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ){
                //Pulsante CHART
                NavBarItem(
                    title = "Chart",
                    emoji = "🗺️",
                    isSelected = currentTab == AppTab.CHART,
                    accentColor = accentColor,
                    onClick = { currentTab = AppTab.CHART }
                )

                NavBarItem(
                    title = "Graph",
                    emoji = "📊",
                    isSelected = currentTab == AppTab.GRAPH,
                    accentColor = accentColor,
                    onClick = { currentTab = AppTab.GRAPH }
                )

                NavBarItem(
                    title = "Learn",
                    emoji = "📚",
                    isSelected = currentTab == AppTab.LEARN,
                    accentColor = accentColor,
                    onClick = { currentTab = AppTab.LEARN}
                )
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
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.menuAnchor()
    ){
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ){
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
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
        modifier = Modifier.padding(horizontal = 0.dp)
    ){
        //Etichetta del Pilastro
        Text(text = label, style = MaterialTheme.typography.labelMedium)

        //Calcolo e visualizzazione del Ten God
        val tenGod = if (label == "Giorno"){
            "Day Master"
        } else if (dayMaster != null && pillar.stem != null){
            getTenGodName(dayMaster.element, pillar.stem.element, dayMaster.polarity, pillar.stem.polarity)
        }else ""

        Text(
            text = tenGod,
            style = MaterialTheme.typography.labelSmall,
            color = if(label == "Giorno") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.height(16.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        //Tronco Celeste (Heavenly Stem)
        val stemDescription = if (pillar.stem != null) {
            val elemStr = formatToLowercase(pillar.stem.element.name)
            val polStr = formatToLowercase(pillar.stem.polarity.name)
            "($elemStr $polStr)"
        } else ""

        val stemSubText = pillar.stem?.name?.let { formatToLowercase(it) } ?: ""

        BaZiCard(
            chinese = pillar.stem?.chinese, 
            subText = stemSubText,
            extendedText = stemDescription,
            element = pillar.stem?.element
        )

        Spacer(modifier = Modifier.height(4.dp))

        //Ramo Terrestre (Earthly Branch)
        val branchSubText = pillar.branch?.stemName ?: ""

        val branchDescription = if (pillar.branch != null) {
            val elemStr = formatToLowercase(pillar.branch.element.name)
            val polStr = formatToLowercase(pillar.branch.polarity.name)
            "($elemStr $polStr)"
        }else ""
        
        BaZiCard(
            chinese = pillar.branch?.chinese, 
            subText = branchSubText,
            extendedText = branchDescription, 
            element = pillar.branch?.element
        )
    }
}

@Composable
fun BaZiCard(chinese: String?, subText: String?, extendedText: String?, element: Element?){
    Card(
        modifier = Modifier.size(width = 80.dp, height = 115.dp),
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
            Text(text = chinese ?: "?", style = MaterialTheme.typography.headlineMedium, color = Color.Black)
            Text(text = subText ?: "", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)

            if(!extendedText.isNullOrEmpty()) {
                Text(
                    text = extendedText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RowScope.NavBarItem(
    title: String,
    emoji: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
){
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(text = emoji, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = if(isSelected) accentColor else Color.Gray
        )
    }
}