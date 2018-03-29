package com.lightningkite.kotlin.server.base

import lk.kotlin.reflect.TypeInformation
import javax.servlet.http.HttpServletRequest

interface Parser {
    fun <T> parse(type: TypeInformation, request: HttpServletRequest): T
}