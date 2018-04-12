package lk.kotlin.server.types.common.annotations

import lk.kotlin.reflect.fastMutableProperties
import lk.kotlin.reflect.findType
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1


@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
annotation class PrimaryKey()

private val kPropertyPrimaryKey = WeakHashMap<KClass<*>, KMutableProperty1<*, *>?>()
val KClass<*>.primaryKey: KMutableProperty1<*, *>?
    get() = kPropertyPrimaryKey.getOrPut(this) {
        fastMutableProperties.values.find { it.annotations.findType<PrimaryKey>() != null }
    }

@Suppress("UNCHECKED_CAST")
fun KClass<*>.getPrimaryKeyValue(instance: Any): Any? = (primaryKey as? KMutableProperty1<Any, Any?>)?.get(instance)