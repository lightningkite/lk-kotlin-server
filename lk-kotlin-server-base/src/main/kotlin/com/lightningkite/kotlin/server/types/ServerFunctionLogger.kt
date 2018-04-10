package com.lightningkite.kotlin.server.types

interface ServerFunctionLogger {
    fun log(call: HistoricalServerFunction)
}