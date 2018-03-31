package com.lightningkite.kotlin.server.types

import com.lightningkite.kotlin.server.base.HttpRequest
import com.lightningkite.kotlin.server.base.HttpRequestHandler
import lk.kotlin.reflect.TypeInformation

class TypedException(val code: Int, val type: TypeInformation, val data: Any?) : Exception(data.toString())

class TypedExceptionHttpRequestHandler() : HttpRequestHandler() {
    override fun handle(request: HttpRequest): Boolean {
        val lookup = request.method.plus("|").plus(request.path).let {
            if (it.endsWith('/'))
                it.dropLast(1)
            else it
        }
        val callback = map[lookup] ?: return false
        try {
            callback.invoke(request)
        } catch (e: TypedException) {
            request.respondWith(
                    code = e.code,
                    typeInformation = e.type,
                    output = e.data
            )
        } catch (e: Exception) {
            request.respondWith(
                    code = 500,
                    typeInformation = TypeInformation(Exception::class),
                    output = e
            )
        }
        return true
    }

}