package lk.kotlin.server.xodus

import lk.kotlin.reflect.annotations.externalName
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation


@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
annotation class XodusName(val name: String)

val KClass<*>.xodusName
    get() = findAnnotation<XodusName>()?.name ?: externalName