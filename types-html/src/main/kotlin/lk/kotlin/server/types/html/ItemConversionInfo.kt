package lk.kotlin.server.types.html

import lk.kotlin.reflect.TypeInformation
import kotlin.reflect.KProperty1

data class ItemConversionInfo<out T>(
        val context: ConversionContext,
        val depth: Int,
        val callDepth: Int,
        val type: TypeInformation,
        val property: KProperty1<*, *>?,
        val name: String,
        val data: T?
)