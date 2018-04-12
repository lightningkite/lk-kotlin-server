package lk.kotlin.server.types.common.annotations

import lk.kotlin.reflect.findType
import lk.kotlin.server.types.common.HasId
import lk.kotlin.server.types.common.ServerFunction
import java.util.*
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance


@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class Query(val query: KClass<out ServerFunction<out Iterable<HasId<*>>>>)

private val kPropertyQuery = WeakHashMap<KAnnotatedElement, KClass<out ServerFunction<out Iterable<HasId<*>>>>>()
val KAnnotatedElement.queryClass: KClass<out ServerFunction<out Iterable<HasId<*>>>>?
    get() = kPropertyQuery.getOrPut(this) {
        annotations.findType<Query>()?.query
    }

fun KAnnotatedElement.query() = queryClass?.createInstance()