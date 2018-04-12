package lk.kotlin.server.types

import lk.kotlin.reflect.TypeInformation
import lk.kotlin.server.base.HttpRequest
import lk.kotlin.server.base.Transaction

interface Parser {
    fun <T> parse(type: TypeInformation, httpRequest: HttpRequest, getTransaction: () -> Transaction): T
}