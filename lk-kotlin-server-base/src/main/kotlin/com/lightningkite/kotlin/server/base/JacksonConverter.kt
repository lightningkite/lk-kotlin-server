package com.lightningkite.kotlin.server.base

import com.fasterxml.jackson.databind.ObjectMapper
import lk.kotlin.jackson.MyJackson
import lk.kotlin.reflect.TypeInformation
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JacksonConverter(val mapper: ObjectMapper = MyJackson.mapper) : Renderer, Parser {

    override fun <T> render(type: TypeInformation, data: T, request: HttpServletRequest, response: HttpServletResponse) {
        response.outputStream.use {
            mapper.writerFor(type.toJavaType()).writeValue(it, data)
            it.flush()
        }
    }

    override fun <T> parse(type: TypeInformation, request: HttpServletRequest): T {
        return request.inputStream.use {
            mapper.readValue<T>(it, type.toJavaType())
        }
    }
}