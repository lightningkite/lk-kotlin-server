package com.lightningkite.kotlin.server.types

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

object Meta {

    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    @Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
    annotation class FriendlyName(val name: String)

    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    @Target(AnnotationTarget.CLASS)
    annotation class UrlName(val name: String)

    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    @Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
    annotation class Description(val text: String)

    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    @Target(AnnotationTarget.PROPERTY)
    annotation class Subtype(val name: String)

    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    @Target(AnnotationTarget.PROPERTY)
    annotation class Units(val text: String)

    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    @Target(AnnotationTarget.PROPERTY)
    annotation class DecimalPoints(val number: Int)

    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    @Target(AnnotationTarget.PROPERTY)
    annotation class ReferenceTo(val type: KClass<*>, val canBeFoundBy: KClass<*>)

    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    @Target(AnnotationTarget.PROPERTY)
    annotation class DoNotModify()

}

private val kclassFriendlyName = WeakHashMap<KClass<*>, String>()
val KClass<*>.friendlyName: String
    get() = kclassFriendlyName.getOrPut(this) {
        findAnnotation<Meta.FriendlyName>()?.name ?: this.simpleName?.nameify() ?: this.toString().nameify()
    }

private val kclassUrlName = WeakHashMap<KClass<*>, String>()
val KClass<*>.urlName: String
    get() = kclassUrlName.getOrPut(this) {
        findAnnotation<Meta.UrlName>()?.name ?: this.simpleName
    }

private val kclassDescription = WeakHashMap<KClass<*>, String?>()
val KClass<*>.description: String?
    get() = kclassDescription.getOrPut(this) {
        findAnnotation<Meta.Description>()?.text
    }

private val kProperty1FriendlyName = WeakHashMap<KProperty1<*, *>, String>()
val KProperty1<*, *>.friendlyName: String
    get() = kProperty1FriendlyName.getOrPut(this) {
        findAnnotation<Meta.FriendlyName>()?.name ?: this.name.nameify()
    }

private val kProperty1Description = WeakHashMap<KProperty1<*, *>, String?>()
val KProperty1<*, *>.description: String?
    get() = kProperty1Description.getOrPut(this) {
        findAnnotation<Meta.Description>()?.text
    }

private val kpropertySubtype = WeakHashMap<KProperty1<*, *>, String?>()
val KProperty1<*, *>.subtype: String?
    get() = kpropertySubtype.getOrPut(this) {
        findAnnotation<Meta.Subtype>()?.name
    }

private val kpropertyUnits = WeakHashMap<KProperty1<*, *>, String?>()
val KProperty1<*, *>.units: String?
    get() = kpropertyUnits.getOrPut(this) {
        findAnnotation<Meta.Units>()?.text
    }

private val kpropertyDecimalPoints = WeakHashMap<KProperty1<*, *>, Int>()
val KProperty1<*, *>.decimalPoints: Int
    get() = kpropertyDecimalPoints.getOrPut(this) {
        findAnnotation<Meta.DecimalPoints>()?.number ?: 2
    }

private val kpropertyDoNotModify = WeakHashMap<KProperty1<*, *>, Boolean>()
val KProperty1<*, *>.doNotModify: Boolean
    get() = kpropertyDoNotModify.getOrPut(this) {
        findAnnotation<Meta.DoNotModify>() != null
    }

private val kpropertyReferenceTo = WeakHashMap<KProperty1<*, *>, Meta.ReferenceTo?>()
val KProperty1<*, *>.referenceTo: Meta.ReferenceTo?
    get() = kpropertyReferenceTo.getOrPut(this) {
        findAnnotation<Meta.ReferenceTo>()
    }

private val addSpaceRegex = Regex("[A-Z0-9]")
fun String.nameify(): String {
    return this.replace(addSpaceRegex) { " " + it.value }.trim().capitalize()
}