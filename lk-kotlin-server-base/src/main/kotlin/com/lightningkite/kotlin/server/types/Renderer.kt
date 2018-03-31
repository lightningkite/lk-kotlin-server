package com.lightningkite.kotlin.server.types

import com.lightningkite.kotlin.server.base.HttpRequest
import lk.kotlin.reflect.TypeInformation
import java.io.OutputStream

interface Renderer {
    fun <T> render(type: TypeInformation, data: T, httpRequest: HttpRequest, out: OutputStream)
}