package com.lightningkite.kotlin.server.types.builtin

//class ResolvePointers<T>(
//        var names: Set<String> = setOf(),
//        var call: ServerFunction<T>
//) : ServerFunction<T> {
//
//    companion object {
//        const val maxCallsPerTransaction: Int = 10
//    }
//
//    var Transaction.calls: Int
//        get() = cache[ResolvePointers.Companion] as? Int ?: 0
//        set(value) {
//            cache[ResolvePointers.Companion] = value
//        }
//
//    override val returnType: TypeInformation
//        get() = call.returnType
//
//    override fun invoke(transaction: Transaction): T {
//        @Suppress("UNCHECKED_CAST")
//        val result = call.invoke(transaction) ?: return null as T
//        for(key in names){
//            val property = call.returnType.kclass.fastMutableProperties[key] ?: continue
//            if(transaction.calls++ > maxCallsPerTransaction){
//                throw IllegalArgumentException("Too many subcalls - request rejected.")
//            }
//            @Suppress("UNCHECKED_CAST")
//            val pointer = property.getUntyped(result) as Pointer<Any, Any?>
//            pointer.value = pointer.access.invoke(transaction)
//        }
//        return result
//    }
//}