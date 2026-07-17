package com.fredcomms.baziapp.ui

import com.fredcomms.baziapp.R
import com.fredcomms.baziapp.logic.*
import java.util.Calendar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.animation.animateContentSize
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


enum class AppTab {CHART, GRAPH, LEARN}
enum class ChartStep {INPUT, LOADING, RESULTS}

data class ExtendedLearnItem(
    val category: String,
    val title: String,
    val subtitle: String = "",
    val description: String,
    val details: String = "",
    val emoji: String
)

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

    val months = remember {
        java.time.Month.values().map { month ->
            month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
                .replaceFirstChar { it.uppercase() }
        }
    }

    var years = remember { (1926..2036).map { it.toString() }.reversed() }
    var days = remember { (1..31).map { it.toString()} }
    var hours = remember { (0..23).map { it.toString().padStart(2, '0')} }
    var minutes = remember { (0..55 step 5).map { it.toString().padStart(2, '0')} }

    var currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()

    var selectedDay by remember { mutableStateOf("1") }
    var selectedMonth by remember { mutableStateOf(months[0]) }
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
                                    BaZiDropdown("Giorno", days, selectedDay, { selectedDay = it }, Modifier.weight(1.0f))
                                    BaZiDropdown("Mese", months, selectedMonth, { selectedMonth = it }, Modifier.weight(1.8f))
                                    BaZiDropdown("Anno", years, selectedYear, { selectedYear = it }, Modifier.weight(1.2f))
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

                                Text(text = "Luogo di Nascita", style = MaterialTheme.typography.labelMedium, color = Color.LightGray)

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
                                        },
                                        colors = TextFieldDefaults.outlinedTextFieldColors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            disabledTextColor = Color.LightGray,
                                            focusedLabelColor = Color.LightGray,
                                            unfocusedLabelColor = Color.Gray,
                                            focusedBorderColor = accentColor,
                                            unfocusedBorderColor = Color.Gray
                                        )
                                    )
                                }

                                Button(
                                    onClick = {
                                        selectedCity?.let { city ->
                                            currentChartStep = ChartStep.LOADING
                                            coroutineScope.launch {
                                                val monthNum = getMonthNum(context, selectedMonth)

                                                baziChart = getFullBaZi(
                                                    selectedYear.toInt(),
                                                    monthNum,
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
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor,
                                        contentColor = Color.Black,
                                        disabledContainerColor = Color(0xFF333333),
                                        disabledContentColor = Color.LightGray
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                                ){
                                    Text("Calcola Carta Natale")
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
                    val scores = baziChart?.let { calculateRoleScores(it) }

                    if(scores == null || scores.total == 0){
                        //se manca la mappa, invito a calcolarla
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ){
                            Column(horizontalAlignment = Alignment.CenterHorizontally){
                                Text("⚠️", style = MaterialTheme.typography.run { headlineLarge })
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Nessun dato disponibile.\n Calcola prima la tua mappa !",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }else{
                        val dmElement = baziChart?.day?.stem?.element ?: Element.WOOD


                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Text(
                                text = "I 5 fattori di Vita",
                                style = MaterialTheme.typography.headlineMedium,
                                color = accentColor
                            )
                            Text(
                                text = "Analisi energetica basata su Day Master (DM)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            BaZiPieChart(scores = scores, dmElement = dmElement)

                            Spacer(modifier = Modifier.height(32.dp))

                            Text(
                                text = "Dettaglio delle Relazioni",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                            )


                            val outElement = Element.entries.find { isProducing(dmElement, it) } ?: dmElement
                            val wthElement = Element.entries.find { isControlling(dmElement, it) } ?: dmElement
                            val offElement = Element.entries.find { isControlling(it, dmElement) } ?: dmElement
                            val rscElement = Element.entries.find { isProducing(it, dmElement) } ?: dmElement

                            RoleRow(label = "Self / Friends (Bi Jian)", count = scores.friend, max = scores.total, color = Color(getElementColor(dmElement)))
                            RoleRow(label = "Rob Wealth (Jie Cai)", count = scores.robWealth, max = scores.total, color = Color(getElementColor(dmElement)).copy(alpha = 0.6f))
                            RoleRow(label = "Output (Espressione/Intelletto)", count = scores.output, max = scores.total, color = Color(getElementColor(outElement)))
                            RoleRow(label = "Wealth (Finanze/Obiettivi)", count = scores.wealth, max = scores.total, color = Color(getElementColor(wthElement)))
                            RoleRow(label = "Officer (Disciplina/Status)", count = scores.officer, max = scores.total, color = Color(getElementColor(offElement)))
                            RoleRow(label = "Resource (Studio/Mente)", count = scores.resource, max = scores.total, color = Color(getElementColor(rscElement)))
                        }
                    }
                }
                AppTab.LEARN -> {
                    var expandedTitle by remember { mutableStateOf<String?>(null) }

                    val glossary = listOf(
                        //Elementi e Cicli
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_elem),
                            title = stringResource(R.string.wood_title),
                            subtitle = stringResource(R.string.wood_subtitle),
                            description = stringResource(R.string.wood_desc),
                            details = stringResource(R.string.wood_details),
                            emoji = "木"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_elem),
                            title = stringResource(R.string.fire_title),
                            subtitle = stringResource(R.string.fire_subtitle),
                            description = stringResource(R.string.fire_desc),
                            details = stringResource(R.string.fire_details),
                            emoji = "火"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_elem),
                            title = stringResource(R.string.metal_title),
                            subtitle = stringResource(R.string.metal_subtitle),
                            description = stringResource(R.string.metal_desc),
                            details = stringResource(R.string.metal_details),
                            emoji = "金"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_elem),
                            title = stringResource(R.string.water_title),
                            subtitle = stringResource(R.string.water_subtitle),
                            description = stringResource(R.string.water_desc),
                            details = stringResource(R.string.water_details),
                            emoji = "水"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_elem),
                            title = stringResource(R.string.earth_title),
                            subtitle = stringResource(R.string.earth_subtitle),
                            description = stringResource(R.string.earth_desc),
                            details = stringResource(R.string.earth_details),
                            emoji = "土"
                        ),

                        //Cicli del Qi
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_cycles),
                            title = stringResource(R.string.cy1_title),
                            subtitle = stringResource(R.string.cy1_subtitle),
                            description = stringResource(R.string.cy1_desc),
                            details = stringResource(R.string.cy1_details),
                            emoji = "➕"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_cycles),
                            title = stringResource(R.string.cy2_title),
                            subtitle = stringResource(R.string.cy2_subtitle),
                            description = stringResource(R.string.cy2_desc),
                            details = stringResource(R.string.cy2_details),
                            emoji = "➖"
                        ),


                        //Tronchi Celesti
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_stems),
                            title = stringResource(R.string.st1_title),
                            subtitle = stringResource(R.string.st1_subtitle),
                            description = stringResource(R.string.st1_desc),
                            details = stringResource(R.string.st1_details),
                            emoji = "🌲"
                        ),
                        
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_stems),
                            title = stringResource(R.string.st2_title),
                            subtitle = stringResource(R.string.st2_subtitle),
                            description = stringResource(R.string.st2_desc),
                            details = stringResource(R.string.st2_details),
                            emoji = "🌸"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_stems),
                            title = stringResource(R.string.st3_title),
                            subtitle = stringResource(R.string.st3_subtitle),
                            description = stringResource(R.string.st3_desc),
                            details = stringResource(R.string.st3_details),
                            emoji = "⛰️"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_stems),
                            title = stringResource(R.string.st4_title),
                            subtitle = stringResource(R.string.st4_subtitle),
                            description = stringResource(R.string.st4_desc),
                            details = stringResource(R.string.st4_details),
                            emoji = "🌱"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_stems),
                            title = stringResource(R.string.st5_title),
                            subtitle = stringResource(R.string.st5_subtitle),
                            description = stringResource(R.string.st5_desc),
                            details = stringResource(R.string.st5_details),
                            emoji = "🌊"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_stems),
                            title = stringResource(R.string.st6_title),
                            subtitle = stringResource(R.string.st6_subtitle),
                            description = stringResource(R.string.st6_desc),
                            details = stringResource(R.string.st6_details),
                            emoji = "💧"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_stems),
                            title = stringResource(R.string.st7_title),
                            subtitle = stringResource(R.string.st7_subtitle),
                            description = stringResource(R.string.st7_desc),
                            details = stringResource(R.string.st7_details),
                            emoji = "☀️"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_stems),
                            title = stringResource(R.string.st8_title),
                            subtitle = stringResource(R.string.st8_subtitle),
                            description = stringResource(R.string.st8_desc),
                            details = stringResource(R.string.st8_details),
                            emoji = "🕯️"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_stems),
                            title = stringResource(R.string.st9_title),
                            subtitle = stringResource(R.string.st9_subtitle),
                            description = stringResource(R.string.st9_desc),
                            details = stringResource(R.string.st9_details),
                            emoji = "🗡️"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_stems),
                            title = stringResource(R.string.st10_title),
                            subtitle = stringResource(R.string.st10_subtitle),
                            description = stringResource(R.string.st10_desc),
                            details = stringResource(R.string.st10_details),
                            emoji = "💍"
                        ),
                        //Rami Terrestri
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_branch),
                            title = stringResource(R.string.br1_title),
                            subtitle = stringResource(R.string.br1_subtitle),
                            description = stringResource(R.string.br1_desc),
                            details = stringResource(R.string.br1_details),
                            emoji = "🐭",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_branch),
                            title = stringResource(R.string.br2_title),
                            subtitle = stringResource(R.string.br2_subtitle),
                            description = stringResource(R.string.br2_desc),
                            details = stringResource(R.string.br2_details),
                            emoji = "🐮"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_branch),
                            title = stringResource(R.string.br3_title),
                            subtitle = stringResource(R.string.br3_subtitle),
                            description = stringResource(R.string.br3_desc),
                            details = stringResource(R.string.br3_details),
                            emoji = "🐯"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_branch),
                            title = stringResource(R.string.br4_title),
                            subtitle = stringResource(R.string.br4_subtitle),
                            description = stringResource(R.string.br4_desc),
                            details = stringResource(R.string.br4_details),
                            emoji = "🐰"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_branch),
                            title = stringResource(R.string.br5_title),
                            subtitle = stringResource(R.string.br5_subtitle),
                            description = stringResource(R.string.br5_desc),
                            details = stringResource(R.string.br5_details),
                            emoji = "🐲"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_branch),
                            title = stringResource(R.string.br6_title),
                            subtitle = stringResource(R.string.br6_subtitle),
                            description = stringResource(R.string.br6_desc),
                            details = stringResource(R.string.br6_details),
                            emoji = "🐍"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_branch),
                            title = stringResource(R.string.br7_title),
                            subtitle = stringResource(R.string.br7_subtitle),
                            description = stringResource(R.string.br7_desc),
                            details = stringResource(R.string.br7_details),
                            emoji = "🐴"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_branch),
                            title = stringResource(R.string.br8_title),
                            subtitle = stringResource(R.string.br8_subtitle),
                            description = stringResource(R.string.br8_desc),
                            details = stringResource(R.string.br8_details),
                            emoji = "🐐"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_branch),
                            title = stringResource(R.string.br9_title),
                            subtitle = stringResource(R.string.br9_subtitle),
                            description = stringResource(R.string.br9_desc),
                            details = stringResource(R.string.br9_details),
                            emoji = "🐵"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_branch),
                            title = stringResource(R.string.br10_title),
                            subtitle = stringResource(R.string.br10_subtitle),
                            description = stringResource(R.string.br10_desc),
                            details = stringResource(R.string.br10_details),
                            emoji = "🐔"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_branch),
                            title = stringResource(R.string.br11_title),
                            subtitle = stringResource(R.string.br11_subtitle),
                            description = stringResource(R.string.br11_desc),
                            details = stringResource(R.string.br11_details),
                            emoji = "🐶"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_branch),
                            title = stringResource(R.string.br12_title),
                            subtitle = stringResource(R.string.br12_subtitle),
                            description = stringResource(R.string.br12_desc),
                            details = stringResource(R.string.br12_details),
                            emoji = "🐷"
                        ),
                        //I Dieci Dei

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_tg),
                            title = stringResource(R.string.tg0_title),
                            subtitle = stringResource(R.string.tg0_subtitle),
                            description = stringResource(R.string.tg0_desc),
                            emoji = "🔟",
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_tg),
                            title = stringResource(R.string.tg1_title),
                            subtitle = stringResource(R.string.tg1_subtitle),
                            description = stringResource(R.string.tg1_desc),
                            details = stringResource(R.string.tg1_details),
                            emoji = "🤝",
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_tg),
                            title = stringResource(R.string.tg2_title),
                            subtitle = stringResource(R.string.tg2_subtitle),
                            description = stringResource(R.string.tg2_desc),
                            details = stringResource(R.string.tg2_details),
                            emoji = "💰",
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_tg),
                            title = stringResource(R.string.tg3_title),
                            subtitle = stringResource(R.string.tg3_subtitle),
                            description = stringResource(R.string.tg3_desc),
                            details = stringResource(R.string.tg3_details),
                            emoji = "🎨",
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_tg),
                            title = stringResource(R.string.tg4_title),
                            subtitle = stringResource(R.string.tg4_subtitle),
                            description = stringResource(R.string.tg4_desc),
                            details = stringResource(R.string.tg4_details),
                            emoji = "📣",
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_tg),
                            title = stringResource(R.string.tg5_title),
                            subtitle = stringResource(R.string.tg5_subtitle),
                            description = stringResource(R.string.tg5_desc),
                            details = stringResource(R.string.tg5_details),
                            emoji = "💼",
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_tg),
                            title = stringResource(R.string.tg6_title),
                            subtitle = stringResource(R.string.tg6_subtitle),
                            description = stringResource(R.string.tg6_desc),
                            details = stringResource(R.string.tg6_details),
                            emoji = "🎲",
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_tg),
                            title = stringResource(R.string.tg7_title),
                            subtitle = stringResource(R.string.tg7_subtitle),
                            description = stringResource(R.string.tg7_desc),
                            details = stringResource(R.string.tg7_details),
                            emoji = "⚖️",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_tg),
                            title = stringResource(R.string.tg8_title),
                            subtitle = stringResource(R.string.tg8_subtitle),
                            description = stringResource(R.string.tg8_desc),
                            details = stringResource(R.string.tg8_details),
                            emoji = "🎯",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_tg),
                            title = stringResource(R.string.tg9_title),
                            subtitle = stringResource(R.string.tg9_subtitle),
                            description = stringResource(R.string.tg9_desc),
                            details = stringResource(R.string.tg9_details),
                            emoji = "📚",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_tg),
                            title = stringResource(R.string.tg10_title),
                            subtitle = stringResource(R.string.tg10_subtitle),
                            description = stringResource(R.string.tg10_desc),
                            details = stringResource(R.string.tg10_details),
                            emoji = "🔮",
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_hst),
                            title = stringResource(R.string.hst_title),
                            subtitle = stringResource(R.string.hst_subtitle),
                            description = stringResource(R.string.hst_desc),
                            emoji = "🕳️",
                        ),

                        //Interazioni
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_interacts),
                            title = stringResource(R.string.i0_title),
                            subtitle = stringResource(R.string.i0_subtitle),
                            description = stringResource(R.string.i0_desc),
                            details = stringResource(R.string.i0_details),
                            emoji = "🔄",
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_interacts),
                            title = stringResource(R.string.i1_title),
                            subtitle = stringResource(R.string.i1_subtitle),
                            description = stringResource(R.string.i1_desc),
                            details = stringResource(R.string.i1_details),
                            emoji = "🤝"
                        ),
                        
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_interacts),
                            title = stringResource(R.string.i2_title),
                            subtitle = stringResource(R.string.i2_subtitle),
                            description = stringResource(R.string.i2_desc),
                            details = stringResource(R.string.i2_details),
                            emoji = "⚡"
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_interacts),
                            title = stringResource(R.string.i3_title),
                            subtitle = stringResource(R.string.i3_subtitle),
                            description = stringResource(R.string.i3_desc),
                            details = stringResource(R.string.i3_details),
                            emoji = "👥"
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_interacts),
                            title = stringResource(R.string.i4_title),
                            subtitle = stringResource(R.string.i4_subtitle),
                            description = stringResource(R.string.i4_desc),
                            details = stringResource(R.string.i4_details),
                            emoji = "🔺"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_interacts),
                            title = stringResource(R.string.i5_title),
                            subtitle = stringResource(R.string.i5_subtitle),
                            description = stringResource(R.string.i5_desc),
                            details = stringResource(R.string.i5_details),
                            emoji = "💥"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_interacts),
                            title = stringResource(R.string.i6_title),
                            subtitle = stringResource(R.string.i6_subtitle),
                            description = stringResource(R.string.i6_desc),
                            details = stringResource(R.string.i6_details),
                            emoji = "⏳"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_interacts),
                            title = stringResource(R.string.i7_title),
                            subtitle = stringResource(R.string.i7_subtitle),
                            description = stringResource(R.string.i7_desc),
                            details = stringResource(R.string.i7_details),
                            emoji = "💔"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_interacts),
                            title = stringResource(R.string.i8_title),
                            subtitle = stringResource(R.string.i8_subtitle),
                            description = stringResource(R.string.i8_desc),
                            details = stringResource(R.string.i8_details),
                            emoji = "🗑️"
                        ),

                        //Fasi del Qi

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_qi_intro),
                            title = stringResource(R.string.qi_intro_title),
                            subtitle = stringResource(R.string.qi_intro_subtitle),
                            description = stringResource(R.string.qi_intro_desc),
                            emoji = "☯️",
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_qi),
                            title = stringResource(R.string.q1_title),
                            subtitle = stringResource(R.string.q1_subtitle),
                            description = stringResource(R.string.q1_desc),
                            details = stringResource(R.string.q1_details),
                            emoji = "🤱"
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_qi),
                            title = stringResource(R.string.q2_title),
                            subtitle = stringResource(R.string.q2_subtitle),
                            description = stringResource(R.string.q2_desc),
                            details = stringResource(R.string.q2_details),
                            emoji = "👶"
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_qi),
                            title = stringResource(R.string.q3_title),
                            subtitle = stringResource(R.string.q3_subtitle),
                            description = stringResource(R.string.q3_desc),
                            details = stringResource(R.string.q3_details),
                            emoji = "🎓"
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_qi),
                            title = stringResource(R.string.q4_title),
                            subtitle = stringResource(R.string.q4_subtitle),
                            description = stringResource(R.string.q4_desc),
                            details = stringResource(R.string.q4_details),
                            emoji = "💼"
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_qi),
                            title = stringResource(R.string.q5_title),
                            subtitle = stringResource(R.string.q5_subtitle),
                            description = stringResource(R.string.q5_desc),
                            details = stringResource(R.string.q5_details),
                            emoji = "👑"
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_qi),
                            title = stringResource(R.string.q6_title),
                            subtitle = stringResource(R.string.q6_subtitle),
                            description = stringResource(R.string.q6_desc),
                            details = stringResource(R.string.q6_details),
                            emoji = "👴"
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_qi),
                            title = stringResource(R.string.q7_title),
                            subtitle = stringResource(R.string.q7_subtitle),
                            description = stringResource(R.string.q7_desc),
                            details = stringResource(R.string.q7_details),
                            emoji = "🛌"
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_qi),
                            title = stringResource(R.string.q8_title),
                            subtitle = stringResource(R.string.q8_subtitle),
                            description = stringResource(R.string.q8_desc),
                            details = stringResource(R.string.q8_details),
                            emoji = "🧘"
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_qi),
                            title = stringResource(R.string.q9_title),
                            subtitle = stringResource(R.string.q9_subtitle),
                            description = stringResource(R.string.q9_desc),
                            details = stringResource(R.string.q9_details),
                            emoji = "📦"
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_qi),
                            title = stringResource(R.string.q10_title),
                            subtitle = stringResource(R.string.q10_subtitle),
                            description = stringResource(R.string.q10_desc),
                            details = stringResource(R.string.q10_details),
                            emoji = "🕳️"
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_qi),
                            title = stringResource(R.string.q11_title),
                            subtitle = stringResource(R.string.q11_subtitle),
                            description = stringResource(R.string.q11_desc),
                            details = stringResource(R.string.q11_details),
                            emoji = "🌱"
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_qi),
                            title = stringResource(R.string.q12_title),
                            subtitle = stringResource(R.string.q12_subtitle),
                            description = stringResource(R.string.q12_desc),
                            details = stringResource(R.string.q12_details),
                            emoji = "🥚"
                        ),
                        
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_nayin),
                            title = stringResource(R.string.ny_intro_title),
                            subtitle = stringResource(R.string.ny_intro_subtitle),
                            description = stringResource(R.string.ny_intro_desc),
                            emoji = "🎵",
                        ),

                        
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_nayin),
                            title = stringResource(R.string.ny_math_title),
                            subtitle = stringResource(R.string.ny_math_subtitle),
                            description = stringResource(R.string.ny_math_desc),
                            emoji = "🌀",
                        ),
                        //Lista dei 30 Na Yin
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_metal),
                            title = stringResource(R.string.ny_m1_title),
                            subtitle = stringResource(R.string.ny_m1_subtitle),
                            description = stringResource(R.string.ny_m1_desc),
                            details = stringResource(R.string.ny_m1_details),
                            emoji = "🦪",
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_metal),
                            title = stringResource(R.string.ny_m2_title),
                            subtitle = stringResource(R.string.ny_m2_subtitle),
                            description = stringResource(R.string.ny_m2_desc),
                            details = stringResource(R.string.ny_m2_details),
                            emoji = "⚔️",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_metal),
                            title = stringResource(R.string.ny_m3_title),
                            subtitle = stringResource(R.string.ny_m3_subtitle),
                            description = stringResource(R.string.ny_m3_desc),
                            details = stringResource(R.string.ny_m3_details),
                            emoji = "🕯️",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_metal),
                            title = stringResource(R.string.ny_m4_title),
                            subtitle = stringResource(R.string.ny_m4_subtitle),
                            description = stringResource(R.string.ny_m4_desc),
                            details = stringResource(R.string.ny_m4_details),
                            emoji = "🏆",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_metal),
                            title = stringResource(R.string.ny_m5_title),
                            subtitle = stringResource(R.string.ny_m5_subtitle),
                            description = stringResource(R.string.ny_m5_desc),
                            details = stringResource(R.string.ny_m5_details),
                            emoji = "💎",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_metal),
                            title = stringResource(R.string.ny_m6_title),
                            subtitle = stringResource(R.string.ny_m6_subtitle),
                            description = stringResource(R.string.ny_m6_desc),
                            details = stringResource(R.string.ny_m6_details),
                            emoji = "📿",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_water),
                            title = stringResource(R.string.ny_w1_title),
                            subtitle = stringResource(R.string.ny_w1_subtitle),
                            description = stringResource(R.string.ny_w1_desc),
                            details = stringResource(R.string.ny_w1_details),
                            emoji = "💧",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_water),
                            title = stringResource(R.string.ny_w2_title),
                            subtitle = stringResource(R.string.ny_w2_subtitle),
                            description = stringResource(R.string.ny_w2_desc),
                            details = stringResource(R.string.ny_w2_details),
                            emoji = "⛲",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_water),
                            title = stringResource(R.string.ny_w3_title),
                            subtitle = stringResource(R.string.ny_w3_subtitle),
                            description = stringResource(R.string.ny_w3_desc),
                            details = stringResource(R.string.ny_w3_details),
                            emoji = "🚣",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_water),
                            title = stringResource(R.string.ny_w4_title),
                            subtitle = stringResource(R.string.ny_w4_subtitle),
                            description = stringResource(R.string.ny_w4_desc),
                            details = stringResource(R.string.ny_w4_details),
                            emoji = "🌧️",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_water),
                            title = stringResource(R.string.ny_w5_title),
                            subtitle = stringResource(R.string.ny_w5_subtitle),
                            description = stringResource(R.string.ny_w5_desc),
                            details = stringResource(R.string.ny_w5_details),
                            emoji = "🏞️",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_water),
                            title = stringResource(R.string.ny_w6_title),
                            subtitle = stringResource(R.string.ny_w6_subtitle),
                            description = stringResource(R.string.ny_w6_desc),
                            details = stringResource(R.string.ny_w6_details),
                            emoji = "🌊",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_wood),
                            title = stringResource(R.string.ny_wd1_title),
                            subtitle = stringResource(R.string.ny_wd1_subtitle),
                            description = stringResource(R.string.ny_wd1_desc),
                            details = stringResource(R.string.ny_wd1_details),
                            emoji = "🌳",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_wood),
                            title = stringResource(R.string.ny_wd2_title),
                            subtitle = stringResource(R.string.ny_wd2_subtitle),
                            description = stringResource(R.string.ny_wd2_desc),
                            details = stringResource(R.string.ny_wd2_details),
                            emoji = "🍃",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_wood),
                            title = stringResource(R.string.ny_wd3_title),
                            subtitle = stringResource(R.string.ny_wd3_subtitle),
                            description = stringResource(R.string.ny_wd3_desc),
                            details = stringResource(R.string.ny_wd3_details),
                            emoji = "🌿",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_wood),
                            title = stringResource(R.string.ny_wd4_title),
                            subtitle = stringResource(R.string.ny_wd4_subtitle),
                            description = stringResource(R.string.ny_wd4_desc),
                            details = stringResource(R.string.ny_wd4_details),
                            emoji = "🌾",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_wood),
                            title = stringResource(R.string.ny_wd5_title),
                            subtitle = stringResource(R.string.ny_wd5_subtitle),
                            description = stringResource(R.string.ny_wd5_desc),
                            details = stringResource(R.string.ny_wd5_details),
                            emoji = "🐛",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_wood),
                            title = stringResource(R.string.ny_wd6_title),
                            subtitle = stringResource(R.string.ny_wd6_subtitle),
                            description = stringResource(R.string.ny_wd6_desc),
                            details = stringResource(R.string.ny_wd6_details),
                            emoji = "🍎",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_fire),
                            title = stringResource(R.string.ny_f1_title),
                            subtitle = stringResource(R.string.ny_f1_subtitle),
                            description = stringResource(R.string.ny_f1_desc),
                            details = stringResource(R.string.ny_f1_details),
                            emoji = "🔥",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_fire),
                            title = stringResource(R.string.ny_f2_title),
                            subtitle = stringResource(R.string.ny_f2_subtitle),
                            description = stringResource(R.string.ny_f2_desc),
                            details = stringResource(R.string.ny_f2_details),
                            emoji = "🌋",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_fire),
                            title = stringResource(R.string.ny_f3_title),
                            subtitle = stringResource(R.string.ny_f3_subtitle),
                            description = stringResource(R.string.ny_f3_desc),
                            details = stringResource(R.string.ny_f3_details),
                            emoji = "⛺",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_fire),
                            title = stringResource(R.string.ny_f4_title),
                            subtitle = stringResource(R.string.ny_f4_subtitle),
                            description = stringResource(R.string.ny_f4_desc),
                            details = stringResource(R.string.ny_f4_details),
                            emoji = "⚡",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_fire),
                            title = stringResource(R.string.ny_f5_title),
                            subtitle = stringResource(R.string.ny_f5_subtitle),
                            description = stringResource(R.string.ny_f5_desc),
                            details = stringResource(R.string.ny_f5_details),
                            emoji = "🏮",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_fire),
                            title = stringResource(R.string.ny_f6_title),
                            subtitle = stringResource(R.string.ny_f6_subtitle),
                            description = stringResource(R.string.ny_f6_desc),
                            details = stringResource(R.string.ny_f6_details),
                            emoji = "🌅",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_earth),
                            title = stringResource(R.string.ny_e1_title),
                            subtitle = stringResource(R.string.ny_e1_subtitle),
                            description = stringResource(R.string.ny_e1_desc),
                            details = stringResource(R.string.ny_e1_details),
                            emoji = "🥾",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_earth),
                            title = stringResource(R.string.ny_e2_title),
                            subtitle = stringResource(R.string.ny_e2_subtitle),
                            description = stringResource(R.string.ny_e2_desc),
                            details = stringResource(R.string.ny_e2_details),
                            emoji = "🏯",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_earth),
                            title = stringResource(R.string.ny_e3_title),
                            subtitle = stringResource(R.string.ny_e3_subtitle),
                            description = stringResource(R.string.ny_e3_desc),
                            details = stringResource(R.string.ny_e3_details),
                            emoji = "🏚️",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_earth),
                            title = stringResource(R.string.ny_e4_title),
                            subtitle = stringResource(R.string.ny_e4_subtitle),
                            description = stringResource(R.string.ny_e4_desc),
                            details = stringResource(R.string.ny_e4_details),
                            emoji = "🧱",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_earth),
                            title = stringResource(R.string.ny_e5_title),
                            subtitle = stringResource(R.string.ny_e5_subtitle),
                            description = stringResource(R.string.ny_e5_desc),
                            details = stringResource(R.string.ny_e5_details),
                            emoji = "🛣️",
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_ny_earth),
                            title = stringResource(R.string.ny_e6_title),
                            subtitle = stringResource(R.string.ny_e6_subtitle),
                            description = stringResource(R.string.ny_e6_desc),
                            details = stringResource(R.string.ny_e6_details),
                            emoji = "⏳",
                        ),

                        //Stelle simboliche

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_stars),
                            title = stringResource(R.string.star_intro_title),
                            subtitle = stringResource(R.string.star_intro_subtitle),
                            description = stringResource(R.string.star_intro_desc),
                            emoji = "✨"
                        ),

                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_protect),
                            title = stringResource(R.string.star_rclouds_title),
                            subtitle = stringResource(R.string.star_rclouds_subtitle),
                            description = stringResource(R.string.star_rclouds_desc),
                            details = stringResource(R.string.star_rclouds_details),
                            emoji = "🌹"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_career),
                            title = stringResource(R.string.star_car_title),
                            subtitle = stringResource(R.string.star_car_subtitle),
                            description = stringResource(R.string.star_car_desc),
                            details = stringResource(R.string.star_car_details),
                            emoji = "👑"
                        ),
                        ExtendedLearnItem(
                            category = stringResource(R.string.cat_protect),
                            title = stringResource(R.string.star_doc_title),
                            subtitle = stringResource(R.string.star_doc_subtitle),
                            description = stringResource(R.string.star_doc_desc),
                            details = stringResource(R.string.star_doc_details),
                            emoji = "🏥"
                        ),
                        

                    )
                    val groupedItems = glossary.groupBy { it.category }


                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ){
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Manuale BaZi Interattivo",
                            style = MaterialTheme.typography.headlineMedium,
                            color = accentColor
                        )
                        Text(
                            text = "Tocca un argomento per espandere la spiegazione guidata.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ){
                            groupedItems.forEach { (categoryName, itemsInCategory) ->
                                item {
                                    Text(
                                        text = categoryName.uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = accentColor,
                                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                    )
                                }

                                items(itemsInCategory) { item ->
                                    val isExpanded = expandedTitle == item.title

                                    InteractiveLearnCard (
                                        item = item,
                                        isExpanded = isExpanded,
                                        onClick = {
                                            expandedTitle = if (isExpanded) null else item.title
                                        }
                                    )
                                }
                            } 
                        }
                    }
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
    
    

@Composable
fun BaZiDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
){
    var expanded by remember { mutableStateOf(false)}
    var textFieldWidth by remember { mutableStateOf(0) }

    Box(modifier = modifier){
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                enabled = true,
                label = { Text(label) },
                trailingIcon = { 
                    Text(if (expanded) "🔼" else "🔽", modifier = Modifier.padding(end = 8.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        textFieldWidth = coordinates.size.width
                    },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.LightGray,
                    unfocusedLabelColor = Color.LightGray,
                    focusedBorderColor = Color(0xFFFFCA28),
                    unfocusedBorderColor = Color.Gray,
                    focusedTrailingIconColor = Color.White,
                    unfocusedTrailingIconColor = Color.Gray
                )
            )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                    .width(with(LocalDensity.current){ textFieldWidth.toDp() })
                    .background(Color(0xFF1E1E1E))
        ){
            val isLongList = options.size > 12
            val menuHeight = if (isLongList) 240.dp else 400.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(menuHeight)
                    .verticalScroll(rememberScrollState())
            ){
                options.forEach { option ->
                    val isSelected = (option == selectedOption)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) Color(0xFF2C2C2C) else Color(0xFF1E1E1E))
                            .clickable{
                                onOptionSelected(option)
                                expanded = false
                            }
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                    ){
                        Text(
                            text = option,
                            color = if (isSelected) Color(0xFFFFCA28) else Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PillarDisplay(label: String, pillar: Pillar, dayMaster: Stem?){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 0.dp)
    ){
        //Etichetta del Pilastro
        Text(
             text = label,
             style = MaterialTheme.typography.labelMedium,
             color = Color(0xFF32CD32),
             modifier = Modifier.padding(bottom = 4.dp)
        )

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
        val branchSubText = pillar.branch?.branchName ?: ""

        val branchDescription = if (pillar.branch != null) {
            val elemStr = formatToLowercase(pillar.branch.element.name)
            val polStr = formatToLowercase(pillar.branch.polarity.name)
            "($elemStr $polStr)"
        } else ""
        
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

@Composable
fun BaZiPieChart(scores: RoleScores, dmElement: Element){
    val outElement = Element.entries.find { isProducing(dmElement, it) } ?: dmElement
    val wthElement = Element.entries.find { isControlling(dmElement, it) } ?: dmElement
    val offElement = Element.entries.find { isControlling(it, dmElement) } ?: dmElement
    val rscElement = Element.entries.find { isProducing(it, dmElement) } ?: dmElement 

    val slices =  listOf(
        Pair(scores.companionTot, Color(getElementColor(dmElement))),
        Pair(scores.output, Color(getElementColor(outElement))),
        Pair(scores.wealth, Color(getElementColor(wthElement))),
        Pair(scores.officer, Color(getElementColor(offElement))),
        Pair(scores.resource, Color(getElementColor(rscElement)))
    )

    val total = scores.total.toFloat()
    var startAngle = -90f

    Box(
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ){
        Canvas(modifier = Modifier.fillMaxSize()){
            slices.forEach { slice ->
                if(slice.first > 0 && total > 0f){
                    val sweepAngle = (slice.first/ total) * 360f
                    drawArc(
                        color = slice.second,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                    startAngle += sweepAngle
                }
            }
        }

        Canvas(modifier = Modifier.size(100.dp)){
            drawCircle(color = Color(0xFF121212))
        }
    }
}

@Composable
fun RoleRow(label: String, count: Int, max: Int, color: Color){
    val percentage = if (max > 0) (count.toFloat() / max.toFloat() * 100).toInt() else 0

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Text(text = "$count Caratteri ($percentage%)", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
        }

        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress =  if (max > 0) count.toFloat() / max.toFloat() else 0f,
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = Color.DarkGray
        )
    }
}

@Composable
fun InteractiveLearnCard(
    item: ExtendedLearnItem,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){
    val elementColors = mapOf(
        "木" to Color(0xFF2E7D32),
        "火" to Color(0xFFC62828),
        "土" to Color(0xFFEF6C00),
        "金" to Color(0xFF78909C),
        "水" to Color(0xFF1565C0)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ){

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                if(item.emoji.isNotEmpty()){
                    val elementBackgroundColor = elementColors[item.emoji]

                    if(elementBackgroundColor != null){
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(36.dp)
                                .background(elementBackgroundColor, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                text = item.emoji,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }else{
                        Text(
                            text = item.emoji,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                }
            

                Column(modifier = Modifier.weight(1f)){
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )

                        Text(
                            text = if (isExpanded) "🔼" else "🔽",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    if(item.subtitle.isNotEmpty()){
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFCA28)
                        )
                    }
                }
            }
            if(isExpanded){

                if(item.description.isNotEmpty()){
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if(item.details.isNotEmpty()){
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ){
                        Text(
                            text = item.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF66BB6A)
                        )
                    }
                }
            }
        }
    }
}

