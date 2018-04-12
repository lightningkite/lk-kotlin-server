package lk.kotlin.server.types.log

import java.io.File

class SimpleServerFunctionLogger(
        val file: File = File("./log.txt")
) : ServerFunctionLogger {
    override fun log(call: HistoricalServerFunction) {
    }
}