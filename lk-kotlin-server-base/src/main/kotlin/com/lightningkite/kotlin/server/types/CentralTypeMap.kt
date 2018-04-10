package com.lightningkite.kotlin.server.types

import com.fasterxml.jackson.databind.jsontype.NamedType
import lk.kotlin.jackson.MyJackson
import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.annotations.externalName
import kotlin.reflect.KClass

object CentralTypeMap {
    val nameToType = HashMap<String, KClass<*>>()

    fun explore(type: KClass<*>) {
        val set = TypeSet()
        set.explore(type)
        for (item in set.types) {
            if (nameToType.put(item.externalName, type) != null) throw IllegalArgumentException("Type name already registered!")
        }
    }

    fun setup() {
        MyJackson.mapper.registerSubtypes(*nameToType.map { NamedType(it.value.java, it.key) }.toTypedArray())
    }

    fun export(typeInformation: TypeInformation): String = typeInformation.kclass.externalName +
            (if (typeInformation.typeParameters.isEmpty()) {
                ""
            } else {
                typeInformation.typeParameters.joinToString(",", "<", ">") { export(it) }
            }) +
            if (typeInformation.nullable) "?" else ""

    fun import(reader: String): TypeInformation = TODO()
}