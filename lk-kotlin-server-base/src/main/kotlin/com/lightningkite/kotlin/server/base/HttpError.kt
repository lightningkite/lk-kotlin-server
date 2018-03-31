package com.lightningkite.kotlin.server.base


data class HttpError(
        var code: Int,
        var message: String
)

