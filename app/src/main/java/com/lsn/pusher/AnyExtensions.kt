package com.lsn.pusher

import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import java.math.RoundingMode
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 全局扩展
 * Created by Aramis on 2017/8/28.
 */
fun Any.logE(str: String? = "null") {
    Log.e("===${javaClass.simpleName}===", str)
}

fun Any.toMD5(): String {
    try {
        //获取md5加密对象
        val instance: MessageDigest = MessageDigest.getInstance("MD5")
        //对字符串加密，返回字节数组
        val digest: ByteArray = instance.digest(this.toString().toByteArray())
        val sb = StringBuffer()
        for (b in digest) {
            //获取低八位有效值
            var i: Int = b.toInt() and 0xff
            //将整数转化为16进制
            var hexString = Integer.toHexString(i)
            if (hexString.length < 2) {
                //如果是一位的话，补0
                hexString = "0$hexString"
            }
            sb.append(hexString)
        }
        return sb.toString()

    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }

    return ""
}

fun keep(d: Double): String {
    val decimalFormat = DecimalFormat("#0.00")
    decimalFormat.roundingMode = RoundingMode.HALF_UP
    return decimalFormat.format(d)
}

fun <T> MutableCollection<T>.shift(): T {
    val first = this.first()
    this.remove(first)
    return first
}

fun <T> MutableCollection<T>.pop(): T {
    val last = this.last()
    this.remove(last)
    return last
}

fun RectF.toRect(): Rect {
    return Rect(this.left.toInt(), this.top.toInt(), this.right.toInt(), this.bottom.toInt())
}

fun Rect.toRectF(): RectF {
    return RectF(this.left.toFloat(), this.top.toFloat(), this.right.toFloat(), this.bottom.toFloat())

}

fun Int.toHex(): String {

    fun toHex16(n: Int): String {
        return if (n < 10) {
            n.toString()
        } else {
            when (n) {
                10 -> "A"
                11 -> "B"
                12 -> "C"
                13 -> "D"
                14 -> "E"
                else -> "F"
            }
        }
    }
    return if (this < 16) {
        toHex16(this)
    } else {
        val b = this / 16
        b.toHex() + toHex16(this % 16)
    }
}

/**
 * 十六进制转十进制
 */
fun String.toDecimal(): Long {
    val hs= if (this.length>2&& this[0]=='0'&& this[1].toLowerCase()=='x') this.substring(2 until this.length) else this
    fun getInt(s: String): Int {
        return when (s.toUpperCase()) {
            "A" -> 10
            "B" -> 11
            "C" -> 12
            "D" -> 13
            "E" -> 14
            "F" -> 15
            else -> s.toInt()
        }
    }

    var s = 0L
    for (index in (0 until hs.length)) {
        val c = hs[index]
        s += (getInt(c.toString()) * Math.pow(16.0, (hs.length - index - 1).toDouble())).toLong()
    }

    return s
}