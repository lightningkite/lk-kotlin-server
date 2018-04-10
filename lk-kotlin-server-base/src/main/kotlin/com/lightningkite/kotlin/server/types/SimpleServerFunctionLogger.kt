package com.lightningkite.kotlin.server.types

import java.io.File

class SimpleServerFunctionLogger(
        val file: File = File("./log.txt")
) : ServerFunctionLogger {
    override fun log(call: HistoricalServerFunction) {
    }
}