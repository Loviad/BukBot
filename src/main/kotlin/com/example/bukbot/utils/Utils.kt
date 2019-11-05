package com.example.bukbot.utils

import org.apache.commons.codec.binary.Hex
import java.security.MessageDigest
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.TimeZone



fun getAccessToken(): String? {
    val username = "unity_group153"
    val password = "71Ul4aeCkh"
    val builder: StringBuilder
    var dateFormat: DateFormat
    val time = Date()

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

    try {
        val bytesOfMessage = builder.toString().toByteArray(charset("utf-8"))
        val md = MessageDigest.getInstance("MD5")
        val theDigest = md.digest(bytesOfMessage)

        return String(Hex.encodeHex(theDigest))
    } catch (e: Exception) {
        return null
    }

}