package com.lightningkite.kotlin.server.base

import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KClass

class Transaction(
        val user: Any? = null
)

//Ser/des
//KType might be slow?  This is the alternative
class TypeInformation(
        val kclass: KClass<*>,
        val nullable: Boolean,
        val typeParameters: List<TypeInformation> = listOf()
)

interface Parser {
    fun <T> parse(type: TypeInformation, data: InputStream): T
}

interface Renderer {
    fun <T> render(type: TypeInformation, data: T): OutputStream
}

annotation class Description(val description: String)


//Database stuff
object SecurityRule {
    fun <T> always() = { _: Transaction, _: T -> true }
    fun <T> never() = { _: Transaction, _: T -> false }
}

annotation class ReadSecurityRule<T>(
        /**
         * Determines if the user is allowed to read this object.
         */
        val read: KClass<out (Transaction, T) -> Boolean>
)

annotation class WriteSecurityRule<T>(
        /**
         * Determines if the given data is allowed to be written to this object.
         */
        val write: KClass<out (Transaction, T, T) -> Boolean>
)