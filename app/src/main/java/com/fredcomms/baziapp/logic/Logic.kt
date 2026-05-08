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



enum class EarthlyBranch(
    val chinese: String,
    val pinyin: String,
    val element: Element,
    val polarity: Polarity,
    val zodiac: String
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

fun getTenGods(dayMaster: HeavenlyStem, target: HeavenlyStem): String{
    val isSamePolarity = dayMaster.polarity == target.polarity

    return when{
        //Output
        isProducing(dayMaster.element, target.element) ->
            if(isSamePolarity) "Eating God (Pian Shi)" else "Hurting Officer (Shang Guan)"
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

fun getBaZiProfile(year: Int, month: Int, day: Int, hour: Int, minute: Int): String {
    return try {
    val solar = Solar(year, month, day, hour, minute, 0)
    val lunar = solar.lunar

    val dayStemChar = lunar.dayGan
    val dayBranchChar = lunar.dayZhi

    val dayStem = HeavenlyStem.fromChinese(dayStemChar)
    val dayBranch = EarthlyBranch.fromChinese(dayBranchChar)

    if (dayStem != null && dayBranch != null){
        "Day Pillar: ${dayStem.name} ${dayBranch.name} (${dayBranch.zodiac})"
    }else{
        "Data: $dayStemChar $dayBranchChar non trovata."
    }
    } catch (e: Exception) {
        "Errore nel calcolo del BaZi: ${e.message}"
    }
}

fun getElementColor(element: Element): Color {
    return when (element) {
        Element.FIRE -> Color(0xFFEF5350)
        Element.EARTH -> Color(0xFFFFCA28)
        Element.WOOD -> Color(0xFF66BB6A)
        Element.WATER -> Color(0xFF42A5F5)
        Element.METAL -> Color(0xFFBDBDBD)
    }
}

fun main() {
    val dm = HeavenlyStem.JIA // Legno Yang
    val anno = HeavenlyStem.BING // Fuoco Yang
    println("la relazione è  ${getTenGods(dm, anno)}")
    

}