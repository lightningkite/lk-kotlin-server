package lk.kotlin.server.types

import lk.kotlin.reflect.TypeInformation
import lk.kotlin.server.base.Transaction
import lk.kotlin.server.types.common.ServerFunction
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.jvm.jvmErasure

private val KClassServerFunctionInvocation = HashMap<KClass<*>, Any.(Transaction) -> Any?>()
var <S : ServerFunction<T>, T> KClass<S>.invocation: S.(Transaction) -> T
    set(value) {
        KClassServerFunctionInvocation[this] = value as (Any.(Transaction) -> T)
    }
    get() = KClassServerFunctionInvocation[this] as (S.(Transaction) -> T)

fun <T> ServerFunction<T>.invoke(transaction: Transaction) = javaClass.kotlin.invocation.invoke(this, transaction)

private val KClassServerFunctionReturnType = HashMap<KClass<*>, TypeInformation>()
val KClass<out ServerFunction<*>>.returnType
    get() = KClassServerFunctionReturnType.getOrPut(this) {
        this.allSupertypes.find { it.jvmErasure == ServerFunction::class }!!.let { TypeInformation.fromKotlin(it) }.typeParameters.first()
    }