package lk.kotlin.server.xodus

import jetbrains.exodus.entitystore.Entity
import lk.kotlin.reflect.*
import lk.kotlin.reflect.annotations.estimatedLength
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

inline fun <reified T : Any> Entity.read(): T = read(T::class)
fun <T : Any> Entity.read(type: KClass<T>): T {
    val item = type.createInstance()
    readInto(type, item)
    return item
}

inline fun <reified T : Any> Entity.readInto(item: T) = readInto(T::class, item)
fun <T : Any> Entity.readInto(type: KClass<T>, item: T) {
    for (field in type.fastMutableProperties.values) {
        val valueRead = this.get(field.name, field.fastType)
        if (valueRead == null && !field.fastType.nullable) {
            //Skip.  We'll let the default value stand.
            continue
        }
        field.setUntyped(item, valueRead)
    }
}

inline fun <reified T : Any> Entity.write(item: T) = write(T::class, item)
fun <T : Any> Entity.write(type: KClass<T>, item: T) {
    for (field in type.fastMutableProperties.values) {
        this.set(field.name, field.fastType, field.getUntyped(item))
    }
}

fun Entity.get(name: String, typeInformation: TypeInformation): Any? {
    if (name == "id") return toIdString()
    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    return if ((typeInformation.estimatedLength ?: 255) > 255)
        getBlobString(name)?.let { XodusConversion.fromXodus(it, typeInformation) }
    else
        getProperty(name)?.let { XodusConversion.fromXodus(it, typeInformation) }
}

fun Entity.set(name: String, typeInformation: TypeInformation, value: Any?) {
    if (name == "id") return
    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    if (value == null)
        deleteProperty(name)
    else if ((typeInformation.estimatedLength ?: 255) > 255)
        setBlobString(name, XodusConversion.toXodus(value, typeInformation) as String)
    else
        setProperty(name, XodusConversion.toXodus(value, typeInformation))
}