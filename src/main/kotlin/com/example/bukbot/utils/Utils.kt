package com.example.bukbot.utils

import org.apache.commons.codec.binary.Hex
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.MessageDigest
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


fun getAccessToken(): String? {
    val username = "unity_group170"
    val password = "52fsPDeBn8"
    val builder: StringBuilder
    var dateFormat: DateFormat
    val time = Date()

    // combine the input fields

    // combine the input fields
    builder = StringBuilder()
    builder.append(username).append('_')

    dateFormat = SimpleDateFormat("dd/MM/yyyy")
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-0:00"))
    val date1 = dateFormat.format(time)
    builder.append(date1).append('_')

    builder.append(password).append('_')

    dateFormat = SimpleDateFormat("HH/mm")
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-0:00"))
    val date2 = dateFormat.format(time)
    builder.append(date2)

    return try {
        val bytesOfMessage = builder.toString().toByteArray(charset("utf-8"))
        val md = MessageDigest.getInstance("MD5")
        val theDigest = md.digest(bytesOfMessage)
        String(Hex.encodeHex(theDigest))
    } catch (e: Exception) {
        null
    }

}

fun Double.round(places: Int): Double {
    require(places >= 0)
    var bd = BigDecimal(this.toString())
    bd = bd.setScale(places, RoundingMode.HALF_UP)
    return bd.toDouble()
}

private fun clampMax(value: Int, max: Int): Int {
    return if (value > max) value else max
}

private fun clampMin(value: Int, min: Int): Int {
    return if (value > min) min else value
}

private fun clampMax(value: Double, max: Double): Double {
    return if (value > max) value else max
}

private fun clampMin(value: Double, min: Double): Double {
    return if (value > min) min else value
}