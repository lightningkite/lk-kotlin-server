package com.lightningkite.kotlin.server.types

import com.fasterxml.jackson.databind.ObjectMapper
import com.lightningkite.kotlin.server.base.HttpRequest
import com.lightningkite.kotlin.server.base.Transaction
import lk.kotlin.jackson.MyJackson
import lk.kotlin.reflect.TypeInformation
import java.io.OutputStream

class JacksonConverter(val mapper: ObjectMapper = MyJackson.mapper) : Renderer, Parser {

    override fun <T> render(type: TypeInformation, data: T, httpRequest: HttpRequest, getTransaction: () -> Transaction, out: OutputStream) {
        mapper.writerFor(type.toJavaType()).writeValue(out, data)
    }

    override fun <T> parse(type: TypeInformation, httpRequest: HttpRequest, getTransaction: () -> Transaction): T {
        return httpRequest.input.use {
            mapper.readValue<T>(it, type.toJavaType())
        }
    }
}