package com.example.bukbot.data.api.Response

data class BetTicketResponse(
        val actionStatus: Int?,
        val actionMessage: String?,
        val reqId: String?,
        val currentOdd: Float?,
        val minStake: Float?,
        val maxStake: Float?,
        val pivotValue: String?,
        val homeScore: Int?,
        val awayScore: Int?,
        var sportBook: String? = null
) {
//    val piVal: Double?
//    init {
//        piVal = if (pivotValue != null) {
//            if (pivotValue.isNotEmpty()) {
//                if (pivotValue.contains("/")){
//                    val o = pivotValue.split(" / ")
//                    o[0].toDouble() / o[2].toDouble()
//                } else pivotValue.toDouble()
//            } else null
//        } else null
//    }
}
