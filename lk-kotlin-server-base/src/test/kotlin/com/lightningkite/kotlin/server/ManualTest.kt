package com.lightningkite.kotlin.server

import com.lightningkite.kotlin.server.base.Context
import com.lightningkite.kotlin.server.base.ServerSettings
import com.lightningkite.kotlin.server.base.Transaction
import com.lightningkite.kotlin.server.base.respondHtml
import com.lightningkite.kotlin.server.jetty.asJettyHandler
import com.lightningkite.kotlin.server.types.Meta
import com.lightningkite.kotlin.server.types.ServerFunction
import com.lightningkite.kotlin.server.types.TypedExceptionHttpRequestHandler
import com.lightningkite.kotlin.server.types.rpc
import com.lightningkite.kotlin.server.xodus.XodusStorable
import com.lightningkite.kotlin.server.xodus.read
import com.lightningkite.kotlin.server.xodus.write
import com.lightningkite.kotlin.server.xodus.xodus
import jetbrains.exodus.entitystore.PersistentEntityStores
import lk.kotlin.reflect.TypeInformation
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.handler.ResourceHandler
import org.junit.Test
import java.util.*

class ManualTest {
    //Data Model
    data class DataEntry(
            @Meta.DoNotModify override var id: String = "",
            var data: String = "",
            var timestamp: Date = Date()
    ) : XodusStorable

    //Functionality
    class HelloWorldFunction(
            var name: String = "no-name"
    ) : ServerFunction<String> {
        override fun invoke(transaction: Transaction): String = "Hello, $name!"
    }

    class AddDataEntry(
            var entry: DataEntry = DataEntry()
    ) : ServerFunction<String> {
        override fun invoke(transaction: Transaction): String = transaction.xodus.write(entry)
    }

    class GetDataEntries : ServerFunction<List<DataEntry>> {
        override fun invoke(transaction: Transaction): List<DataEntry> {
            return transaction.xodus.getAll(DataEntry::class.qualifiedName!!)
                    .asSequence()
                    .take(100)
                    .map { it.read<DataEntry>() }
                    .toList()
        }
    }

    class BrokenFunction : ServerFunction<String> {
        override fun invoke(transaction: Transaction): String = throw IllegalArgumentException("Not allowed")
    }

    @Test
    fun main() {
        val context: Context = HashMap()
        context.xodus = PersistentEntityStores.newInstance("./xodus")

        ServerSettings.debugMode = true

        Server(8080).apply {
            handler = HandlerCollection(
                    TypedExceptionHttpRequestHandler().apply {
                        get("") {
                            respondHtml(html = "Hello!")
                        }
                        rpc("rpc", context, listOf(
                                HelloWorldFunction::class,
                                AddDataEntry::class,
                                GetDataEntries::class,
                                BrokenFunction::class
                        ).map { TypeInformation(it) })
                    }.asJettyHandler(),
                    ResourceHandler().apply {
                        isDirectoriesListed = true
                        welcomeFiles = arrayOf("index.html")
                        resourceBase = "C:\\Public"
                    }
            )
            start()
            join()
        }
    }
}