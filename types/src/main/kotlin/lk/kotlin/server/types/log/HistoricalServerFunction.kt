package lk.kotlin.server.types.log

import lk.kotlin.server.types.common.ServerFunction
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