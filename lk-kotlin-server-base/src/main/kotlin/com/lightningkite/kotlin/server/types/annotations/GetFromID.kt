package com.lightningkite.kotlin.server.types.annotations

import com.lightningkite.kotlin.server.types.Pointer
import com.lightningkite.kotlin.server.types.ServerFunction
import lk.kotlin.reflect.findType
import java.util.*
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor


@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class GetFromID(val getter: KClass<out ServerFunction<*>>)

private val kPropertyGetFromID = WeakHashMap<KAnnotatedElement, KClass<out ServerFunction<*>>>()
val KAnnotatedElement.getFromID: KClass<out ServerFunction<*>>?
    get() = kPropertyGetFromID.getOrPut(this) {
        annotations.findType<GetFromID>()?.getter
    }

fun KAnnotatedElement.getFromId(id: Any) = getFromID?.primaryConstructor?.let {
    val param = it.parameters.first()
    it.callBy(mapOf(
            param to Pointer<Any, Any?>(id)
    ))
}

fun KAnnotatedElement.getFromId(pointer: Pointer<*, *>) = getFromID?.primaryConstructor?.let {
    val param = it.parameters.first()
    it.callBy(mapOf(
            param to pointer
    ))
}