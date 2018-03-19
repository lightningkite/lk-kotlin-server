package com.lightningkite.kotlin.server.base

import lk.kotlin.reflect.TypeInformation
import java.io.OutputStream

interface Renderer {
    fun <T> render(options: Map<String, String>, type: TypeInformation, data: T, stream: OutputStream)
}