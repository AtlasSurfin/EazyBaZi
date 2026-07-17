package com.fredcomms.baziapp.logic
import com.fredcomms.baziapp.R
import com.nlf.calendar.Solar
import com.nlf.calendar.Lunar
import android.os.Build
import androidx.compose.ui.graphics.Color
import android.location.Geocoder
import android.location.Address
import android.content.Context
import java.util.Locale
import java.util.TimeZone
import java.time.Month
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoField
import java.time.format.TextStyle
import java.time.format.DateTimeFormatter
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class Element {FIRE, EARTH, METAL, WATER, WOOD}

enum class Polarity {YANG, YIN}

interface BaZiComponent {
    val element: Element
    val polarity: Polarity
}

enum class Stem(
    val chinese: String,
    override val element: Element,
    override val polarity: Polarity
) : BaZiComponent {
    BING("丙", Element.FIRE, Polarity.YANG), 
    DING("丁", Element.FIRE, Polarity.YIN), 
    WU("戊", Element.EARTH, Polarity.YANG), 
    JI("己", Element.EARTH, Polarity.YIN), 
    GENG("庚", Element.METAL, Polarity.YANG), 
    XIN("辛", Element.METAL, Polarity.YIN), 
    REN("壬", Element.WATER, Polarity.YANG), 
    GUI("癸", Element.WATER, Polarity.YIN), 
    JIA("甲", Element.WOOD, Polarity.YANG), 
    YI("乙", Element.WOOD, Polarity.YIN);
    
    companion object {
        fun fromChinese(char: String): Stem? =
            entries.find { it.chinese == char }
    }
}

enum class Branch(
    val chinese: String,
    val pinyin: String,
    override val element: Element,
    override val polarity: Polarity,
    val branchName: String,
    val timeRange: String
) : BaZiComponent {
    ZI("子", "Zi", Element.WATER, Polarity.YANG, "Rat", "23:00 - 01:00"),
    CHOU("丑", "Chou", Element.EARTH, Polarity.YIN, "Ox", "01:00 - 03:00"),
    YIN("寅", "Yin", Element.WOOD, Polarity.YANG, "Tiger", "03:00 - 05:00"),
    MAO("卯", "Mao", Element.WOOD, Polarity.YIN, "Rabbit", "05:00 - 07:00"),
    CHEN("辰", "Chen", Element.EARTH, Polarity.YANG, "Dragon", "07:00 - 09:00"),
    SI("巳", "Si", Element.FIRE, Polarity.YIN, "Snake", "09:00 - 11:00"),
    WU("午", "Wu", Element.FIRE, Polarity.YANG, "Horse", "11:00 - 13:00"),
    WEI("未", "Wei", Element.EARTH, Polarity.YIN, "Goat", "13:00 - 15:00"),
    SHEN("申", "Shen", Element.METAL, Polarity.YANG, "Monkey", "15:00 - 17:00"),
    YOU("酉", "You", Element.METAL, Polarity.YIN, "Rooster", "17:00 - 19:00"),
    XU("戌", "Xu", Element.EARTH, Polarity.YANG, "Dog", "19:00 - 21:00"),
    HAI("亥", "Hai", Element.WATER, Polarity.YIN, "Pig", "21:00 - 23:00");

    companion object {
        fun fromChinese(char: String): Branch? =
            entries.find { it.chinese == char }
    }
}

data class Pillar(
    val stem: Stem?,
    val branch: Branch?
)

data class FullBaZiChart(
    val year: Pillar,
    val month: Pillar,
    val day: Pillar,
    val hour: Pillar
)

data class CityData(
    val n: String,
    val ln: Double
)

enum class BaZiRole{
    FRIEND,
    ROB_WEALTH,
    OUTPUT,
    WEALTH,
    OFFICER,
    RESOURCE
}

data class RoleScores(
    val friend: Int = 0,
    val robWealth: Int = 0,
    val output: Int = 0,
    val wealth: Int = 0,
    val officer: Int = 0,
    val resource: Int = 0
){
    val companionTot: Int get() = friend + robWealth
    val total: Int get() = companionTot + output + wealth + officer + resource
}

object CityLoader{
    fun loadCitiesByCountry(context: Context): Map<String, List<CityData>> {
        return try {
            val jsonString = context.assets.open("cities.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<Map<String, List<CityData>>>() {}.type
            Gson().fromJson(jsonString, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

fun getPillar(stem: Stem?, branch: Branch?): Pillar {
    return Pillar(stem, branch)}

fun getTenGods(dayMaster: Stem, target: Stem): String{
    val isSamePolarity = dayMaster.polarity == target.polarity

    return when{
        //Output
        isProducing(dayMaster.element, target.element) ->
            if(isSamePolarity) "Eating God (Pian Shi)" else "Hurting Officer (Shang Guan)"
        //Wealth
        isControlling(dayMaster.element, target.element) ->
            if(isSamePolarity) "Direct Wealth (Zheng Cai)" else "Indirect Wealth (Pian Cai)"

        //Power
        isControlling(target.element, dayMaster.element) ->
            if(isSamePolarity) "7 Killings (Qi Sha)" else "Direct Officer (Zheng Guan)"

        //Resource
        isProducing(target.element, dayMaster.element) ->
            if(isSamePolarity) "Indirect Resource (Pian Yin)" else "Direct Resource (Zheng Yin)"

        //Friends
        dayMaster.element == target.element ->
            if(isSamePolarity) "Friend (Bi Jian)" else "Rob Wealth (Jie Cai)"

        else -> "Unknown"
    }
}

fun isProducing(a: Element, b: Element) = (a == Element.WOOD && b == Element.FIRE)
|| (a == Element.FIRE && b == Element.EARTH) 
|| (a == Element.EARTH && b ==Element.METAL) 
|| (a == Element.METAL && b == Element.WATER) 
|| (a == Element.WATER && b == Element.WOOD)

fun isControlling(a: Element, b: Element) = (a == Element.WOOD && b == Element.EARTH) 
|| (a == Element.EARTH && b == Element.WATER) 
|| (a == Element.WATER && b == Element.FIRE) 
|| (a == Element.FIRE && b == Element.METAL) 
|| (a == Element.METAL && b == Element.WOOD)

fun findStem(name: String): Stem? {
    return Stem.valueOf(name.trim().uppercase())
}


fun getFullBaZi(year: Int, month: Int, day: Int, hour: Int, minute: Int, longitude: Double): FullBaZiChart {
    return try {

        val inputDateTime = LocalDateTime.of(year, month, day, hour, minute)

        val tz = TimeZone.getDefault()
        val rawOffsetMinutes = tz.getOffset(System.currentTimeMillis()) / 1000 / 60
        val timezoneOffsetHours = rawOffsetMinutes / 60.0

        val longitudeCorrectionMinutes = (longitude * 4) - (timezoneOffsetHours * 60)

        val solarDateTime = if (longitudeCorrectionMinutes >= 0){
            inputDateTime.plusMinutes(longitudeCorrectionMinutes.toLong())
        }else{
            inputDateTime.minusMinutes(Math.abs(longitudeCorrectionMinutes).toLong())
        }

        val solar = Solar.fromYmdHms(
            solarDateTime.year,
            solarDateTime.monthValue,
            solarDateTime.dayOfMonth,
            solarDateTime.hour,
            solarDateTime.minute,
            0
        )

        val baZi = solar.lunar.baZi

        fun extractPillar(baziPair: String): Pillar {
            val s = Stem.fromChinese(baziPair.substring(0,1))
            val b = Branch.fromChinese(baziPair.substring(1,2))
            return Pillar(s, b)
        }

        FullBaZiChart(
            year = extractPillar(baZi[0]),
            month = extractPillar(baZi[1]),
            day = extractPillar(baZi[2]),
            hour = extractPillar(baZi[3])
        )
    } catch (e: Exception) {
        val emptyPillar = Pillar(null, null)
        FullBaZiChart(emptyPillar, emptyPillar, emptyPillar, emptyPillar)
    }
}

fun getTenGodName(dayMaster: Element, target: Element, dayMasterPolarity: Polarity, targetPolarity: Polarity): String{
    return when{
        dayMaster == target && dayMasterPolarity == targetPolarity -> "Friends"
        dayMaster == target && dayMasterPolarity != targetPolarity -> "Rob Wealth"
        isProducing(dayMaster, target) && dayMasterPolarity == targetPolarity -> "Eating God"
        isProducing(dayMaster, target) && dayMasterPolarity != targetPolarity -> "Hurting Officer"
        isProducing(target, dayMaster) && dayMasterPolarity == targetPolarity -> "Indirect Resource"
        isProducing(target, dayMaster) && dayMasterPolarity != targetPolarity -> "Direct Resource"
        isControlling(dayMaster, target) && dayMasterPolarity == targetPolarity -> "Indirect Wealth"
        isControlling(dayMaster, target) && dayMasterPolarity != targetPolarity -> "Direct Wealth"
        isControlling(target, dayMaster) && dayMasterPolarity == targetPolarity -> "7 Killings"
        isControlling(target, dayMaster) && dayMasterPolarity != targetPolarity -> "Direct Officer"
        else -> ""
    }
}

fun getElementColor(element: Element): Long {
    return when (element) {
        Element.FIRE -> 0xFFEF5350
        Element.EARTH -> 0xFFFFCA28
        Element.WOOD -> 0xFF66BB6A
        Element.WATER -> 0xFF42A5F5
        Element.METAL -> 0xFFBDBDBD
    }
}

fun getCoordinatesFromName(context: Context, cityName: String, onResult: (CityData?) -> Unit) {
    try{
        val geocoder = Geocoder(context, Locale.getDefault())
        val processAddress:  (Address?) -> Unit = { address ->
            if(address != null) {
                val localita = address.locality ?: address.subAdminArea ?: cityName
                val nazione = address.countryName ?: ""
                val regioneStato = address.adminArea
                val provincia = address.subAdminArea

                val fullName = when {
                    nazione.equals("Italia", ignoreCase = true) -> {
                        val siglaProvincia = when {
                            provincia == null -> ""
                            provincia.contains("Roma", ignoreCase = true) -> "RM"
                            provincia.contains("Torino", ignoreCase = true) -> "TO"
                            provincia.contains("Milano", ignoreCase = true) -> "MI"
                            provincia.contains("Napoli", ignoreCase = true) -> "NA"
                            else -> {
                                val pulita = provincia.replace("Provincia di ", "", ignoreCase = true)
                                                     .replace("Città Metropolitana di ", "", ignoreCase = true)
                                if (pulita.length <= 3) pulita.uppercase() else pulita
                            }
                        }
                        if(siglaProvincia.isNotEmpty()) "$localita ($siglaProvincia), $nazione" else "$localita, $nazione"
                    }


                    nazione.equals("United States", ignoreCase = true) || nazione.equals("Stati Uniti", ignoreCase = true) -> {
                        if(!regioneStato.isNullOrEmpty()) "$localita, $regioneStato, USA" else "$localita, USA"
                    }

                    else -> {
                        if (!regioneStato.isNullOrEmpty() && regioneStato != localita){
                            "$localita, $regioneStato, $nazione"
                        } else if (nazione.isNotEmpty()) {
                            "$localita, $nazione"
                        } else {
                            localita
                        }
                    }
                }

                onResult(CityData(n = fullName, ln = address.longitude))
            } else {
                    onResult(null)
            }
        }

        val addresses = geocoder.getFromLocationName(cityName, 1)
        if(!addresses.isNullOrEmpty()) {
            processAddress(addresses[0])
        }else{
            onResult(null)
        }

        //Blocco per versioni Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //Per Android 13 o versioni superiori
            geocoder.getFromLocationName(cityName, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>){
                    if(addresses.isNotEmpty()) {
                        processAddress(addresses[0])
                    }else{
                        onResult(null)
                    }
                }
                override fun onError(errorMessage: String?){
                    onResult(null)
                }
            })
        }else{
            //Logica legacy per Android 12 o inferiori
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(cityName, 1)
            if (!addresses.isNullOrEmpty()) {
                processAddress(addresses[0])
            } else {
                onResult(null)
            }
        }

    } catch (e: Exception) {
        onResult(null)
    }
}

fun getCurrentLocation(context: Context, onLocationFetched: (CityData?) -> Unit){
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { gpsLocation ->
            if(gpsLocation != null){
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(gpsLocation.latitude, gpsLocation.longitude, 1)
                val fullName = if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val localita = address.locality ?: address.subAdminArea ?: "Posizione GPS"
                    val nazione = address.countryName ?: ""
                    if(nazione.isNotEmpty()) "$localita, $nazione" else localita
                }else{
                    "Posizione GPS"
                }

                onLocationFetched(CityData(n = fullName, ln = gpsLocation.longitude))
            }else{
                onLocationFetched(null)
            }
        }
    } catch (e: SecurityException){
        onLocationFetched(null)
    }
}

fun calculateRoleScores(chart: FullBaZiChart): RoleScores? {
    val dmStem = chart.day.stem ?: return null
    val dmElement = dmStem.element
    val dmPolarity = dmStem.polarity

    var friend = 0
    var robWealth = 0
    var output = 0
    var wealth = 0
    var officer = 0
    var resource = 0
    
    val components = listOfNotNull(
        chart.year.stem, chart.year.branch,
        chart.month.stem, chart.month.branch,
        chart.day.stem, chart.day.branch,
        chart.hour.stem, chart.hour.branch
    )

    for(comp in components) {

        val element = comp.element
        val polarity = comp.polarity
        val isSamePolarity = (polarity == dmPolarity)

        when {
            element == dmElement -> {
                if (isSamePolarity) friend++ else robWealth++
            }

            isProducing(dmElement, element) -> output++
            isControlling(dmElement, element) -> wealth++
            isProducing(element, dmElement) -> resource++
            isControlling(element, dmElement) -> officer++
        }
    }

    return RoleScores(
        friend = friend,
        robWealth = robWealth,
        output = output,
        wealth = wealth,
        resource = resource,
        officer = officer
    )
}

fun Branch.getHiddenStems(): List<Stem> {
    return when (this) {
        Branch.ZI -> listOf(Stem.GUI)
        Branch.CHOU -> listOf(Stem.JI, Stem.XIN, Stem.GUI)
        Branch.YIN -> listOf(Stem.JIA, Stem.BING, Stem.WU)
        Branch.MAO -> listOf(Stem.YI)
        Branch.CHEN -> listOf(Stem.WU, Stem.YI, Stem.GUI)
        Branch.SI -> listOf(Stem.BING, Stem.GENG, Stem.WU)
        Branch.WU -> listOf(Stem.DING, Stem.JI)
        Branch.WEI -> listOf(Stem.JI, Stem.DING, Stem.YI)
        Branch.SHEN -> listOf(Stem.GENG, Stem.REN, Stem.WU)
        Branch.YOU -> listOf(Stem.XIN)
        Branch.XU -> listOf(Stem.WU, Stem.XIN, Stem.DING)
        Branch.HAI -> listOf(Stem.REN, Stem.JIA)
    }
}

fun Pillar.getNaYinKey(): String{
    val s = this.stem ?: return ""
    val b = this.branch ?: return ""

    return when(s){
        Stem.JIA, Stem.YI -> when (b) {
            Branch.ZI, Branch.CHOU -> "ny_m1"
            Branch.YIN, Branch.MAO -> "ny_w5"
            Branch.CHEN, Branch.SI -> "ny_f5"
            Branch.WU, Branch.WEI -> "ny_m4"
            Branch.SHEN, Branch.YOU -> "ny_w2"
            Branch.XU, Branch.HAI -> "ny_f2"
        }
        Stem.BING, Stem.DING -> when (b) {
            Branch.ZI, Branch.CHOU -> "ny_w1"
            Branch.YIN, Branch.MAO -> "ny_f1"
            Branch.CHEN, Branch.SI -> "ny_e6"
            Branch.WU, Branch.WEI -> "ny_w4"
            Branch.SHEN, Branch.YOU -> "ny_f3"
            Branch.XU, Branch.HAI -> "ny_e3"
        }
        Stem.WU, Stem.JI -> when (b) {
            Branch.ZI, Branch.CHOU -> "ny_f4"
            Branch.YIN, Branch.MAO -> "ny_e2"
            Branch.CHEN, Branch.SI -> "ny_wd1"
            Branch.WU, Branch.WEI -> "ny_f6"
            Branch.SHEN, Branch.YOU -> "ny_e5"
            Branch.XU, Branch.HAI -> "ny_wd4"
        }
        Stem.GENG, Stem.XIN -> when (b) {
            Branch.ZI, Branch.CHOU -> "ny_e4"
            Branch.YIN, Branch.MAO -> "ny_wd3"
            Branch.CHEN, Branch.SI -> "ny_m3"
            Branch.WU, Branch.WEI -> "ny_e1"
            Branch.SHEN, Branch.YOU -> "ny_wd6"
            Branch.XU, Branch.HAI -> "ny_m6"
        }
        Stem.REN, Stem.GUI -> when (b) {
            Branch.ZI, Branch.CHOU -> "ny_wd5"
            Branch.YIN, Branch.MAO -> "ny_m5"
            Branch.CHEN, Branch.SI -> "ny_w3"
            Branch.WU, Branch.WEI -> "ny_wd2"
            Branch.SHEN, Branch.YOU -> "ny_m2"
            Branch.XU, Branch.HAI -> "ny_w6"
        }
    }
}

@Composable
fun getNaYinTitle(key: String?): String{
    if (key.isNullOrEmpty()) return ""

    val context = LocalContext.current
    val resName = "${key}_title"

    val resId = context.resources.getIdentifier(resName, "string", context.packageName)

    if (resId != 0){
        val fullTitle = stringResource(resId)
        return fullTitle.substringBefore(" (").trim()
    }

    return ""
}

fun getMonthNum(context: Context, monthName: String): Int{
    return try {
        val monthsArray = context.resources.getStringArray(R.array.months_arr)

        val formattedInput = monthName.trim()
            .lowercase()
            .replaceFirstChar { it.uppercase() }

        val index = monthsArray.indexOf(formattedInput)

        if(index != -1) {
            index + 1
        }else{
            formattedInput.toInt()
        }
    }catch(e: Exception){
            1 //if user input is invalid, fall back to default value (1 for Jan)
    }
}

fun formatToLowercase(text: String): String {
    return text.lowercase().replaceFirstChar {it.uppercase()}
}