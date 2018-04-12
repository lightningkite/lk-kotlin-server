package lk.kotlin.server.types

import lk.kotlin.reflect.TypeInformation
import lk.kotlin.server.types.common.PointerServerFunction
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.jvm.jvmErasure

val KClassPointerServerFunctionInputType = HashMap<KClass<*>, TypeInformation>()
val KClass<out PointerServerFunction<*, *>>.inputType
    get() = KClassPointerServerFunctionInputType.getOrPut(this) {
        this.allSupertypes.find { it.jvmErasure == PointerServerFunction::class }!!.let { TypeInformation.fromKotlin(it) }.typeParameters[1]
    }

//TODO: Get ID's type

//
//val KClassServerFunctionInvocation = HashMap<KClass<*>, ()->Any?>()
//var <T> KClass<out ServerFunction<T>>.invocation: ()->T
//    set(value){
//        KClassServerFunctionInvocation[this] = value
//    }
//    get() = KClassServerFunctionInvocation[this] as (()->T)
//
//val KClassServerFunctionReturnType = HashMap<KClass<*>, TypeInformation>()
//val KClass<out ServerFunction<*>>.returnType
//    get() = KClassServerFunctionReturnType.getOrPut(this) {
//        this.allSupertypes.find { it.jvmErasure == ServerFunction::class }!!.let{ TypeInformation.fromKotlin(it) }
//    }