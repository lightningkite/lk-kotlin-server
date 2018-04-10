package com.lightningkite.kotlin.server.types

import lk.kotlin.reflect.fastFunctions
import lk.kotlin.reflect.fastMutableProperties
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

class TypeSet {
    val types = HashSet<KClass<*>>()

    fun explore(type: KClass<*>) {
        if (!types.add(type)) return
        for (prop in type.fastMutableProperties) {
            explore(prop.value.returnType)
        }
        for (func in type.fastFunctions) {
            explore(func.returnType)
            for (param in func.parameters) {
                explore(param.type)
            }
        }
    }

    fun explore(type: KType) {
        explore(type.jvmErasure)
        for (sub in type.arguments) {
            val subType = sub.type
            if (subType != null) {
                explore(subType)
            }
        }
    }
}

