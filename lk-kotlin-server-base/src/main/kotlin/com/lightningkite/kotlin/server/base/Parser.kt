package com.lightningkite.kotlin.server.base

import lk.kotlin.reflect.TypeInformation
import java.io.InputStream

interface Parser {
    fun <T> parse(options: Map<String, String>, type: TypeInformation, stream: InputStream): T
}