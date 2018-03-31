package com.lightningkite.kotlin.server.types

import com.lightningkite.kotlin.server.base.HttpRequest
import lk.kotlin.reflect.TypeInformation
import java.io.OutputStream
import java.io.OutputStreamWriter

class DefaultTextRenderer : Renderer {
    override fun <T> render(type: TypeInformation, data: T, httpRequest: HttpRequest, out: OutputStream) {
        OutputStreamWriter(out).apply {
            println(data.toString())
        }
    }

}