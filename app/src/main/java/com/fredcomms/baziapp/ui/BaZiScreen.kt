package com.fredcomms.baziapp.ui

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
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
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor,
                                        contentColor = Color.Black,
                                        disabledContainerColor = Color(0xFF333333),
                                        disabledContentColor = Color.LightGray
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                                ){
                                    Text("Calcola Mappa")
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

                        //Tronchi Celesti
                        ExtendedLearnItem(
                            category = "I 10 Tronchi Celesti (Heavenly Stems)",
                            title = "Jia (甲) - Legno Yang",
                            subtitle = "L'Albero Secolare - Leader Nato",
                            description = "Rappresenta un grande albero robusto. Le persone Jia sono idealiste, progressiste, testarde ma estremamente protettive" + 
                            "verso gli altri. Hanno una crescita costante e non amano essere controllate. ",
                            details = "Elemento: Legno | Polarità: Yang | Direzione: Est",
                            emoji = "🌲"
                        ),
                        
                        ExtendedLearnItem(
                            category = "I 10 Tronchi Celesti (Heavenly Stems)",
                            title = "Yi (乙) - Legno Yin",
                            subtitle = "L'Edera o l'Erba - Il Sopravvissuto",
                            description = "Rappresenta piante flessibili, fiori o rampicanti. Le persone Yi sono diplomatiche, incredibilmente resilienti," + 
                            "e sanno adattarsi a qualsiasi situazione per sopravvivere. Ottimi strateghi dietro le quinte. ",
                            details = "Elemento: Legno | Polarità: Yin | Direzione: Est",
                            emoji = "🌸"
                        ),
                        ExtendedLearnItem(
                            category = "I 10 Tronchi Celesti (Heavenly Stems)",
                            title = "Wu (戊) - Terra Yang",
                            subtitle = "La Montagna o la Roccia - Il Protettore",
                            description = "Rappresenta una montagna maestosa o una roccia imponente. Le persone Wu sono stabili, affidabili, protettive e agiscono " +
                            "come un punto di riferimento per la comunità. Possono risultare lente al cambiamento ma sono incrollabili.",
                            details = "Elemento: Terra | Polarità: Yang | Direzione: Centro",
                            emoji = "⛰️"
                        ),
                        ExtendedLearnItem(
                            category = "I 10 Tronchi Celesti (Heavenly Stems)",
                            title = "Ji (己) - Terra Yin",
                            subtitle = "Il Giardino o la Terra Fertile - Il Nutritore",
                            description = "Rappresenta il terreno fertile dei campi o il giardino coltivato. Le persone Ji sono tolleranti, creative, capaci di accudire" +
                            "gli altri e dotate di una memoria eccezionale. Trovano la propria forza nel coltivare talenti e relazioni.",
                            details = "Elemento: Terra | Polarità: Yin | Direzione: Centro",
                            emoji = "🌱"
                        ),
                        ExtendedLearnItem(
                            category = "I 10 Tronchi Celesti (Heavenly Stems)",
                            title = "Ren (壬) - Acqua Yang",
                            subtitle = "L'Oceano o il Fiume - Il Visionario",
                            description = "Rappresenta le acque dell'oceano, dei grandi fiumi o delle cascate. Le persone Ren sono dinamiche, amano la libertà, " +
                            "possiedono un'intelligenza fluida e sono in continuo movimento. Hanno una grande influenza sul proprio ambiente.",
                            details = "Elemento: Acqua | Polarità: Yang | Direzione: Nord",
                            emoji = "🌊"
                        ),
                        ExtendedLearnItem(
                            category = "I 10 Tronchi Celesti (Heavenly Stems)",
                            title = "Gui (癸) - Acqua Yin",
                            subtitle = "La Rugiada o la Pioggia Leggera - L'Intuitivo",
                            description = "Rappresenta la pioggia leggera, la rugiada mattutina o i ruscelli di montagna. Le persone Gui sono introspettive, intuitive, " + 
                            "gentili ma capaci di infiltrarsi ovunque con costanza. Possiedono una spiccata sensibilità psicologica.",
                            details = "Elemento: Acqua | Polarità: Yin | Direzione: Nord",
                            emoji = "💧"
                        ),
                        ExtendedLearnItem(
                            category = "I 10 Tronchi Celesti (Heavenly Stems)",
                            title = "Bing (丙) - Fuoco Yang",
                            subtitle = "Il Sole - Il Magnanimo",
                            description = "Rappresenta la luce e il calore del sole nel cielo. Le persone Bing sono generose, passionali, piene di entusiasmo " +
                            "e amano illuminare la vita altrui. Hanno un forte bisogno di espressione e trasparenza.",
                            details = "Elemento: Fuoco | Polarità: Yang | Direzione: Sud",
                            emoji = "☀️"
                        ),
                        ExtendedLearnItem(
                            category = "I 10 Tronchi Celesti (Heavenly Stems)",
                            title = "Ding (丙) - Fuoco Yin",
                            subtitle = "La Candela o la Lanterna - Il Mentore",
                            description = "Rappresenta la fiamma di una candela, una lanterna o un focolare. Le persone Ding sono meticolose, guidate dall'intuito, " +
                            "profondamente stabili ma capaci di infiammarsi improvvisamente. Sono leader motivatori stabili nel tempo.",
                            details = "Elemento: Fuoco | Polarità: Yin | Direzione: Sud",
                            emoji = "🏮"
                        ),
                        ExtendedLearnItem(
                            category = "I 10 Tronchi Celesti (Heavenly Stems)",
                            title = "Geng (庚) - Metallo Yang",
                            subtitle = "La Spada o il Ferro Grezzo - Il Giusto",
                            description = "Rappresenta il metallo grezzo, la spada o l'ascia d'acciaio. Le persone Geng sono governate dal senso di giustizia, "+ 
                            "sono determinate, amano le sfide e affrontano i problemi in modo diretto. Crescono attraverso le avversità.",
                            details = "Elemento: Metallo | Polarità: Yang | Direzione: Ovest",
                            emoji = "🗡️"
                        ),
                        ExtendedLearnItem(
                            category = "I 10 Tronchi Celesti (Heavenly Stems)",
                            title = "Xin (辛) - Metallo Yin",
                            subtitle = "Il Gioiello o lo Spillo - L'Esteta",
                            description = "Rappresenta l'oro lavorato, i gioielli preziosi o i metalli fini. Le persone Xin sono eleganti, sensibili ai dettagli, " +
                            "amano risplendere e cercano la perfezione estetica. Hanno una mente acuta come uno spillo.",
                            details = "Elemento: Metallo | Polarità: Yin | Direzione: Ovest",
                            emoji = "💍"
                        ),
                        //Rami Terrestri
                        ExtendedLearnItem(
                            category = "I 12 Rami Terrestri (Zodiaco Cinese)",
                            title = "Zi (子) - Il Topo (The Rat)",
                            subtitle = "Acqua Yang - L'Iniziatore",
                            description = "Il primo segno dello zodiaco. Rappresenta l'energia dell'inverno profondo e della notte." +
                            "Le persone nate con questo ramo sono intuitive, astute, piene di risorse e possiedono una saggezza nascosta.",
                            details = "Elemento: Acqua | Radice: Gui (Acqua Yin)",
                            emoji = "🐭",
                        ),
                        ExtendedLearnItem(
                            category = "I 12 Rami Terrestri (Zodiaco Cinese)",
                            title = "Chou (丑) - Il Bue (The Ox)",
                            subtitle = "Terra Yin - Lo Stoico",
                            description = "Il secondo segno dello zodiaco. Rappresenta l'energia della fine dell'inverno e " +
                            "l'ora che precede l'alba. Le persone nate con questo ramo sono resilienti, metodiche, costanti e custodi " +
                            "di una determinazione incrollabile.",
                            details = "Elemento: Terra | Radice: Ji (Terra Yin)",
                            emoji = "🐮"
                        ),
                        ExtendedLearnItem(
                            category = "I 12 Rami Terrestri (Zodiaco Cinese)",
                            title = "Yin (寅) - La Tigre (The Tiger)",
                            subtitle = "Legno Yang - Il Conquistatore",
                            description = "Il terzo segno dello zodiaco. Rappresenta l'energia dell'inizio della primavera e del risveglio " +
                            "mattutino. Le persone nate con questo ramo sono audaci, indipendenti, passionali e animate da un forte spirito di leadership.",
                            details = "Elemento: Legno | Radice: Jia (Legno Yang)",
                            emoji = "🐯"
                        ),
                        ExtendedLearnItem(
                            category = "I 12 Rami Terrestri (Zodiaco Cinese)",
                            title = "Mao (卯) - Il Coniglio (The Rabbit)",
                            subtitle = "Legno Yin - Il Diplomatico",
                            description = "Il quarto segno dello zodiaco. Rappresenta l'energia della primavera piena e del sorgere del sole. " +
                            "Le persone nate con questo ramo sono pacifiche, sensibili, diplomatiche e dotate di uno spiccato senso artistico ed estetico.",
                            details = "Elemento: Legno | Radice: Yi (Legno Yin)",
                            emoji = "🐰"
                        ),
                        ExtendedLearnItem(
                            category = "I 12 Rami Terrestri (Zodiaco Cinese)",
                            title = "Chen (辰) - Il Drago (The Dragon)",
                            subtitle = "Terra Yang - Il Magnate",
                            description = "Il quinto segno dello zodiaco. Rappresenta l'energia della fine della primavera e della prima mattinata. " +
                            "Le persone nate con questo ramo sono carismatiche, piene di vitalità, ambiziose e mostrano un forte magnetismo ideale.",
                            details = "Elemento: Terra | Radice: Wu (Terra Yang)",
                            emoji = "🐲"
                        ),
                        ExtendedLearnItem(
                            category = "I 12 Rami Terrestri (Zodiaco Cinese)",
                            title = "Si (巳) - Il Serpente (The Snake)",
                            subtitle = "Fuoco Yin - Il Filosofo",
                            description = "Il sesto segno dello zodiaco. Rappresenta l'energia dell'inizio dell'estate e della mattinata inoltrata. " +
                            "Le persone nate con questo ramo sono acute, riflessive, enigmatiche e dotate di una profonda intelligenza analitica.",
                            details = "Elemento: Fuoco | Radice: Ding (Fuoco Yin)",
                            emoji = "🐍"
                        ),
                        ExtendedLearnItem(
                            category = "I 12 Rami Terrestri (Zodiaco Cinese)",
                            title = "Wu (午) - Il Cavallo (The Horse)",
                            subtitle = "Fuoco Yang - Il Pioniere",
                            description = "Il settimo segno dello zodiaco. Rappresenta l'energia dell'estate profonda e del mezzogiorno radioso. " +
                            "Le persone nate con questo ramo sono solari, amano la libertà, sono espansive e costantemente proiettate verso l'azione.",
                            details = "Elemento: Fuoco | Radice: Bing (Fuoco Yang)",
                            emoji = "🐴"
                        ),
                        ExtendedLearnItem(
                            category = "I 12 Rami Terrestri (Zodiaco Cinese)",
                            title = "Wei (未) - La Capra (The Goat)",
                            subtitle = "Terra Yin - L'Artista",
                            description = "L' ottavo segno dello zodiaco. Rappresenta l'energia della fine dell'estate e del primo pomeriggio. " +
                            "Le persone nate con questo ramo sono empatiche, protettive, amano l'armonia e cercano stabilità nelle relazioni personali.",
                            details = "Elemento: Terra | Radice: Ji (Terra Yin)",
                            emoji = "🐐"
                        ),
                        ExtendedLearnItem(
                            category = "I 12 Rami Terrestri (Zodiaco Cinese)",
                            title = "Shen (申) - La Scimmia (The Monkey)",
                            subtitle = "Metallo Yang - L'Innovatore",
                            description = "Il nono segno dello zodiaco. Rappresenta l'energia dell'inizio dell'autunno e del tardo pomeriggio. " +
                            "Le persone nate con questo ramo sono poliedriche, inventive, ironiche e capaci di risolvere problemi complessi con rapidità.",
                            details = "Elemento: Metallo | Radice: Geng (Metallo Yang)",
                            emoji = "🐵"
                        ),
                        ExtendedLearnItem(
                            category = "I 12 Rami Terrestri (Zodiaco Cinese)",
                            title = "You (酉) - Il Gallo (The Rooster)",
                            subtitle = "Metallo Yin - Il Perfezionista",
                            description = "Il decimo segno dello zodiaco. Rappresenta l'energia dell'autunno pieno e del tramonto. " +
                            "Le persone nate con questo ramo sono precise, franche, organizzate e hanno un naturale talento per l'osservazione e il dettaglio.",
                            details = "Elemento: Metallo | Radice: Xin (Metallo Yin)",
                            emoji = "🐔"
                        ),
                        ExtendedLearnItem(
                            category = "I 12 Rami Terrestri (Zodiaco Cinese)",
                            title = "Xu (戌) - Il Cane (The Dog)",
                            subtitle = "Terra Yang - Il Custode",
                            description = "L'undicesimo segno dello zodiaco. Rappresenta l'energia della fine dell'autunno e del crepuscolo serale. " +
                            "Le persone nate con questo ramo sono leali, protettive, dotate di un forte senso etico e molto attente alla sicurezza dei propri cari.",
                            details = "Elemento: Terra | Radice: Wu (Terra Yang)",
                            emoji = "🐶"
                        ),
                        ExtendedLearnItem(
                            category = "I 12 Rami Terrestri (Zodiaco Cinese)",
                            title = "Hai (亥) - Il Maiale (The Pig)",
                            subtitle = "Acqua Yin - Il Filantropo",
                            description = "Il dodicesimo segno dello zodiaco. Rappresenta l'energia dell'inizio dell'inverno e della prima serata. " +
                            "Le persone nate con questo ramo sono generose, sincere, tolleranti e affrontano la vita con un ottimismo calmo e pacifico.",
                            details = "Elemento: Acqua | Radice: Gui (Acqua Yin)",
                            emoji = "🐷"
                        ),
                        //Concetti Avanzati

                        ExtendedLearnItem(
                            category = "Tronchi Celesti Nascosti (Hidden Stems)",
                            title = "Le Radici dei Rami Terrestri",
                            subtitle = "Il potenziale nascosto dentro di te",
                            description = "In occidente siamo abituati a vedere un solo segno zodiacale. Nel BaZi, ogni animale nasconde sotto terra (nelle sue radici) da 1 a 3 Tronchi Celesti. Rappresentano i tuoi talenti nascosti, i desideri segreti e ciò che si attiverà solo in determinati momenti della tua vita.",
                            emoji = "🕳️",
                        ),

                        ExtendedLearnItem(
                            category = "Fasi del Qi (Qi Phase) / 12 Life Stages",
                            title = "Le 12 Fasi di Forza del Day Master",
                            subtitle = "Dalla Nascita all'Imperatore, fino alla Tomba",
                            description = "Questo concetto misura quanto il tuo Day Master si sente forte nel Ramo Terrestre di un pilastro. Va dalla fase di Nascita (piena di entusiasmo), alla fase 'Imperatore' (massimo potere) fino alla fase 'Tomba' (energia di accumulo ed introspezione). Rivela se le tue azioni avranno un impatto immediato o avranno bisogno di tempo.",
                            emoji = "☯️",
                        ),

                        
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Na Yin: Il Suono degli Elementi",
                            subtitle = "I 60 Cicli d'oro",
                            description = "Mentre il BaZI classico analizza gli elementi singoli, il Na Yin combina il Tronco Celeste ed il Ramo Terrestre per creare un 'Elemento Melodico' unico (Es: 'Fuoco della Lampada', 'Legno del Melograno'). Definisce l'atmosfera psicologica profonda e l'aura spirituale della persona.",
                            emoji = "🎵",
                        ),

                        
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Na Yin: La Matematica dei 5 elementi",
                            subtitle = "Il calcolo dell'elemento",
                            description = "Come si calcola con esattezza il Na Yin di un pilastro ? Il calcolo si basa sull'antica numerologia cinese, in particolare sull'antico testo 'Suen Tzu Suan Ching' e sul ciclo sessagesimale cinese. Si assegnano dei valori numerici ad ogni Tronco e ad ogni Ramo.\n\nEs:\nLegno: 1\nFuoco: 2\nTerra: 3\nMetallo: 4\nAcqua: 5\n\nInvece i Rami sono valutati nel seguente modo:\n\nEs:\nTopo/Bue/Cavallo/Capra: 1\nTigre/Coniglio/Scimmia/Gallo: 2\nDrago/Serpente/Cane/Maiale: 3\n\nSi sommano i valori del Tronco e del Ramo, sottraendo 5 se la somma è maggiore o uguale a 5 fino ad ottenere uno dei 5 resti (0, 1, 2, 3, 4). Il numero finale indica l'elemento del Na Yin secondo questo ordine:\n\nNumeri NaYin:\n1 = Legno\n2 = Metallo\n3 = Acqua\n4 = Fuoco\n0 = Terra",
                            emoji = "🌀",
                        ),
                        
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Na Yin: Le Combinazioni Tronco/Ramo",
                            subtitle = "Il nome dei NaYin",
                            description = "Combinando un Tronco definito con un Ramo si possono ottenere in totale 30 combinazioni diverse.\n\n" +
                                        "Tronco\tRamo\tNa Yin (Nome)\n" +
                                        "Jia (Legno Yang)\tShen (Scimmia)\tAcqua della Primavera\n",
                            details = "Il calcolo si basa sul ciclo sessagesimale.",
                            emoji = "✨",
                        )
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
            options.forEach { option ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = option, 
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) 
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    modifier = Modifier.background(Color(0xFF1E1E1E))
                )
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
                    Text(
                        text = item.emoji,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(end = 12.dp)
                    )
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

                    val tabellaStops = ParagraphStyle(
                        tabStops = listOf(
                            TabStop(110.sp),
                            TabStop(210.sp)
                        )
                    )

                    val testoFormattato = buildAnnotatedString{
                        withStyle(style = tabellaStops){
                            append(item.description)
                        }
                    }

                    Text(
                        text = testoFormattato,
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

