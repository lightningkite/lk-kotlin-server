package com.lightningkite.kotlin.server.types

import com.lightningkite.kotlin.server.base.HttpRequest
import com.lightningkite.kotlin.server.base.Transaction
import lk.kotlin.reflect.TypeInformation

interface Parser {
    fun <T> parse(type: TypeInformation, httpRequest: HttpRequest, getTransaction: () -> Transaction): T
}