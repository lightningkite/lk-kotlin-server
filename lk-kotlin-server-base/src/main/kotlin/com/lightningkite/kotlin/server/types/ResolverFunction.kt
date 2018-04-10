package com.lightningkite.kotlin.server.types

interface ResolverFunction<TYPE> : ServerFunction<TYPE> {
    var id: String
}