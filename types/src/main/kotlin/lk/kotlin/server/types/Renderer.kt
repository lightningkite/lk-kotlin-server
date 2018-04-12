package lk.kotlin.server.types

import lk.kotlin.reflect.TypeInformation
import lk.kotlin.server.base.HttpRequest
import lk.kotlin.server.base.Transaction
import java.io.OutputStream

interface Renderer {
    fun <T> render(type: TypeInformation, data: T, httpRequest: HttpRequest, getTransaction: () -> Transaction, out: OutputStream)
}