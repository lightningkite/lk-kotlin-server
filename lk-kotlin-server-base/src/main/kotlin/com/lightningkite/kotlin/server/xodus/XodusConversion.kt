package com.lightningkite.kotlin.server.xodus

import com.fasterxml.jackson.databind.JsonNode
import com.lightningkite.kotlin.server.types.Pointer
import com.lightningkite.kotlin.server.types.toJavaType
import lk.kotlin.jackson.MyJackson
import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.enumValues
import lk.kotlin.reflect.fastSuperclasses
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

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

        Pointer::class -> toXodus((value as Pointer<Any, Any>).key, type.typeParameters[1])

        Date::class -> (value as Date).time
        ZonedDateTime::class -> (value as ZonedDateTime).toInstant().toEpochMilli()

        String::class -> value as String
        JsonNode::class -> MyJackson.mapper.writeValueAsString(value as JsonNode)

        else -> {
            when {
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

        Pointer::class -> Pointer(fromXodus(value, type.typeParameters[1]), null)

        Date::class -> Date(value as Long)
        ZonedDateTime::class -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(value as Long), ZoneId.systemDefault())

        String::class -> value as String
        JsonNode::class -> MyJackson.mapper.writeValueAsString(value as JsonNode)

        else -> {
            when {
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