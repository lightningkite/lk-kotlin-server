package com.lightningkite.kotlin.server.types.annotations

import com.lightningkite.kotlin.server.types.ServerFunction
import lk.kotlin.reflect.findType
import java.util.*
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance


@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class Query(val query: KClass<out ServerFunction<out Iterable<*>>>)

private val kPropertyQuery = WeakHashMap<KAnnotatedElement, KClass<out ServerFunction<out Iterable<*>>>>()
val KAnnotatedElement.queryClass: KClass<out ServerFunction<out Iterable<*>>>?
    get() = kPropertyQuery.getOrPut(this) {
        annotations.findType<Query>()?.query
    }

fun KAnnotatedElement.query() = queryClass?.createInstance()