package dk.mzw.pyroman

import org.scalajs.dom.{KeyboardEvent, document}

class Keys {
    var keys = Set[Int]()

    def apply(keyCode : Int) : Boolean = keys(keyCode)

    def factor(keyCode : Int) : Int = if(keys(keyCode)) 1 else 0
    def factor(keyCode1 : Int, keyCode2 : Int) : Int = -factor(keyCode1) + factor(keyCode2)

    document.onkeydown = {event : KeyboardEvent =>
        keys += event.keyCode
    }

    document.onkeyup = {event : KeyboardEvent =>
        keys -= event.keyCode
    }
}

object Keys {
    val backspace = 8
    val tab = 9
    val enter = 13
    val shift = 16
    val ctrl = 17
    val alt = 18
    val pause = 19
    val capsLock = 20
    val escape = 27
    val pageUp = 33
    val pageDown = 34
    val end = 35
    val home = 36
    val leftArrow = 37
    val upArrow = 38
    val rightArrow = 39
    val downArrow = 40
    val insert = 45
    val delete = 46
    val number0 = 48
    val number1 = 49
    val number2 = 50
    val number3 = 51
    val number4 = 52
    val number5 = 53
    val number6 = 54
    val number7 = 55
    val number8 = 56
    val number9 = 57
    val a = 65
    val b = 66
    val c = 67
    val d = 68
    val e = 69
    val f = 70
    val g = 71
    val h = 72
    val i = 73
    val j = 74
    val k = 75
    val l = 76
    val m = 77
    val n = 78
    val o = 79
    val p = 80
    val q = 81
    val r = 82
    val s = 83
    val t = 84
    val u = 85
    val v = 86
    val w = 87
    val x = 88
    val y = 89
    val z = 90
    val leftWindowKey = 91
    val rightWindowKey = 92
    val selectKey = 93
    val numpad0 = 96
    val numpad1 = 97
    val numpad2 = 98
    val numpad3 = 99
    val numpad4 = 100
    val numpad5 = 101
    val numpad6 = 102
    val numpad7 = 103
    val numpad8 = 104
    val numpad9 = 105
    val multiply = 106
    val add = 107
    val subtract = 109
    val decimalPoint = 110
    val divide = 111
    val f1 = 112
    val f2 = 113
    val f3 = 114
    val f4 = 115
    val f5 = 116
    val f6 = 117
    val f7 = 118
    val f8 = 119
    val f9 = 120
    val f10 = 121
    val f11 = 122
    val f12 = 123
    val numLock = 144
    val scrollLock = 145
    val semiColon = 186
    val equalSign = 187
    val comma = 188
    val dash = 189
    val period = 190
    val forwardSlash = 191
    val graveAccent = 192
    val openBracket = 219
    val backSlash = 220
    val closeBraket = 221
    val singleQuote = 222
}
