package com.lightningkite.kotlin.server.base

data class HttpError(
        var httpCode: Int,
        var message: String,
        var data: DynamicallyTypedData<*>
)