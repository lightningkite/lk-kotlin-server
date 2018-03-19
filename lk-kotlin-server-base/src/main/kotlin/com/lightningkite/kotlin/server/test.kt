package com.lightningkite.kotlin.server

import com.lightningkite.kotlin.server.base.*
import com.lightningkite.kotlin.server.xodus.XodusStorable
import com.lightningkite.kotlin.server.xodus.read
import com.lightningkite.kotlin.server.xodus.write
import com.lightningkite.kotlin.server.xodus.xodus
import jetbrains.exodus.entitystore.PersistentEntityStores
import lk.kotlin.jackson.jacksonFromString
import lk.kotlin.jackson.jacksonToString
import spark.Service
import java.util.*

class HelloWorldFunction(
        var name: String = "no-name"
) : ServerFunction<String> {
    override fun invoke(transaction: Transaction): String = "Hello, $name!"
}

data class DataEntry(
        override var id: String = "",
        var data: String = "",
        var timestamp: Date = Date()
) : XodusStorable

class AddDataEntry(
        var entry: DataEntry = DataEntry()
) : ServerFunction<String> {
    override fun invoke(transaction: Transaction): String = transaction.xodus.write(entry)
}

class GetDataEntries(
) : ServerFunction<List<DataEntry>> {
    override fun invoke(transaction: Transaction): List<DataEntry> {
        return transaction.xodus.getAll(DataEntry::class.qualifiedName!!)
                .asSequence()
                .take(100)
                .map { it.read<DataEntry>() }
                .toList()
    }
}

fun main(vararg args: String) {
    Service.ignite().apply {
        port(80)

        val context: Context = newContext()
        context.xodus = PersistentEntityStores.newInstance("./working/xodus")

        get("hello") { req, resp -> "Hello World!" }
        post("rpc") { req, resp ->
            try {
                Transaction(context).use {
                    val body = req.body()
                    val parsed = body.jacksonFromString(ServerFunction::class.java)
                    val result = parsed.invoke(it)
                    result.jacksonToString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                e.message
            }
        }
        Transaction(context).use {
            GetDataEntries().invoke(it)
        }
    }
}