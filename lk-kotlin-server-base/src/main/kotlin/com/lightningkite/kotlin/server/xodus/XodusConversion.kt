package com.lightningkite.kotlin.server.xodus

import com.fasterxml.jackson.databind.JsonNode
import com.lightningkite.kotlin.server.base.toJavaType
import jetbrains.exodus.entitystore.Entity
import jetbrains.exodus.entitystore.StoreTransaction
import lk.kotlin.jackson.MyJackson
import lk.kotlin.reflect.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*
import kotlin.reflect.KClass

interface XodusStorable {
    var id: String
}

inline fun <reified T : XodusStorable> StoreTransaction.write(item: T) = write(T::class, item, item.id)
fun <T : XodusStorable> StoreTransaction.write(type: KClass<T>, item: T) = write(type, item, item.id)

fun <T : Any> StoreTransaction.write(type: KClass<T>, item: T, id: String): String {
    return if (id.isEmpty()) {
        //new
        newEntity(type.qualifiedName!!).apply {
            write(type, item)
        }.toIdString()
    } else {
        getEntity(id).apply {
            write(type, item)
        }
        id
    }
}

fun <T : Any> StoreTransaction.get(type: KClass<T>, id: String): T {
    return this.getEntity(id).read(type)
}

inline fun <reified T : Any> Entity.read(): T = read(T::class)
fun <T : Any> Entity.read(type: KClass<T>): T {
    val item = type.reflectAsmConstruct()
    readInto(type, item)
    return item
}

inline fun <reified T : Any> Entity.readInto(item: T) = readInto(T::class, item)
fun <T : Any> Entity.readInto(type: KClass<T>, item: T) {
    for (field in type.fastMutableProperties.values) {
        val valueRead = this.get(field.name, field.fastType)
        if (valueRead == null && !field.fastType.nullable) {
            //Skip.  We'll let the default value stand.
            continue
        }
        field.reflectAsmSet(item, valueRead)
    }
}

inline fun <reified T : Any> Entity.write(item: T) = write(T::class, item)
fun <T : Any> Entity.write(type: KClass<T>, item: T) {
    for (field in type.fastMutableProperties.values) {
        this.set(field.name, field.fastType, field.reflectAsmGet(item))
    }
}

annotation class ShortString

private fun xodusZonedDateTimeRenderFormat() = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
private fun xodusZonedDateTimeParseFormat() = DateTimeFormatterBuilder()
        // date/time
        .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        // offset (hh:mm - "+00:00" when it's zero)
        .optionalStart().appendOffset("+HH:MM", "+00:00").optionalEnd()
        // offset (hhmm - "+0000" when it's zero)
        .optionalStart().appendOffset("+HHMM", "+0000").optionalEnd()
        // offset (hh - "Z" when it's zero)
        .optionalStart().appendOffset("+HH", "Z").optionalEnd()
        // create formatter
        .toFormatter()

fun Entity.get(name: String, typeInformation: TypeInformation): Any? {
    if (name == "id") return toIdString()
    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    return when (typeInformation.kclass) {
        Unit::class -> Unit
        Boolean::class,
        Int::class,
        Long::class,
        Float::class,
        Double::class,
        Byte::class,
        Short::class,
        Char::class -> getProperty(name)

        Date::class -> (getProperty(name) as? Long)?.let { Date(it) }
        ZonedDateTime::class -> (getProperty(name) as? String)?.let { xodusZonedDateTimeRenderFormat().parse(it) }

    //optimize?
        String::class -> if (typeInformation.annotations.any { it is ShortString })
            getProperty(name)
        else
            getBlobString(name)

        JsonNode::class -> getBlobString(name)?.let { MyJackson.mapper.readTree(it) }
        else -> {
            if (typeInformation.kclass.isEnum)
                (getProperty(name) as? String)?.let { typeInformation.kclass.enumValues[it] }
            else
                getBlobString(name)?.let {
                    MyJackson.mapper.readValue<Any?>(
                            it,
                            typeInformation.toJavaType()
                    )
                }
        }

    }
}

fun Entity.set(name: String, typeInformation: TypeInformation, value: Any?) {
    if (name == "id") return
    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    if (value == null)
        deleteProperty(name)
    else when (typeInformation.kclass) {
        Unit::class -> Unit
        Boolean::class,
        Int::class,
        Long::class,
        Float::class,
        Double::class,
        Byte::class,
        Short::class,
        Char::class -> setProperty(name, value as Comparable<*>)

        Date::class -> setProperty(name, (value as Date).time)
        ZonedDateTime::class -> setProperty(name, xodusZonedDateTimeRenderFormat().format(value as ZonedDateTime))

    //optimize?
        String::class -> if (typeInformation.annotations.any { it is ShortString })
            setProperty(name, value as String)
        else
            setBlobString(name, value as String)

        JsonNode::class -> setBlobString(name, MyJackson.mapper.writeValueAsString(value))
        else -> {
            if (typeInformation.kclass.isEnum)
                setBlobString(name, MyJackson.mapper.writeValueAsString(value))
            else
                setProperty(name, (value as Enum<*>).name)
        }

    }
}