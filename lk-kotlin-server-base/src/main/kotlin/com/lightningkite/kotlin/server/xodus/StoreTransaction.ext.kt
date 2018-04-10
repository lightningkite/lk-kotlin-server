package com.lightningkite.kotlin.server.xodus

import jetbrains.exodus.entitystore.StoreTransaction
import lk.kotlin.reflect.fastType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


inline fun <reified T : Any> StoreTransaction.modify(id: String, modifier: (T) -> Unit) = modify(T::class, id, modifier)
inline fun <T : Any> StoreTransaction.modify(type: KClass<T>, id: String, modifier: (T) -> Unit): T {
    return getEntity(id).apply {
        write(type, this.read(type).also(modifier))
    }.read(type)
}


inline fun <reified T : XodusStorable> StoreTransaction.write(item: T) = write(T::class, item, item.id).also { item.id = it }
fun <T : XodusStorable> StoreTransaction.write(type: KClass<T>, item: T) = write(type, item, item.id).also { item.id = it }
fun <T : Any> StoreTransaction.write(type: KClass<T>, item: T, id: String): String {
    return if (id.isEmpty()) {
        //new
        newEntity(type.xodusName).apply {
            write(type, item)
        }.toIdString()
    } else {
        getEntity(id).apply {
            write(type, item)
        }
        id
    }
}


inline fun <reified T : Any> StoreTransaction.get(id: String): T = get(T::class, id)
fun <T : Any> StoreTransaction.get(type: KClass<T>, id: String): T {
    return this.getEntity(id).read(type)
}


inline fun <reified T : Any> StoreTransaction.find(property: KProperty1<*, *>, value: Any) = find(T::class, property, value)
fun <T : Any> StoreTransaction.find(type: KClass<T>, property: KProperty1<*, *>, value: Any) = find(
        type.xodusName,
        property.name,
        XodusConversion.toXodus(value, property.fastType)
)

inline fun <reified T : Any> StoreTransaction.find(property: KProperty1<*, *>, from: Any, to: Any) = find(T::class, property, from, to)
fun <T : Any> StoreTransaction.find(type: KClass<T>, property: KProperty1<*, *>, from: Any, to: Any) = find(
        type.xodusName,
        property.name,
        XodusConversion.toXodus(from, property.fastType),
        XodusConversion.toXodus(to, property.fastType)
)

inline fun <reified T : Any> StoreTransaction.getAll() = getAll(T::class)
fun <T : Any> StoreTransaction.getAll(type: KClass<T>) = getAll(type.xodusName)

