package com.lightningkite.kotlin.server.types

import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.typeInformation

data class DynamicallyTypedData<T>(
        val type: TypeInformation,
        val data: T
)

inline fun <reified T : Any> T?.toDTD() = DynamicallyTypedData<T?>(typeInformation<T>(), this)