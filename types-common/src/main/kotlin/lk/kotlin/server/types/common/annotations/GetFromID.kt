package lk.kotlin.server.types.common.annotations

import lk.kotlin.reflect.findType
import lk.kotlin.server.types.common.PointerServerFunction
import java.util.*
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor


@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class GetFromID(val getter: KClass<out PointerServerFunction<*, *>>)

private val kPropertyGetFromID = WeakHashMap<KAnnotatedElement, KClass<out PointerServerFunction<*, *>>>()
val KAnnotatedElement.getFromID: KClass<out PointerServerFunction<*, *>>?
    get() = kPropertyGetFromID.getOrPut(this) {
        annotations.findType<GetFromID>()?.getter
    }

fun KAnnotatedElement.getFromId(id: Any) = getFromID?.primaryConstructor?.let {
    val param = it.parameters.first()
    it.callBy(mapOf(
            param to id
    ))
}