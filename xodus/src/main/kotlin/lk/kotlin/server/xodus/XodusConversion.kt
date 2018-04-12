package lk.kotlin.server.xodus

import com.fasterxml.jackson.databind.JsonNode
import lk.kotlin.jackson.MyJackson
import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.enumValues
import lk.kotlin.reflect.fastAllSuperclasses
import lk.kotlin.reflect.fastSuperclasses
import lk.kotlin.server.types.common.HasId
import lk.kotlin.server.types.common.PointerServerFunction
import lk.kotlin.server.types.inputType
import lk.kotlin.server.types.toJavaType
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

object XodusConversion {

    fun toXodus(value: Any, type: TypeInformation): Comparable<*> = when (type.kclass) {
        Boolean::class,
        Int::class,
        Long::class,
        Float::class,
        Double::class,
        Byte::class,
        Short::class,
        Char::class -> value as Comparable<*>

        Date::class -> (value as Date).time
        ZonedDateTime::class -> (value as ZonedDateTime).toInstant().toEpochMilli()

        String::class -> value as String
        JsonNode::class -> MyJackson.mapper.writeValueAsString(value as JsonNode)

        else -> {
            when {
                value is PointerServerFunction<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    toXodus(
                            value = (value as PointerServerFunction<HasId<Any>, Any>).id,
                            type = (type.kclass as KClass<PointerServerFunction<*, *>>).inputType
                    )
                }
                type.kclass.fastSuperclasses.contains(Enum::class) -> {
                    (value as Enum<*>).name
                }
                else -> {
                    MyJackson.mapper.writerFor(type.toJavaType()).writeValueAsString(value)
                }
            }
        }
    }

    fun fromXodus(value: Any, type: TypeInformation): Any = when (type.kclass) {
        Boolean::class,
        Int::class,
        Long::class,
        Float::class,
        Double::class,
        Byte::class,
        Short::class,
        Char::class -> value

        Date::class -> Date(value as Long)
        ZonedDateTime::class -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(value as Long), ZoneId.systemDefault())

        String::class -> value as String
        JsonNode::class -> MyJackson.mapper.writeValueAsString(value as JsonNode)

        else -> {
            when {
                type.kclass.fastAllSuperclasses.contains(PointerServerFunction::class) -> {
                    //create instance
                    @Suppress("UNCHECKED_CAST")
                    val instance = type.kclass.createInstance() as PointerServerFunction<HasId<Any>, Any>
                    @Suppress("UNCHECKED_CAST")
                    instance.id = fromXodus(
                            value = value,
                            type = (type.kclass as KClass<PointerServerFunction<*, *>>).inputType
                    )
                    instance
                }
                type.kclass.fastSuperclasses.contains(Enum::class) -> {
                    type.kclass.enumValues[value as String]!!
                }
                else -> {
                    MyJackson.mapper.readerFor(type.toJavaType()).readValue<Any>(value as String)
                }
            }
        }
    }
}