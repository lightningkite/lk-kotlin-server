package com.lightningkite.kotlin.server.base

import lk.kotlin.reflect.fastSuperclasses
import kotlin.reflect.KClass


fun Collection<KClass<*>>.closest(type: KClass<*>): KClass<*> {
    if (contains(type)) return type
    val queue = arrayListOf(type)
    while (queue.isNotEmpty()) {
        val next = queue.removeAt(0)
        if (next == Any::class) continue
        if (contains(next)) return next
        //add all supertypes
        queue.addAll(next.fastSuperclasses)
    }
    return Any::class
}

fun Map<KClass<*>, *>.closestKey(type: KClass<*>): KClass<*> = keys.closest(type)
//fun <T> Map<KClass<*>, T>.closest(type:KClass<*>)