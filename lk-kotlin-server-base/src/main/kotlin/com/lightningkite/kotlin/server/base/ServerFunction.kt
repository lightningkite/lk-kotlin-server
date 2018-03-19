package com.lightningkite.kotlin.server.base

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
interface ServerFunction<T> {
    fun invoke(transaction: Transaction): T
}