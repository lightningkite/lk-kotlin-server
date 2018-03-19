package com.lightningkite.kotlin.server.base

inline fun <reified T : Any> Context.getType(): T? = get(T::class.qualifiedName!!) as? T
inline fun <reified T : Any> Context.getOrPutType(gen: () -> T): T = getOrPut(T::class.qualifiedName!!, gen) as T
inline fun <reified T : Any> Context.putType(item: T) = put(T::class.qualifiedName!!, item)
