package com.lightningkite.kotlin.server.xodus

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation


@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
annotation class XodusName(val name: String)

val KClass<*>.xodusName
    get() = findAnnotation<XodusName>()?.name ?: qualifiedName ?: toString()