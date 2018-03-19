package com.lightningkite.kotlin.server.base

import com.fasterxml.jackson.databind.ObjectMapper
import lk.kotlin.jackson.MyJackson
import lk.kotlin.reflect.TypeInformation
import java.io.InputStream
import java.io.OutputStream

class JacksonConverter(val mapper: ObjectMapper = MyJackson.mapper) : Renderer, Parser {
    override fun <T> render(options: Map<String, String>, type: TypeInformation, data: T, stream: OutputStream) {
        mapper.writerFor(type.toJavaType()).writeValue(stream, data)
    }

    override fun <T> parse(options: Map<String, String>, type: TypeInformation, stream: InputStream): T {
        return mapper.readValue<T>(stream, type.toJavaType())
    }
}