package lk.kotlin.server.types.log

interface ServerFunctionLogger {
    fun log(call: HistoricalServerFunction)
}