package com.fredcomms.baziapp.logic
import com.nlf.calendar.Solar
import com.nlf.calendar.Lunar
import androidx.compose.ui.graphics.Color
import android.location.Geocoder
import android.content.Context
import java.util.Locale
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class Element {FIRE, EARTH, METAL, WATER, WOOD}

enum class Polarity {YANG, YIN}

enum class HeavenlyStem(val chinese: String, val element: Element, val polarity: Polarity){
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
        fun fromChinese(char: String): HeavenlyStem? =
            entries.find { it.chinese == char }
    }
}

data class Pillar(
    val stem: HeavenlyStem?,
    val branch: EarthlyBranch?
)


enum class EarthlyBranch(
    val chinese: String,
    val pinyin: String,
    val element: Element,
    val polarity: Polarity,
    val stemName: String
){
    ZI("子", "Zi", Element.WATER, Polarity.YANG, "Rat"),
    CHOU("丑", "Chou", Element.EARTH, Polarity.YIN, "Ox"),
    YIN("寅", "Yin", Element.WOOD, Polarity.YANG, "Tiger"),
    MAO("卯", "Mao", Element.WOOD, Polarity.YIN, "Rabbit"),
    CHEN("辰", "Chen", Element.EARTH, Polarity.YANG, "Dragon"),
    SI("巳", "Si", Element.FIRE, Polarity.YIN, "Snake"),
    WU("午", "Wu", Element.FIRE, Polarity.YANG, "Horse"),
    WEI("未", "Wei", Element.EARTH, Polarity.YIN, "Goat"),
    SHEN("申", "Shen", Element.METAL, Polarity.YANG, "Monkey"),
    YOU("酉", "You", Element.METAL, Polarity.YIN, "Rooster"),
    XU("戌", "Xu", Element.EARTH, Polarity.YANG, "Dog"),
    HAI("亥", "Hai", Element.WATER, Polarity.YIN, "Pig");

    companion object {
        fun fromChinese(char: String): EarthlyBranch? =
            entries.find { it.chinese == char }
    }
}

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

fun getPillar(stem: HeavenlyStem?, branch: EarthlyBranch?): Pillar {
    return Pillar(stem, branch)}

fun getTrueSolarTime(hour: Int, minute: Int, longitude: Double): Pair<Int, Int> {
    val timezoneMeridian = 15.0
    val diffMinutes = ((longitude - timezoneMeridian) * 4).toInt()

    var totalMinutes = hour * 60 + minute + diffMinutes

    //Gestione overflow/underflow delle 24 ore
    if(totalMinutes < 0) totalMinutes += 1440
    if(totalMinutes >= 1440 ) totalMinutes -= 1440

    return Pair(totalMinutes / 60, totalMinutes % 60)
}

fun getTenGods(dayMaster: HeavenlyStem, target: HeavenlyStem): String{
    val isSamePolarity = dayMaster.polarity == target.polarity

    return when{
        //Output
        isProducing(dayMaster.element, target.element) ->
            if(isSamePolarity) "Hurting Officer (Shang Guan)" else "Eating God (Pian Shi)"
        //Wealth
        isControlling(dayMaster.element, target.element) ->
            if(isSamePolarity) "Indirect Wealth (Pian Cai)" else "Direct Wealth (Zheng Cai)"

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

fun findStem(name: String): HeavenlyStem? {
    return HeavenlyStem.valueOf(name.trim().uppercase())
}

fun getFullBaZi(year: Int, month: Int, day: Int, hour: Int, minute: Int, longitude: Double): FullBaZiChart {
    return try {

        val (solarH, solarM) = getTrueSolarTime(hour, minute, longitude)

        val solar = Solar.fromYmdHms(year, month, day, solarH, solarM, 0)
        val baZi = solar.lunar.baZi

        fun extractPillar(baziPair: String): Pillar {
            val s = HeavenlyStem.fromChinese(baziPair.substring(0,1))
            val b = EarthlyBranch.fromChinese(baziPair.substring(1,2))
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
        isProducing(dayMaster, target) && dayMasterPolarity == targetPolarity -> "Hurting Officer"
        isProducing(dayMaster, target) && dayMasterPolarity != targetPolarity -> "Eating God"
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
        val addresses = geocoder.getFromLocationName(cityName, 1)
        if(!addresses.isNullOrEmpty()) {
            val addr = addresses[0]
            onResult(CityData(n = addr.locality ?: cityName, ln = addr.longitude))
        }else{
            onResult(null)
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
                val cityName = if (!addresses.isNullOrEmpty()){
                    addresses[0].locality ?: "Posizione GPS"
                }else{
                    "Posizione GPS"
                }

                onLocationFetched(CityData(n = cityName, ln = gpsLocation.longitude))
            }else{
                onLocationFetched(null)
            }
        }
    } catch (e: SecurityException){
        onLocationFetched(null)
    }
}
    

fun main(){}