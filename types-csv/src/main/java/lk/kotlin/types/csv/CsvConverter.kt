package lk.kotlin.types.csv

import lk.kotlin.reflect.TypeInformation
import lk.kotlin.server.base.HttpRequest
import lk.kotlin.server.base.Transaction
import lk.kotlin.server.types.Parser
import lk.kotlin.server.types.Renderer
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.text.DateFormat
import java.util.*
import kotlin.reflect.KClass

class CsvConverter: Renderer, Parser{

    val subConverters = HashMap<KClass<*>, CsvSubConverter>()
    val subConverterGenerators = ArrayList<CsvSubConverterGenerator>()
    operator fun get(type:KClass<*>):CsvSubConverter = subConverters.getOrPut(type){
        subConverterGenerators.asSequence().mapNotNull { it.invoke(type) }.firstOrNull()
            ?: throw IllegalArgumentException("No known way to handle type $type")
    }

    fun escape(value:String): String{
        return if(value.contains(',') || value.contains('"')){
            "\"" + value.replace("\"", "\"\"") + "\""
        } else value
    }

    fun unescape(string:String):String{
        return if(string.startsWith('"')){
            string.drop(1).dropLast(1).replace("\"\"", "\"")
        } else string
    }

    override fun <T> render(
            type: TypeInformation,
            data: T,
            httpRequest: HttpRequest,
            getTransaction: () -> Transaction,
            out: OutputStream
    ) {
        OutputStreamWriter(out).use { writer ->
            if(type.kclass == List::class){
                val subtype = type.typeParameters.first()
                val sub = this[subtype.kclass]
                sub.renderHeader("", subtype){
                    writer.append(escape(it))
                    writer.append(',')
                }
                writer.append('\n')
                for(item in (data as List<Any?>)){
                    sub.render(subtype, item){
                        writer.append(escape(it))
                        writer.append(',')
                    }
                    writer.append('\n')
                }
            } else {
                val sub = this[type.kclass]
                sub.renderHeader("", type){
                    writer.append(escape(it))
                    writer.append(',')
                }
                writer.append('\n')
                sub.render(type, data){
                    writer.append(escape(it))
                    writer.append(',')
                }
                writer.append('\n')
            }
            writer.flush()
        }
    }

    override fun <T> parse(
            type: TypeInformation,
            httpRequest: HttpRequest,
            getTransaction: () -> Transaction
    ): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    init {
        subConverters[Byte::class] = SingleColumnConverter{ it.toByte() }
        subConverters[Short::class] = SingleColumnConverter{ it.toShort() }
        subConverters[Int::class] = SingleColumnConverter{ it.toInt() }
        subConverters[Long::class] = SingleColumnConverter{ it.toLong() }
        subConverters[Float::class] = SingleColumnConverter{ it.toFloat() }
        subConverters[Double::class] = SingleColumnConverter{ it.toDouble() }
        subConverters[Boolean::class] = SingleColumnConverter{ it.toBoolean() }
        subConverters[String::class] = SingleColumnConverter{ it }
        subConverters[Date::class] = SingleColumnConverter(
                renderToString = { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(it) },
                parseFromString = { SuperDateParser.parse(it) }
        )

        subConverterGenerators += DefaultCsvConverterGenerator(this)
    }
}

