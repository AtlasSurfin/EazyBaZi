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
import androidx.compose.ui.unit.sp
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
                                    BaZiDropdown("Giorno", days, selectedDay, { selectedDay = it }, Modifier.weight(0.8f))
                                    BaZiDropdown("Mese", months, selectedMonth, { selectedMonth = it }, Modifier.weight(1.6f))
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
                                                val monthNum = getMonthNum(selectedMonth)

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
                            category = "I 5 elementi del BaZi",
                            title = "Legno (Mù)",
                            subtitle = "L'Energia della Primavera e della Nascita",
                            description = "Rappresenta l'espansione, la crescita verticale, l'idealismo e l'istinto di esplorazione. " +
                            "Psicologicamente governa la gentilezza, la compassione e la spinta al progresso. Se in eccesso può portare a " +
                            "rabbia e frustrazione. Nel corpo è legato a Fegato e Cistifellea.",
                            details = "Genera: Fuoco | Controlla: Terra | È generato da: Acqua | È controllato da: Metallo",
                            emoji = "木"
                        ),
                        ExtendedLearnItem(
                            category = "I 5 elementi del BaZi",
                            title = "Fuoco (Huǒ)",
                            subtitle = "L'Energia dell'Estate e dell'Espansione",
                            description = "Rappresenta la luminosità, il dinamismo, la passione e la visibilità sociale. " +
                            "Psicologicamente governa l'entusiasmo, l'espressione delle emozioni, la gioia e l'intelletto brillante. " +
                            "Se in eccesso, può portare a instabilità emotiva, ansia o aggressività. Nel corpo è legato al Cuore e all'Intestino Tenue. ",
                            details = "Genera: Terra | Controlla: Metallo | È generato da: Legno | È controllato da: Acqua",
                            emoji = "火"
                        ),
                        ExtendedLearnItem(
                            category = "I 5 elementi del BaZi",
                            title = "Metallo (Jīn)",
                            subtitle = "L'Energia dell'Autunno e della Contrazione",
                            description = "Rappresenta l'introspezione, la purezza, la giustizia, il taglio netto e la definizione dei confini. "  +
                            "Psicologicamente governa la disciplina, la logica affilata, il senso del dovere e il valore personale. " +
                            "Se in eccesso può portare a freddezza, rigidità e tristezza. Nel corpo è legato a Polmoni ed Intestino Crasso.",
                            details = "Genera: Acqua | Controlla: Legno | È generato da: Terra | È controllato da: Fuoco",
                            emoji = "金"
                        ),
                        ExtendedLearnItem(
                            category = "I 5 elementi del BaZi",
                            title = "Acqua (Shuǐ)",
                            subtitle = "L'Energia dell'Inverno e della Concentrazione",
                            description = "Rappresenta la fluidità, l'oscurità rigenerante, il riposo, l'adattabilità e il potenziale nascosto. " +
                            "Psicologicamente governa l'intelligenza profonda, l'intuito, la saggezza e la filosofia di vita. " +
                            "Se in eccesso può portare a paura ed indecisione. Nel corpo è legata a Reni e Vescica.",
                            details = "Genera: Legno | Controlla: Fuoco | È generato da: Metallo | È controllato da: Terra",
                            emoji = "水"
                        ),
                        ExtendedLearnItem(
                            category = "I 5 elementi del BaZi",
                            title = "Terra (Tǔ)",
                            subtitle = "L'Energia del Centro e della Stabilità",
                            description = "Rappresenta il fulcro immobile, la nutrizione, la stabilità e la capacità di raccogliere e trasformare. " +
                            "Psicologicamente governa la fiducia, l'onestà, la concretezza e il senso di radicamento. Se in eccesso può portare a rimuginio " +
                            "costante, testardaggine e stagnazione. Nel corpo è legata a Milza e Stomaco.",
                            details = "Genera: Metallo | Controlla: Acqua | È generato da: Fuoco | È controllato da: Legno",
                            emoji = "土"
                        ),

                        //Cicli del Qi
                        ExtendedLearnItem(
                            category = "I Cicli del Qi (o Cicli dei Wu Xing)",
                            title = "Il Ciclo di Generazione (Ciclo Sheng)",
                            subtitle = "La Relazione Madre-Figlio",
                            description = "Rappresenta il flusso armonioso, spontaneo e cooperativo dell'energia, dove ogni elemento " +
                            "nutre, supporta e dà la vita a quello successivo. È un ciclo perenne di crescita e trasformazione positiva che permette " +
                            "al Qi di evolversi continuamente senza sosta.",
                            details = "Flusso: Legno ➔ Fuoco ➔ Terra ➔ Metallo ➔ Acqua ➔ Legno",
                            emoji = "➕"
                        ),
                        ExtendedLearnItem(
                            category = "I Cicli del Qi (o Cicli dei Wu Xing)",
                            title = "Il Ciclo di Controllo (Ciclo Ke)",
                            subtitle = "La Forza del Limite",
                            description = "Rappresenta la forza che frena, regola, limita o domina l'elemento opposto. Non è un ciclo negativo: " +
                            "nella filosofia del BaZi il controllo è fondamentale per mantenere l'equilibrio della carta ed evitare che un elemento " +
                            "cresca troppo diventando distruttivo.",
                            details = "Flusso: Legno ➔ Terra ➔ Acqua ➔ Fuoco ➔ Metallo ➔ Legno",
                            emoji = "➖"
                        ),


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
                            emoji = "🕯️"
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
                        //I Dieci Dei

                        ExtendedLearnItem(
                            category = "I Dieci Dei (Ten Gods)",
                            title = "Il Sistema delle Dieci Relazioni",
                            subtitle = "Le relazioni tra il DM e gli elementi restanti",
                            description = "Nel BaZi si utilizza il sistema dei Dieci Dei o delle Dieci Relazioni per analizzare le interazioni " +
                            "tra l'elemento del Day Master e tutti gli elementi rimasti degli altri Tronchi e/o Rami. Queste sono determinate dal " +
                            "Ciclo di Generazione e dal Ciclo di Distruzione, assieme alle polarità Yin/Yang.",
                            emoji = "🔟",
                        ),

                        ExtendedLearnItem(
                            category = "I Dieci Dei (Ten Gods)",
                            title = "Friend (Bi Jian)",
                            subtitle = "L'Amico / Il Pari",
                            description = "Rappresenta un elemento della carta che ha lo stesso elemento e la stessa polarità del Day Master. " + 
                            "Simboleggia i pari, i colleghi ed il supporto reciproco. Nella personalità indica fiducia in sè stessi, indipendenza, " +
                            "forte ego e capacità di collaborare alla pari senza farsi sottomettere.",
                            details = "Es: (DM)Wood Yang + Wood Yang = Friend",
                            emoji = "🤝",
                        ),

                        ExtendedLearnItem(
                            category = "I Dieci Dei (Ten Gods)",
                            title = "Rob Wealth (Jie Cai)",
                            subtitle = "Il Concorrente / Il Ladro di Ricchezze",
                            description = "Si attiva quando un elemento nella carta è uguale al Day Master ma ha polarità opposta. " +
                            "Rappresenta i rivali, i competitor o partner d'affari molto determinati. Nella personalità dona un carisma immenso, " +
                            "capacità di leadership, abilità sociali e una natura competitiva, ma può spingere a spendere troppo o a subire perdite " +
                            "finanziarie a causa degli altri.",
                            details = "Es: Wood Yang + Wood Yin = Rob Wealth",
                            emoji = "💰",
                        ),

                        ExtendedLearnItem(
                            category = "I Dieci Dei (Ten Gods)",
                            title = "Eating God (Shi Shen)",
                            subtitle = "Lo Spirito del Nutrimento",
                            description = "Rappresenta l'elemento generato dal Day Master con la stessa polarità. " +
                            "È la stella del benessere, dell'espressione artistica rilassata e del godersi la vita. " +
                            "Indica una mente profonda, amore per il cibo, la cultura e l'estetica, orientata a creare un puro piacere personale.",
                            details = "Es: Wood Yang + Fire Yang = Eating God",
                            emoji = "🎨",
                        ),

                        ExtendedLearnItem(
                            category = "I Dieci Dei (Ten Gods)",
                            title = "Hurting Officer (Shang Guan)",
                            subtitle = "L'Ufficiale Ribelle",
                            description = "Rappresenta l'elemento generato dal Day Master ma con polarità opposta." +
                            "È la stella della ribellione, del talento visibile e dell'anticonformismo. " +
                            "Dona una grande intelligenza critica, abilità oratorie, carisma da palcoscenico e il desiderio di sfidare l'autorità " +
                            "e lo status quo.",
                            details = "Es: Wood Yang + Fire Yin = Hurting Officer",
                            emoji = "📣",
                        ),

                        ExtendedLearnItem(
                            category = "I Dieci Dei (Ten Gods)",
                            title = "Direct Wealth (Zheng Cai)",
                            subtitle = "La Ricchezza Diretta",
                            description = "Rappresenta l'elemento controllato dal Day Master con polarità opposta. " +
                            "Simboleggia il denaro guadagnato con il duro lavoro, la stabilità finanziaria e i beni tangibili. " +
                            "Nella personalità indica realismo, forte senso di responsabilità, onestà, disciplina ed un approccio oculato " +
                            "nella gestione delle risorse.",
                            details = "Es: Wood Yang + Earth Yin = Direct Wealth",
                            emoji = "💼",
                        ),

                        ExtendedLearnItem(
                            category = "I Dieci Dei (Ten Gods)",
                            title = "Indirect Wealth (Pian Cai)",
                            subtitle = "La Ricchezza indiretta",
                            description = "Rappresenta l'elemento controllato dal Day Master con la stessa polarità. " +
                            "Simboleggia le entrate improvvise, la fortuna negli affari, gli investimenti e lo spirito imprenditoriale. " +
                            "Indica una mente audace, generosa, amante del rischio e capace di vedere opportunità di profitto dove gli altri " +
                            "non vedono nulla.",
                            details = "Es: Wood Yang + Earth Yang = Indirect Wealth",
                            emoji = "🎲",
                        ),

                        ExtendedLearnItem(
                            category = "I Dieci Dei (Ten Gods)",
                            title = "Direct Officer (Zheng Guan)",
                            subtitle = "L'Ufficiale Legittimo",
                            description = "Rappresenta l'elemento che controlla il Day Master con polarità opposta. " +
                            "È la stella del potere benevolo, dello status sociale, della legge e del dovere. " +
                            "Dona un forte senso della giustizia, rispetto per le tradizioni, autocontrollo, affidabilità e la naturale " +
                            "predisposizione a ricoprire ruoli istituzionali.",
                            details = "Es: Wood Yang + Metal Yin = Direct Officer",
                            emoji = "⚖️",
                        ),
                        ExtendedLearnItem(
                            category = "I Dieci Dei (Ten Gods)",
                            title = "7 Killings (Qi Sha)",
                            subtitle = "Il Potere Aggressivo",
                            description = "Rappresenta l'elemento che controlla il Day Master con stessa polarità. " +
                            "È la stella del coraggio, dell'azione tempestiva e della resilienza estrema di fronte alle crisi. " +
                            "Indica una personalità autoritaria, determinata e combattiva, capace di imporsi e prendere decisioni difficili " +
                            "sotto forte pressione.",
                            details = "Es : Wood Yang + Metal Yang = 7 Killings",
                            emoji = "🎯",
                        ),
                        ExtendedLearnItem(
                            category = "I Dieci Dei (Ten Gods)",
                            title = "Direct Resource (Zheng Yin)",
                            subtitle = "La Risorsa Diretta",
                            description = "Rappresenta l'elemento che nutre il Day Master con polarità opposta. " +
                            "Simboleggia la figura materna, la protezione, la salute e la conoscenza accademica tradizionale. " +
                            "Rende la persona riflessiva, compassionevole, incline allo studio, bisognosa di sicurezza e " +
                            "dotata di un forte intuito morale.",
                            details = "Es: Wood Yang + Water Yin = Direct Resource",
                            emoji = "📚",
                        ),
                        ExtendedLearnItem(
                            category = "I Dieci Dei (Ten Gods)",
                            title = "Indirect Resource (Pian Yin)",
                            subtitle = "La Risorsa Indiretta",
                            description = "Rappresenta l'elemento che nutre il Day Master con stessa polarità. " +
                            "È la stella dell'intuito puro, del pensiero non convenzionale e del sesto senso. " +
                            "Indica una mente analitica, solitaria, fortemente attratta dalla spiritualità, dalla psicologia, " +
                            "dall'astrologia e dai segreti nascosti del mondo.",
                            details = "Es: Wood Yang + Water Yang = Indirect Resource",
                            emoji = "🔮",
                        ),

                        ExtendedLearnItem(
                            category = "Tronchi Celesti Nascosti (Hidden Stems)",
                            title = "Le Radici dei Rami Terrestri",
                            subtitle = "Il potenziale nascosto dentro di te",
                            description = "In occidente siamo abituati a vedere un solo segno zodiacale. Nel BaZi, ogni animale nasconde sotto " + 
                            "terra (nelle sue radici) da 1 a 3 Tronchi Celesti. Rappresentano i tuoi talenti nascosti, i desideri segreti e ciò che " + 
                            "si attiverà solo in determinati momenti della tua vita.",
                            emoji = "🕳️",
                        ),

                        //Interazioni
                        ExtendedLearnItem(
                            category = "Interazioni nel BaZi",
                            title = "Introduzione alle Interazioni",
                            subtitle = "La dinamica energetica della Carta Natale",
                            description = "Nel BaZi, i Tronchi Celesti e i Rami Terrestri non sono elementi statici ma interagiscono costantemente " +
                            "tra loro creando combinazioni, conflitti, punizioni o distruzioni. Queste interazioni rappresentano gli eventi, i cambiamenti " +
                            "e le dinamiche psicologiche o relazionali della persona. Le interazioni tra i Rami Terrestri (i segni dello zodiaco cinese) " +
                            "muovono gli eventi nel mondo reale e sociale, mentre quelle tra Tronchi Celesti mostrano i cambiamenti nelle intenzioni consce e " +
                            "nell'ambiente esterno.",
                            details = "Categorie di Interazioni: Combinazioni (Armonia) | Scontri (Cambiamento) | Punizioni (Attrito) | Distruzioni (Ostacoli)",
                            emoji = "🔄",
                        ),

                        ExtendedLearnItem(
                            category = "Interazioni nel BaZi",
                            title = "Combinazioni dei Tronchi Celesti",
                            subtitle = "L'unione delle polarità e la trasformazione",
                            description = "Rappresentano l'attrazione e l'armonia tra i Tronchi Celesti di polarità opposta (Yin e Yang). Quando due Tronchi si combinano, " +
                            "la loro natura cambia: simboleggiano alleanze, relazioni importanti, fusioni e la capacità di trovare compromessi. " +
                            "Se le condizioni della Carta Natale lo permettono, questa unione può dare vita a un nuovo elemento energetico. Nel Pilastro indicato " +
                            "mostra dove la persona cerca integrazione, cooperazione o dove le sue intenzioni consce tendono a legarsi a fattori esterni.",
                            details = "Tipo: Armonia | Elementi coinvolti: Tronchi Celesti (Yin + Yang) | Effetto: unione, romanticismo, patti, generazione di un nuovo elemento.",
                            emoji = "🤝"
                        ),
                        
                        ExtendedLearnItem(
                            category = "Interazioni nel BaZi",
                            title = "Scontri tra Tronchi Celesti",
                            subtitle = "Attrito, sfide e cambiamenti visibili",
                            description = "Rappresentano i conflitti diretti tra i Tronchi Celesti, causati dall'incontro di elementi opposti con la stessa polarità (Yin/Yin o Yang/Yang). " +
                            "Essendo i Tronchi energie visibili, questi scontri si manifestano come sfide esterne, eventi improvvisi, cambi di prospettiva e tensioni ideologiche/lavorative. " +
                            "Non sono negativi: funzionano spesso come catalizzatori che costringono ad uscire dalla zona di comfort, stimolando l'azione e rompendo " +
                            "situazioni stagnanti.",
                            details = "Tipo: Conflitto | Elementi coinvolti: Tronchi Celesti (Stessa polarità) | Effetto: instabilità temporanea, stress, spinte al cambiamento.",
                            emoji = "⚡"
                        ),

                        ExtendedLearnItem(
                            category = "Interazioni nel BaZi",
                            title = "Combinazioni tra Rami Terrestri",
                            subtitle = "Sinergia profonda e stabilità nel mondo reale",
                            description = "Rappresentano l'unione armonica tra due specifici Rami Terrestri che si attraggono e collaborano. Poichè i rami governano la Terra e la materia, " +
                            "queste combinazioni indicano legami affettivi solidi, partnership professionali stabili e situazioni in cui le cose scorrono senza intoppi. " +
                            "Agiscono come un collante energetico che protegge i settori coinvolti dagli attacchi esterni (scontri), sebbene a volte possano rendere la persona troppo " +
                            "restia al cambiamento o conservatrice.",
                            details = "Tipo: Alleanza e coppie | Elementi coinvolti: Rami Terrestri | Effetto: Protezione, stabilità, forte intesa relazionale, resistenza al cambiamento.",
                            emoji = "👥"
                        ),

                        ExtendedLearnItem(
                            category = "Interazioni nel BaZi",
                            title = "Combinazioni Triangolari tra Rami Terrestri ",
                            subtitle = "Massima espansione di un elemento",
                            description = "Rappresentano l'unione di tre specifici Rami Terrestri che, unendo le forze, generano una potentissima ondata di un singolo elemento. " +
                            "Questa combinazione unisce l'animale che fa nascere l'elemento, quello che ne rappresenta il picco e quello che lo custodisce. Quando si attiva nella Carta " +
                            "Natale, porta una straordinaria spinta focalizzata verso un obiettivo comune, amplificando a dismisura i significati di quell'elemento nei settori coinvolti, " +
                            "sia in positivo che in termini di eccesso.",
                            details = "Tipo: Alleanza a tre | Elementi coinvolti: Rami Terrestri | Effetto: Creazione di un elemento dominante, grande focus, eventi di vasta portata.",
                            emoji = "🔺"
                        ),
                        ExtendedLearnItem(
                            category = "Interazioni nel BaZi",
                            title = "Scontri tra Rami Terrestri",
                            subtitle = "Terremoti evolutivi e cambiamenti concreti",
                            description = "Rappresentano lo scontro frontale tra due animali situati in posizioni opposte sulla bussola energetica. Poichè i Rami governano la realtà " +
                            "materiale, questi scontri si manifestano come eventi tangibili: rotture relazionali, cambi di lavoro improvvisi, traslochi o forti scossoni emotivi e fisici. " +
                            "Sebbene possano generare forte stress ed instabilità temporanea, la loro funzione cosmica è distruggere le strutture stagnanti per consentire la persona a evolversi " +
                            "ed a rinnovare la propria vita.",
                            details = "Tipo: Conflitto | Elementi coinvolti: Rami Terrestri | Effetto: Rotture, mobilità geografica/professionale, sblocchi energetici radicali.",
                            emoji = "💥"
                        ),
                        ExtendedLearnItem(
                            category = "Interazioni nel BaZi",
                            title = "Punizioni dei Rami Terrestri",
                            subtitle = "Attriti psicologici, autosabotaggi e nodi karmici",
                            description = "Rappresentano una delle interazioni più complesse e psicologiche del BaZi. A differenza degli scontri diretti, le Punizioni agiscono dall'interno, manifestandosi come " +
                            "dinamiche di autosabotaggio, sensi di colpa, ingratitudine subita/compiuta e conflitti relazionali logoranti. Spesso indicano aree in cui la persona tende a ripetere gli stessi errori " +
                            "o a subire lo stress di aspettative non soddisfatte. Nel Pilastro indicato rivelano tensioni interne nascoste che richiedono profonda consapevolezza per essere sciolte." ,
                            details = "Tipo: Attrito | Elementi coinvolti: Rami Terrestri | Effetto: Autosabotaggio, stress emotivo, senso di ingratitudine o ingiustizia.",
                            emoji = "⏳"
                        ),
                        ExtendedLearnItem(
                            category = "Interazioni nel BaZi",
                            title = "Danni dei Rami Terrestri",
                            subtitle = "Ferite emotive, malintesi e aspettative tradite",
                            description = "Rappresentano un'interazione indiretta che colpisce soprattutto la sfera emotiva e relazionale. Un Danno si attiva quando un Ramo sabota o si scontra con il partner ideale (la Combinazione) di un altro Ramo. " +
                            "Nella vita reale questo si traduce in situazioni di incomprensione, aspettative non corrisposte, piccoli tradimenti o la sensazione che qualcuno stia remando contro i nostri piani. " +
                            "Nel Pilastro indicato mostra un'area in cui la persona deve fare attenzione alla comunicazione e a non accumulare risentimenti silenziosi.",
                            details = "Tipo: Attrito | Elementi coinvolti: Rami Terrestri | Effetto: Malintesi, delusioni relazionali, stress psicologico, vulnerabilità emotiva.",
                            emoji = "💔"
                        ),
                        ExtendedLearnItem(
                            category = "Interazioni nel BaZi",
                            title = "Distruzioni dei Rami Terrestri",
                            subtitle = "Sfaldamento, cattive abitudini e logoramento",
                            description = "Rappresentano un'interazione di attrito sottile che mina la stabilità dall'interno. Più che grandi shock esterni, le Distruzioni simboleggiano " + 
                            "il deterioramento graduale dovuto a comportamenti ripetitivi, vizi, mancanza di cura o decisioni impulsive che distruggono il lavoro fatto. Nel Pilastro indicato " +
                            "mostra un settore della vita in cui la persona tende a perdere effiacia a causa di vecchi schemi mentali sabotanti, dove è necessario fare pulizia per evitare che le " +
                            "risorse vadano sprecate.",
                            details = "Tipo: Logoramento | Elementi coinvolti: Rami Terrestri | Effetto: Rotture di schemi, spreco di energia, necessità di eliminare il superfluo.",
                            emoji = "🗑️"
                        ),

                        //Fasi del Qi

                        ExtendedLearnItem(
                            category = "Fasi del Qi (Qi Phase) / 12 Life Stages",
                            title = "Le 12 Fasi di Forza del Day Master",
                            subtitle = "Dalla Nascita all'Imperatore, fino alla Tomba",
                            description = "Questo concetto misura quanto il tuo Day Master si sente forte nel Ramo Terrestre di un pilastro. " + 
                            "Va dalla fase di Nascita (piena di entusiasmo), alla fase 'Imperatore' (massimo potere) fino alla fase 'Tomba' "+ 
                            "(energia di accumulo ed introspezione). Rivela se le tue azioni avranno un impatto immediato o avranno bisogno di tempo.",
                            emoji = "☯️",
                        ),

                        ExtendedLearnItem(
                            category = "Le 12 Fasi del Qi (Shi Er Chang Sheng)",
                            title = "Nascita (Chang Sheng)",
                            subtitle = "L'energia emerge nel mondo",
                            description = "Rappresenta il momento in cui il Qi prende forma e viene alla luce nel ciclo vitale. " + 
                            "È l'archetipo dell'energia fresca, della curiosità innata e della crescita vigorosa. " +
                            "Simboleggia una fase fortunata e priva di preconcetti, dove la mente è aperta all'apprendimento " +
                            "e il potenziale si esprime con ottimismo ed entusiasmo contagioso. Nel Pilastro indicato indica un settore della vita caratterizzato " +
                            "da partenze fortunate, grande vitalità fisica/mentale e capacità di rigenerarsi dopo ogni crisi.",
                            details = "Forza del Qi: Molto Alta | Tratti chiave: ottimismo, capacità di rinnovamento, forte supporto da mentori.",
                            emoji = "🤱"
                        ),

                        ExtendedLearnItem(
                            category = "Le 12 Fasi del Qi (Shi Er Chang Sheng)",
                            title = "Bagno (Mu Yu)",
                            subtitle = "Vulnerabilità, crescita e purificazione",
                            description = "Rappresenta la fase del neonato che viene lavato e purificato. È l'archetipo della vulnerabilità, della scoperta " +
                            "di sè e dell'esposizione al mondo senza filtri. Simboleggia un momento di transizione delicato ma affascinante, legato al fascino interpersonale, " +
                            "al romanticismo ed alla curiosità emotiva. Nel Pilastro indicato mostra un settore della vita dinamico e sensibile, dove si impara " +
                            "attraverso la sperimentazione e gli errori, dove le relazioni e l'attrattiva giocano un ruolo centrale.",
                            details = "Forza del Qi: Medio-Bassa | Tratti chiave: sensibilità, romanticismo, esposizione pubblica, fascino naturale.",
                            emoji = "👶"
                        ),

                        ExtendedLearnItem(
                            category = "Le 12 Fasi del Qi (Shi Er Chang Sheng)",
                            title = "Adolescenza (Dai Guan)",
                            subtitle = "L'ingresso nella giovinezza e l'ambizione",
                            description = "Rappresenta la fase in cui il giovane indossa gli abiti della maturità, pronto a fare il suo " +
                            "ingresso nella società. È l'archetipo dell'indipendenza, dell'esplorazione del proprio potere e dell'ambizione vigorosa. " +
                            "Simboleggia un momento di grande determinazione e spinta verso il successo, dove la mente è focalizzata sugli obiettivi " +
                            "e sul riconoscimento sociale. Nel Pilastro indicato mostra un settore della vita caratterizzato da un forte spirito di iniziativa, " +
                            "competitività e il desiderio di emergere, pur con il rischio di cadere nell'impulsività o nell'ostinazione.",
                            details = "Forza del Qi: Alta | Tratti chiave: ambizione, indipendenza, determinazione, spinta all'azione.",
                            emoji = "🎓"
                        ),

                        ExtendedLearnItem(
                            category = "Le 12 Fasi del Qi (Shi Er Chang Sheng)",
                            title = "Maturità (Lin Guan)",
                            subtitle = "La stabilità, il lavoro e la responsabilità",
                            description = "Rappresenta la fase dell'adulto che consolida la propria posizione e inizia a raccogliere i frutti del proprio " +
                            "impegno. È l'archetipo della stabilità materiale, del senso del dovere e dell'indipendenza finanziaria. Simboleggia un " +
                            "momento di grande equilibrio ed efficienza, in cui la persona agisce con pragmatismo, maturità e rispetto per le regole. " +
                            "Nel Pilastro indicato mostra un settore della vita solido e costruttivo, caratterizzato da avanzamenti di carriera stabili, " +
                            "affidabilità e la capacità di gestire responsabilità importanti senza farsi travolgere.",
                            details = "Forza del Qi: Molto Alta | Tratti chiave: pragmatismo, senso del dovere, indipendenza, affidabilità.",
                            emoji = "💼"
                        ),

                        ExtendedLearnItem(
                            category = "Le 12 Fasi del Qi (Shi Er Chang Sheng)",
                            title = "Picco (Di Wang)",
                            subtitle = "Il massimo splendore e il potere assoluto",
                            description = "Rappresenta il momento di massimo vigore, autorità e splendore dell'intero ciclo vitale. È l'archetipo del Sovrano, caratterizzato " +
                            "da una forza immensa, leadership naturale e capacità di dominare le circostanze. Simboleggia il punto più alto del successo e dell'autoaffermazione, " +
                            "ma porta con sè anche l'inizio nascosto del declino, poichè chi si trova sulla vetta può solo scendere. Nel Pilastro indicato mostra un settore " +
                            "della vita di grande impatto, dove si gode di estrema autonomia e potere d'azione, con il rischio latente di eccessivo orgoglio o isolamento.",
                            details = "Forza del Qi: Massima | Tratti chiave: leadership, autorità, indipendenza totale, successo.",
                            emoji = "👑"
                        ),

                        ExtendedLearnItem(
                            category = "Le 12 Fasi del Qi (Shi Er Chang Sheng)",
                            title = "Declino (Shuai)",
                            subtitle = "La transizione verso la saggezza e la prudenza",
                            description = "Rappresenta il momento in cui l'energia inizia a ritirarsi dopo aver toccato l'apice. Non indica un crollo ma una " +
                            "perdita di forza fisica a favore di un grande guadagno in termini di maturità, esperienza e cautela. È l'archetipo del saggio anziano " +
                            "o del sovrano che abdica, preferendo la diplomazia e la strategia alla forza bruta. Nel Pilastro indicato mostra un settore della vita " +
                            "caratterizzato da stabilità conservativa, dove si preferisce mantenere ciò che si ha piuttosto che rischiare in nuove avventure.",
                            details = "Forza del Qi: Medio-Alta | Tratti chiave: saggezza, prudenza, conservazione, maturità emotiva.",
                            emoji = "👴"
                        ),

                        ExtendedLearnItem(
                            category = "Le 12 Fasi del Qi (Shi Er Chang Sheng)",
                            title = "Ritiro (Bing)",
                            subtitle = "L'introspezione, il ritiro e la sensibilità",
                            description = "Rappresenta la fase in cui il Qi si indebolisce significativamente e si ritira dal mondo esterno. " +
                            "È l'archetipo dell'introversione, dell'empatia profonda e della riflessione filosofica. Simboleggia un momento in cui " +
                            "le energie fisiche calano, spingendo la mente a focalizzarsi sulla salute interiore, la spiritualità e la comprensione psicologica " +
                            "degli altri. Nel Pilastro indicato mostra un settore della vita caratterizzato da grande sensibilità, intuito e prudenza, dove è necessario " +
                            "agire senza fretta e ascoltare i propri ritmi biologici ed emotivi.",
                            details = "Forza del Qi: Bassa | Tratti chiave: empatia, introspezione, forte intuito, bisogno di riposo.",
                            emoji = "🛌"
                        ),

                        ExtendedLearnItem(
                            category = "Le 12 Fasi del Qi (Shi Er Chang Sheng)",
                            title = "Morte (Si)",
                            subtitle = "L'immobilità, la logica e la massima focalizzazione",
                            description = "Rappresenta il momento in cui il Qi si ferma del tutto, raggiungendo un livello di quiete assoluta. " + 
                            "È l'archetipo del distacco emotivo, della razionalità pura e della concentrazione profonda. Simboleggia la fine dei conflitti e " +
                            "la capacità di analizzare le cose senza l'interferenza dell'ego o delle passioni. Nel Pilastro indicato mostra un settore della vita " +
                            "caratterizzato da grande acume intellettuale, precisione e dedizione a compiti che richiedono logica, ricerca o specializzazione, " +
                            "sebbene possa mancare di spontaneità.",
                            details = "Forza del Qi: Molto Bassa | Tratti chiave: logica, focalizzazione, razionalità, distacco emotivo.",
                            emoji = "🧘"
                        ),

                        ExtendedLearnItem(
                            category = "Le 12 Fasi del Qi (Shi Er Chang Sheng)",
                            title = "Tomba (Mu)",
                            subtitle = "L'accumulo, il risparmio e la protezione",
                            description = "Rappresenta la fase in cui il Qi viene sepolto e custodito all'interno della terra per essere protetto. " +
                            "È l'archetipo del magazzino, del caveau e dei segreti. Simboleggia un momento di forte introversione, focalizzato sulla " +
                            "sicurezza materiale, il risparmio e la conservazione delle risorse personali. Nel Pilastro indicato mostra un settore della vita " +
                            "caratterizzato da una gestione prudente o riservata, dove si tende ad accumulare ricchezza o conoscenza e mantenere una vita privata " +
                            "molto protetta, con la tendenza a trattenere le cose.",
                            details = "Forza del Qi: Bassa | Tratti chiave: accumulo, riservatezza, risparmio, protezione delle risorse.",
                            emoji = "📦"
                        ),

                        ExtendedLearnItem(
                            category = "Le 12 Fasi del Qi (Shi Er Chang Sheng)",
                            title = "Vuoto (Jue)",
                            subtitle = "Il vuoto assoluto e la rottura con il passato",
                            description = "Rappresenta il momento in cui il vecchio Qi si estingue completamente, dissolvendosi nel vuoto. È l'archetipo " +
                            "della tabula rasa, della fine dei legami e della liberazione totale dai condizionamenti precedenti. Simboleggia un punto di svolta " +
                            "drastico ma necessario, dove l'assenza di struttura lascia spazio all'inaspettato e alla pura potenzialità futura. " +
                            "Nel Pilastro indicato mostra un settore della vita instabile o mutevole, caratterizzato da radicali cambiamenti, chiusure definitive " +
                            "e la necessità di imparare a lasciare andare per ricominciare da zero.",
                            details = "Forza del Qi: Minima | Tratti chiave : distacco, fine dei cicli, potenziale invisibile, transizione radicale.",
                            emoji = "🕳️"
                        ),

                        ExtendedLearnItem(
                            category = "Le 12 Fasi del Qi (Shi Er Chang Sheng)",
                            title = "Concepimento (Tai)",
                            subtitle = "La scintilla iniziale, la speranza e l'idea",
                            description = "Rappresenta il momento in cui la nuova energia viene concepita nell'oscurità, come un embrione che inizia " +
                            "a formarsi. È l'archetipo dell'ispirazione iniziale, del sogno e della speranza invisibile. Simboleggia una fase di vulnerabilità, " +
                            "ma anche di grande creatività latente, dove i progetti e le idee vengono seminati e protetti dal mondo esterno. Nel Pilastro indicato " +
                            "mostra un settore della vita caratterizzato da un costante bisogno di protezione, sogni ad occhi aperti e l'inizio silenzioso di nuove " +
                            "e promettenti fasi esistenziali.",
                            details = "Forza del Qi: Molto Bassa | Tratti chiave: speranza, pianificazione, vulnerabilità, potenziale creativo.",
                            emoji = "🌱"
                        ),

                        ExtendedLearnItem(
                            category = "Le 12 Fasi del Qi (Shi Er Chang Sheng)",
                            title = "Nutrimento (Yang)",
                            subtitle = "La gestazione, l'attesa e la preparazione",
                            description = "Rappresenta la fase finale del ciclo, in cui l'energia cresce e si sviluppa al sicuro all'interno dell'utero. " +
                            "È l'archetipo della gestazione, del nutrimento e dell'attesa fiduciosa. Simboleggia un momento protetto, privo di minacce esterne, " +
                            "dove ci si prepara a raccogliere le forze prima di venire alla luce. Nel Pilastro indicato mostra un settore della vita caratterizzato " +
                            "da supporto costante, stabilità serena e una crescita graduale ma sicura, ideale per lo studio, la pianificazione o il consolidamento personale.",
                            details = "Forza del Qi: Media | Tratti chiave: protezione, preparazione, accumulo di forza, serenità.",
                            emoji = "🥚"
                        ),
                        
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Na Yin: Il Suono degli Elementi",
                            subtitle = "I 60 Cicli d'oro",
                            description = "Mentre il BaZI classico analizza gli elementi singoli, il Na Yin combina il Tronco Celeste ed il Ramo Terrestre" + 
                            "per creare un 'Elemento Melodico' unico (Es: 'Fuoco della Lampada', 'Legno del Melograno'). Definisce l'atmosfera psicologica profonda " + 
                            "e l'aura spirituale della persona.",
                            emoji = "🎵",
                        ),

                        
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Na Yin: La Matematica dei 5 Elementi",
                            subtitle = "Il calcolo dell'elemento",
                            description = "Come si calcola con esattezza il Na Yin di un pilastro ? Il calcolo si basa sull'antica numerologia cinese, in particolare " +
                            "sull'antico testo 'Suen Tzu Suan Ching' e sul ciclo sessagesimale cinese. Si assegnano dei valori numerici ad ogni Tronco e ad ogni Ramo.\n\n" + 
                            "Es:\nLegno: 1\nFuoco: 2\nTerra: 3\nMetallo: 4\nAcqua: 5\n\nInvece i Rami sono valutati nel seguente modo:\n\nEs:\nTopo/Bue/Cavallo/Capra: 1\n" + 
                            "Tigre/Coniglio/Scimmia/Gallo: 2\nDrago/Serpente/Cane/Maiale: 3\n\nSi sommano i valori del Tronco e del Ramo, sottraendo 5 se la somma è maggiore " +
                            "o uguale a 5 fino ad ottenere uno dei 5 resti (0, 1, 2, 3, 4). Il numero finale indica l'elemento del Na Yin secondo questo ordine:\n\n" + 
                            "Numeri NaYin:\n1 = Legno\n2 = Metallo\n3 = Acqua\n4 = Fuoco\n0 = Terra",
                            emoji = "🌀",
                        ),
                        //Lista dei 30 Na Yin
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Oro del Mare (Hai Zhong Jin)",
                            subtitle = "Il gioiello in fondo al mare",
                            description = "Generato dai Pilastri Jia Zi (Legno Yang su Topo) e Yi Chou (Legno Yin su Bue)." + 
                            "Rappresenta una natura profonda, flessibile e purificante come le profondità oceaniche. " +  
                            "Le persone con questo Na Yin possiedono un carattere resiliente, capace di adattarsi alle tempeste della vita e " + 
                            "di custodire una saggezza interiore, come un tesoro sommerso.",
                            details = "Jia + Zi / Yi + Chou = Oro del Mare",
                            emoji = "🦪",
                        ),

                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Acciaio della Spada (Jian Feng Jin)",
                            subtitle = "La lama affilata",
                            description = "Generato dai Pilastri Ren Shen (Acqua Yang su Scimmia) e Gui You (Acqua Yin su Gallo)." + 
                            "Rappresenta una natura affilata, determinata e purissima, temprata per superare ogni ostacolo. " +  
                            "Le persone con questo Na Yin possiedono un carattere risoluto e un forte senso di giustizia, capace di agire con " + 
                            "estrema lucidità nei momenti di crisi e di tagliare i rami secchi per favorire l'evoluzione.",
                            details = "Ren + Shen / Gui + You = Acciaio della Spada",
                            emoji = "⚔️",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Oro della Cera Bianca (Bai La Jin)",
                            subtitle = "Il metallo puro e malleabile",
                            description = "Generato dai Pilastri Geng Chen (Metallo Yang su Drago) e Xin Si (Metallo Yin su Serpente). " +
                            "Rappresenta una natura pura, malleabile e in costante divenire, come l'oro nobile ancora liquido nel suo stampo. " +
                            "Le persone con questo Na Yin possiedono un carattere idealista, sensibile e trasparente. Hanno talenti innati e una mente " +
                            "brillante che necessitano però di disciplina, focus e delle giuste esperienze per essere pienamente forgiati.",
                            details = "Geng + Chen / Xin + Si = Oro della Cera Bianca",
                            emoji = "🕯️",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Oro nella Sabbia (Sha Zhong Jin)",
                            subtitle = "Nascosto nella terra",
                            description = "Generato dai Pilastri Jia Wu (Legno Yang su Cavallo) e Yi Wei (Legno Yin su Capra). " +
                            "Rappresenta una natura introspettiva, preziosa e nascosta, come le pagliuzze d'oro celate tra i granelli " +
                            "di sabbia. Le persone con questo Na Yin possiedono un carattere riservato, umile e profondo. " +
                            "Custodiscono un immenso valore interiore e talenti rari che richiedono tempo, isolamento e le giuste opportunità " +
                            "per essere scoperti e valorizzati.",
                            details = "Jia + Wu / Yi + Wei = Oro nella Sabbia",
                            emoji = "🏆",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Oro del Gioiello (Jin Bo Jin)",
                            subtitle = "La lamina preziosa",
                            description = "Generato dai Pilastri Ren Yin (Acqua Yang su Tigre) e Gui Mao (Acqua Yin su Coniglio). " +
                            "Rappresenta una natura raffinata, splendente e ornamentale, come un prezioso gioiello nato per date luce, fascino " +
                            "e prestigio. Le persone con questo Na Yin possiedono un carattere carismatico, elegante e attento ai dettagli. " +
                            "Hanno un talento naturale nel valorizzare chi sta intorno a loro, nel portare armonia estetica e nello splendere nei contesti sociali.",
                            details = "Ren + Yin / Gui + Mao = Oro del Gioiello",
                            emoji = "💎",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Oro del Fermaglio Prezioso (Chai Chuan Jin)",
                            subtitle = "Il gioiello raffinato ed elegante",
                            description = "Generato dai Pilastri Geng Xu (Acqua Yang su Cane) e Xin Hai (Acqua Yin su Maiale). " +
                            "Rappresenta una natura aggrazziata, preziosa e finimente rifinta, come un fermaglio d'oro nato per esaltare " +
                            "la bellezza e lo status. Le persone con questo Na Yin possiedono un carattere magnetico, sensibile ed estetico. " +
                            "Si distinguono per l'eleganza, il profondo senso della dignità e la capacità innata di portare armonia, stile e valore " +
                            "nei loro legami e nel proprio ambiente.",
                            details = "Geng + Xu / Xin + Hai",
                            emoji = "📿",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Acqua del Torrente (Jian Xia Shui)",
                            subtitle = "L'acqua del ruscello di montagna",
                            description = "Generato dai Pilastri Bing Zi (Fuoco Yang su Topo) e Ding Chou (Fuoco Yin su Bue). " +
                            "Rappresenta una natura limpida, intuitiva e dinamica, come un torrente di montagna che scorre con determinazione tra le rocce. " + 
                            "Le persone con questo Na Yin possiedono un carattere calmo ma risoluto, guidato da una profonda onestà e chiarezza mentale. " +
                            "Hanno una spiccata capacità di fluire oltre gli ostacoli senza mai perdere la propria purezza e i propri valori originari.",
                            details = "Bing + Zi / Ding + Chou = Acqua del Torrente",
                            emoji = "💧",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Acqua di Sorgente (Quan Zhong Shui)",
                            subtitle = "L'acqua più pura",
                            description = "Generato dai Pilastri Jia Shen (Legno Yang su Scimmia) e Yi You (Legno Yin su Gallo). " +
                            "Rappresenta una natura generosa, limpida e inesauribile, come una fonte pura che sgorga per offrire nutrimento e volontà. " +
                            "Le persone con questo Na Yin possiedono un carattere altruista, aperto e ricco di risorse. Si distinguono per la spiccata intelligenza. " +
                            "la prontezza nel condividere le proprie idee e il talento naturale nel rigenerare gli ambienti in cui operano.",
                            details = "Jia + Shen / Yi + You = Spring Water",
                            emoji = "⛲",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Acqua del Lungo Fiume (Chang Liu Shui)",
                            subtitle = "Il fiume che scorre verso l'oceano",
                            description = "Generato dai Pilastri Ren Chen (Acqua Yang su Drago) e Gui Si (Acqua Yin su Serpente). " +
                            "Rappresenta una natura vasta, inarrestabile e lungimirante, come un grande fiume che scorre verso l'oceano senza mai fermarsi. " +
                            "Le persone con questo Na Yin possiedono un carattere tenace, ambizioso e dinamico. Guardano sempre al futuro con grande ampiezza di vedute, " +
                            "possiedono una pazienza strategica e sanno guidare progetti a lungo termine verso il successo.",
                            details = "Ren + Chen / Gui + Si = Acqua del Lungo Fiume",
                            emoji = "🚣",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Acqua del Fiume Celeste (Tian He Shui)",
                            subtitle = "La Via Lattea",
                            description = "Generato dai Pilastri Bing Wu (Fuoco Yang su Cavallo) e Ding Wei (Fuoco Yin su Capra). " +
                            "Rappresenta una natura spirituale, elevata e rigenerante, come la pioggia celeste che scende dall'alto " +
                            "per nutrire la Terra. Le persone con questo Na Yin possiedono un carattere idealista, nobile e profondamente empatico. " +
                            "Sono guidate da una grande visione umanitaria, amano elevare lo spirito altrui e portano sollievo e ispirazione ovunque si trovino. ",
                            details = "Bing + Wu / Ding + Wei",
                            emoji = "🌧️",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Acqua del Grande Torrente (Da Xi Shui)",
                            subtitle = "Il grande torrente di montagna",
                            description = "Generato dai Pilastri Jia Yin (Legno Yang su Tigre) e Yi Mao (Legno Yin su Coniglio). " +
                            "Rappresenta una natura impetuosa, travolgente e vitale, come un grande torrente di valle che scende con forza ed energia. " +
                            "Le persone con questo Na Yin possiedono un carattere appassionato, espressivo e carismatico. Hanno una leadership naturale, " +
                            "comunicano con grande impatto e sanno muovere e motivare le masse verso un obiettivo comune.",
                            details = "Jia + Yin / Yi + Mao = Acqua del Grande Torrente",
                            emoji = "🏞️",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Acqua del Grande Oceano (Da Hai Shui)",
                            subtitle = "L'immenso Oceano",
                            description = "Generato dai Pilastri Ren Xu (Acqua Yang su Cane) e Gui Hai (Acqua Yin su Maiale). " +
                            "Rappresenta una natura immensa, profonda e omnicomprensiva, come un grande oceano che accoglie e custodisce ogni cosa. " +
                            "Le persone con questo Na Yin possiedono un carattere magnetico, saggio e tollerante. Hanno una visione globale della vita, " +
                            "una grandissima resilienza emotiva e una forza interiore capace di dominare le tempeste più grandi.",
                            details = "Ren + Xu / Gui + Hai = Acqua del Grande Oceano",
                            emoji = "🌊",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Legno della Grande Foresta (Da Lin Mu)",
                            subtitle = "Il cuore della foresta millenaria",
                            description = "Generato dai Pilastri Wu Chen (Terra Yang su Drago) e Ji Si (Terra Yin su Serpente). " +
                            "Rappresenta una natura solida, protettiva e rigogliosa, come una grande foresta che offre rifugio, stabilità e vita. " +
                            "Le persone con questo Na Yin possiedono un carattere generoso, affidabile e orientato alla comunità. Si distinguono per " +
                            "lo spirito di cooperazione, la resilienza di fronte alle avversità e il talento naturale nel sostenere e far crescere gli altri.",
                            details = "Wu + Chen / Ji + Si = Legno della Grande Foresta",
                            emoji = "🌳",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Legno del Salice Piangente (Yang Liu Mu)",
                            subtitle = "Si piega ma non si spezza",
                            description = "Generato dai Pilastri Ren Wu (Acqua Yang su Cavallo) e Gui Wei (Acqua Yin su Capra). " +
                            "Rappresenta una natura flessibile, elegante e profondamente empatica, come un salice piangente che asseconda il vento " +
                            "senza mai spezzarsi. Le persone con questo Na Yin possiedono un carattere dolce, diplomatico e intuitivo. Si distinguono per " +
                            "l'eccezionale capacità di adattamento, la sensibilità emotiva e il talento nel riportare armonia e comprensione nei conflitti.",
                            details = "Ren + Wu / Gui + Wei = Legno del Salice Piangente",
                            emoji = "🍃",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Legno di Pino e Cipresso (Song Bai Mu)",
                            subtitle = "Sempreverdi anche nel cuore dell'inverno",
                            description = "Generato dai Pilastri Geng Yin (Metallo Yang su Tigre) e Xin Mao (Metallo Yin su Coniglio). " +
                            "Rappresenta una natura fiera, integra e profondamente resiliente, come il pino ed il cipresso che sfidano il gelo invernale. " +
                            "Le persone con questo Na Yin possiedono un carattere forte, leale e di sani principi. Si distinguono per l'incrollabile tenacia, " +
                            "la dignità personale e la capacità di rimanere un punto di riferimento saldo anche nei momenti più difficili.",
                            details = "Geng + Yin / Xin + Mao = Legno di Pino e Cipresso",
                            emoji = "🌿",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Legno della Pianura (Ping Di Mu)",
                            subtitle = "Tranquillo, rigoglioso e sicuro",
                            description = "Generato dai Pilastri Wu Xu (Terra Yang su Cane) e Ji Hai (Terra Yin su Maiale). " +
                            "Rappresenta una natura giovane, fertile e ricca di potenziale, come la vegetazione rigogliosa della pianura. " +
                            "Le persone con questo Na Yin possiedono un carattere genuino, gentile e laborioso. Si distinguono per l'onestà d'animo, la capacità " +
                            "di apprendere rapidamente e un talento naturale nel seminare armonia e nuove idee.",
                            details = "Wu + Xu / Ji + Hai = Legno della Pianura",
                            emoji = "🌾",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Legno di Gelso (Sang Zhe Mu)",
                            subtitle = "Il Gelso trasforma le foglie in seta",
                            description = "Generato dai Pilastri Ren Zi (Acqua Yang su Topo) e Gui Chou (Acqua Yin su Bue). " +
                            "Rappresenta una natura generosa, operosa e ricca di valore, come un albero di gelso che nutre e dà vita a frutti preziosi. " +
                            "Le persone con questo Na Yin possiedono un carattere dedito, premuroso e concreto. Si distinguono per lo spirito di sacrificio, " +
                            "la grande laboriosità e il talento innato nel trasformare risorse semplici in qualcosa di nobile.",
                            details = "Ren + Zi / Gui + Chou = Legno di Gelso",
                            emoji = "🐛",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Legno di Melograno (Shi Liu Mu)",
                            subtitle = "La fioritura vibrante e l'abbondanza interiore",
                            description = "Generato dai Pilastri Geng Shen (Metallo Yang su Scimmia) e Xin You (Metallo Yin su Gallo). " +
                            "Rappresenta una natura tenace, vibrante e generosa, come un albero di melograno capace di fiorire e donare frutti preziosi " +
                            "tra le rocce. Le persone con questo Na Yin possiedono un carattere determinato, creativo e appassionato. Si distinguono per " +
                            "l'eccezionale forza di volontà, l'originalità delle proprie idee e la capacità di prosperare anche nelle avversità.",
                            details = "Geng + Shen / Xin + You = Legno di Melograno",
                            emoji = "🍎",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Fuoco della Fornace (Lu Zhong Huo)",
                            subtitle = "La passione intesa ed il calore trasformativo",
                            description = "Generato dai Pilastri Bing Yin (Fuoco Yang su Tigre) e Ding Mao (Fuoco Yin su Coniglio). " +
                            "Rappresenta una natura ardente, concentrata e trasformatrice, come il fuoco di una fornace che tempra e forgia con costanza. " +
                            "Le persone con questo Na Yin possiedono un carattere appasionato, resiliente e di grande intensità. Si distinguono per l'immensa " +
                            "forza di volontà, l'entusiasmo contagioso e il talento naturale nel guidare e finalizzare progetti complessi.",
                            details = "Bing + Yin / Ding + Mao = Fuoco della Fornace",
                            emoji = "🔥",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Fuoco sulla Cima della Montagna (Shan Tou Huo)",
                            subtitle = "L'incendio visibile che illumina il cielo",
                            description = "Generato dai Pilastri Jia Xu (Legno Yang su Cane) e Yi Hai (Legno Yin su Maiale). " +
                            "Rappresenta una natura radiosa, carismatica e dirompente, come un fuoco che brilla sulla vetta illuminando l'orizzonte. " +
                            "Le persone con questo Na Yin possiedono un carattere schietto, brillante e appassionato. Si distinguono per la rapidità d'azione, " +
                            "la leadership naturale e il talento innato nell'ispirare gli altri e farsi notare.",
                            details = "Jia + Xu / Yi + Hai = Fuoco sulla Cima della Montagna",
                            emoji = "🌋",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Fuoco ai Piedi della Montagna (Shan Xia Huo)",
                            subtitle = "La luce discreta che riscalda e protegge",
                            description = "Generato dai Pilastri Bing Shen (Fuoco Yang su Scimmia) e Ding You (Fuoco Yin su Gallo). " +
                            "Rappresenta una natura intima, strategica e pragmatica, come un fuoco di valle che riscalda la terra e cova con pazienza. " +
                            "Le persone con questo Na Yin possiedono un carattere discreto, acuto e riflessivo. Sanno agire al momento opportuno, " +
                            "coltivano relazioni profonde e autentiche e preferiscono i risultati concreti alla visibilità fine a se stessa.",
                            details = "Bing + Shen  / Ding + You = Fuoco ai Piedi della Montagna",
                            emoji = "⛺",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Fuoco del Fulmine (Pili Huo)",
                            subtitle = "La saetta improvvisa e l'energia elettrica",
                            description = "Generato dai Pilastri Wu Zi (Terra Yang su Topo) e Ji Chou (Terra Yin su Bue). " +
                            "Rappresenta una natura dirompente, istantanea e imprevedibile, come un fulmine che squarcia il cielo e trasforma la realtà. " +
                            "Le persone con questo Na Yin possiedono un carattere magnetico, impulsivo e geniale. Si distinguono per la straordinaria prontezza " +
                            "mentale, il coraggio nelle emergenze e la capacità di rompere gli schemi con idee rivoluzionarie.",
                            details = "Wu + Zi / Ji + Chou = Fuoco del Fulmine",
                            emoji = "⚡",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Fuoco della Lampada (Fu Deng Huo)",
                            subtitle = "La luce della saggezza che guida nell'oscurità",
                            description = "Generato dai Pilastri Jia Chen (Terra Yang su Drago) e Yi Si (Terra Yin su Serpente). " +
                            "Rappresenta una natura saggia, focalizzata e intellettuale, come una lampada che illumina l'oscurità e guida i passi nel buio. " +
                            "Le persone con questo Na Yin possiedono un carattere profondo, intuitivo e riflessivo. Si distinguono per la sete di conoscenza, " +
                            "l'eleganza di pensiero e il talento innato nel portare chiarezza, consiglio e ispirazione nei momenti di crisi.",
                            details = "Jia + Chen / Yi + Si = Fuoco della Lampada",
                            emoji = "🏮",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Fuoco del Sole (Tian Shang Huo)",
                            subtitle = "La luce celestiale che illumina e dà la vita",
                            description = "Generato dai Pilastri Wu Wu (Terra Yang su Cavallo) e Ji Wei (Terra Yin su Capra). " +
                            "Rappresenta una natura sovrana, generosa e illuminante, come il sole nel cielo che riscalda e dà vita a tutto il mondo. " +
                            "Le persone con questo Na Yin possiedono un carattere nobile, magnanimo e carismatico. Si distinguono per l'onestà incrollabile, " +
                            "il profondo senso di giustizia e il talento naturale nel guidare, proteggere e ispirare le masse",
                            details = "Wu + Wu / Ji + Wei = Fuoco del Sole",
                            emoji = "🌅",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Terra del Ciglio della Strada (Lu Bian Tu)",
                            subtitle = "La stabilità umile che sostiene il cammino",
                            description = "Generato dai Pilastri Geng Wu (Metallo Yang su Cavallo) e Xin Wei (Metallo Yin su Capra). " +
                            "Rappresenta una natura solida, accogliente e orientata al servizio, come la terra che costeggia e sostiene le grandi strade. " +
                            "Le persone con questo Na Yin possiedono un carattere affidabile, paziente e pragmatico. Si distinguono per lo spirito di sacrificio, " +
                            "la capacità di unire le persone e il talento naturale nel creare basi sicure per gli altri.",
                            details = "Geng + Wu / Xin + Wei = Terra del Ciglio della Strada",
                            emoji = "🥾",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Terra delle Mura della Città (Cheng Tou Tu)",
                            subtitle = "La fortezza interiore e lo scudo protettivo",
                            description = "Generato dai Pilastri Wu Yin (Terra Yang su Tigre) e Ji Mao (Terra Yin su Coniglio). " +
                            "Rappresenta una natura imponente, protettiva e incrollabile, come le mura di una città che difendono la civiltà e " +
                            "resistono al tempo. Le persone con questo Na Yin possiedono un carattere fiero, stabile e di sani principi. Si distinguono per " +
                            "il forte senso del dovere, l'istinto protettivo verso i propri cari e l'incrollabile capacità di mantenere la posizione nelle avversità.",
                            details = "Wu + Yin / Ji + Mao = Terra delle Mura della Città",
                            emoji = "🏯",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Terra del Tetto (Wu Shang Tu)",
                            subtitle = "La protezione elevata che ripara dalle tempeste",
                            description = "Generato dai Pilastri Bing Xu (Fuoco Yang su Cane) e Ding Hai (Fuoco Yin su Maiale). " +
                            "Rappresenta una natura elevata, protettiva e resiliente, come le tegole di un tetto che offrono ripario dalle intemperie. " +
                            "Le persone con questo Na Yin possiedono un carattere indipendente, fiero e coscienzioso. Si distinguono per il forte senso di " +
                            "responsabilità verso la famiglia, la dignità personale e la capacità di sopportare grandi pesi per proteggere gli altri.",
                            details = "Bing + Xu / Ding + Hai = Terra del Tetto",
                            emoji = "🏚️",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Terra della Parete (Bi Shang Tu)",
                            subtitle = "Il calore domestico e la protezione dell'intimità",
                            description = "Generato dai Pilastri Geng Zi (Metallo Yang su Topo) e Xin Chou (Metallo Yin su Bue). " +
                            "Rappresenta una natura intima, coesiva e orientata all'armonia, come l'intonaco di una parete che unisce i materiali e abbellisce la casa. " +
                            "Le persone con questo Na Yin possiedono un carattere sensibile, leale e costruttivo. Si distinguono per la capacità " +
                            "di mediazione, il talento nel creare ambienti sereni e l'istinto profondo nel proteggere l'armonia familiare.",
                            details = "Geng + Zi / Xin + Chou = Terra della Parete",
                            emoji = "🧱",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Terra della Grande Autostrada (Da Yi Tu)",
                            subtitle = "La vasta via di connessione e lo spirito d'espansione",
                            description = "Generato dai Pilastri Wu Shen (Terra Yang su Scimmia) e Ji You (Terra Yin su Gallo). " +
                            "Rappresenta una natura vasta, dinamica e cosmopolita, come una grande via di comunicazione che unisce mondi e culture diverse. " +
                            "Le persone con questo Na Yin possiedono un carattere aperto, tollerante e lungimirante. Si distinguono per la grande adattabilità, " +
                            "la passione per il movimento e il talento innato nel gestire ampi reti di relazioni.",
                            details = "Wu + Shen / Ji + You = Terra della Grande Autostrada",
                            emoji = "🛣️",
                        ),
                        ExtendedLearnItem(
                            category = "Na Yin (Astrologia Melodica)",
                            title = "Terra nella Sabbia (Sha Zhong Tu)",
                            subtitle = "La fluidità dello spirito e l'intuito profondo",
                            description = "Generato dai Pilastri Bing Chen (Fuoco Yang su Drago) e Ding Si (Fuoco Yin su Serpente). " +
                            "Rappresenta una natura fluida, misteriosa e preziosa, come la sabbia che si adatta agli elementi e custodisce oro " +
                            "nei suoi granelli. Le persone con questo Na Yin possiedono un carattere flessibile, intuitivo e profondo. Si distinguono per " +
                            "la grande resilienza, l'eleganza nei modi e il talento innato nel far emergere il proprio valore unico con il tempo.",
                            details = "Bing + Chen / Ding + Si = Terra nella Sabbia",
                            emoji = "⏳",
                        ),

                        //Stelle simboliche

                        ExtendedLearnItem(
                            category = "Stelle Simboliche (Shen Sha)",
                            title = "Introduzione alle Stelle",
                            subtitle = "I modificatori speciali del destino ",
                            description = "Le Stelle Simboliche (Shen Sha) sono configurazioni energetiche speciali calcolate in base alle " +
                            "relazioni tra il Day Master (o il Ramo dell'anno) e gli altri pilastri. Non rappresentano elementi fisici ma " +
                            "archetipi psicologici, talenti innati, fortune protettive o sfide karmiche specifiche. Agiscono come 'lenti d'ingradimento' " +
                            "che colorano i settori della vita in cui risiedono, aggiungendo sfumature uniche alla personalità e al percorso evolutivo " +
                            "dell'individuo. ",
                            emoji = "✨"
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

