package com.lightningkite.kotlin.server.base

import com.fasterxml.jackson.annotation.JsonTypeInfo
import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.fastFunctions
import kotlin.reflect.KClass

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
interface ServerFunction<T> {
    fun invoke(transaction: Transaction): T
}

val KClassServerFunctionReturnType = HashMap<KClass<*>, TypeInformation>()
val KClass<out ServerFunction<*>>.returnType
    get() = KClassServerFunctionReturnType.getOrPut(this) {
        TypeInformation(this.fastFunctions.find { it.name == "invoke" }!!.returnType)
    }