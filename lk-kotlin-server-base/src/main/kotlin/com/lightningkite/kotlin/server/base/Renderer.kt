package com.lightningkite.kotlin.server.base

import lk.kotlin.reflect.TypeInformation
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

interface Renderer {
    fun <T> render(type: TypeInformation, data: T, request: HttpServletRequest, response: HttpServletResponse)
}