package com.fredcomms.baziapp.logic
import com.nlf.calendar.Solar
import com.nlf.calendar.Lunar
import androidx.compose.ui.graphics.Color

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
            values().find { it.chinese == char }
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
    val name: String
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
            values().find { it.chinese == char }
    }
}

data class FullBaZiChart(
    val year: Pillar,
    val month: Pillar,
    val day: Pillar,
    val hour: Pillar
)

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

fun getTimezoneForCountry(countryCode: String): Double{
    return when (countryCode){
        "IT", "FR", "DE", "ES" -> 1.0 //Central European Time
        "US" -> -5.0 //Eastern Standard Time
        "CN" -> 8.0 //China Standard Time
        "GB", "PT" -> 0.0 //Greenwich Mean Time
        "JP", "KR" -> 9.0 //Japan Standard Time
        else -> 1.0 //Default to CET
    }
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
    return try {
        HeavenlyStem.valueOf(name.trim().uppercase())
    } catch (e: Exception) {
        null
    }
}

fun getFullBaZi(year: Int, month: Int, day: Int, hour: Int, minute: Int, longitude: Double, countryCode: String): FullBaZiChart {
    return try {
        val tz = getTimezoneForCountry(countryCode)

        val (solarH, solarM) = getTrueSolarTime(hour, minute, longitude, tz)

        val solar = Solar.fromYmdHms(year, month, day, solarH, solarM, 0)
        val lunar = solar.lunar
        val baZi = lunar.baZi

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

fun main() {
    val dm = HeavenlyStem.JIA // Legno Yang
    val anno = HeavenlyStem.BING // Fuoco Yang
    println("la relazione è  ${getTenGods(dm, anno)}")
    

}