package com.lightningkite.kotlin.server.types

import java.util.*

data class HistoricalServerFunction(
        var timestamp: Date = Date(),
        var userIdentifier: Any?,
        var call: ServerFunction<*>,
        var result: Any?
) {
    companion object {
        fun make(
                user: Any?,
                call: ServerFunction<*>,
                result: Any?
        ) {
//            return HistoricalServerFunction(
//                    userIdentifier = user,
//                    call = call,
//                    result = result
//            )
        }
    }
}