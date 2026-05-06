package com.fredcomms.baziapp.logic

enum class Polarity {YANG, YIN}

enum class Element {FIRE, EARTH, METAL, WATER, WOOD}

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
    YI("乙", Element.WOOD, Polarity.YIN) 
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
|| (a == Element.EARTH && b.Element.METAL) 
|| (a == Element.METAL && b == Element.WATER) 
|| (a == Element.WATER && b == Element.WOOD)

fun isControlling(a: Element, b: Element) = (a == Element.WOOD && b == Element.EARTH) 
|| (a == Element.EARTH && b == Element.WATER) 
|| (a == Element.WATER && b == Element.FIRE) 
|| (a == Element.FIRE && b == Element.METAL) 
|| (a == Element.METAL && b == Element.WOOD)


fun main() {
    val dm = HeavenlyStem.JIA // Legno Yang
    val anno = HeavenlyStem.BING // Fuoco Yang
    println("la relazione è  ${getTenGods(dm, anno)}")
    

}