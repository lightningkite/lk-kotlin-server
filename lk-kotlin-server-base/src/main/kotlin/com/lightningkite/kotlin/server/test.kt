package com.lightningkite.kotlin.server

import com.lightningkite.kotlin.server.base.*
import com.lightningkite.kotlin.server.spark.getFunction
import com.lightningkite.kotlin.server.spark.parseProcessRespond
import com.lightningkite.kotlin.server.xodus.XodusStorable
import com.lightningkite.kotlin.server.xodus.read
import com.lightningkite.kotlin.server.xodus.write
import com.lightningkite.kotlin.server.xodus.xodus
import jetbrains.exodus.entitystore.PersistentEntityStores
import lk.kotlin.reflect.TypeInformation
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
        getFunction<HelloWorldFunction>("helloworld", context, { null }, { req, func -> func.name = req.queryParamOrDefault("name", "no-name") })

        val anyCallTypeInfo = TypeInformation(ServerFunction::class)
        post("rpc") { req, resp ->
            req.parseProcessRespond<ServerFunction<*>>(resp, context, { null }, anyCallTypeInfo)
        }

        Transaction(context).use {
            GetDataEntries().invoke(it)
        }
    }
}