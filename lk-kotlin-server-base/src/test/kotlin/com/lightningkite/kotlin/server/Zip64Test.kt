package com.lightningkite.kotlin.server

import com.lightningkite.kotlin.server.types.deflate64
import com.lightningkite.kotlin.server.types.inflate64
import org.junit.Test

class Zip64Test {
    @Test
    fun test() {
        val data = """{"a":3}"""
        val compressed = data.deflate64()
        println(compressed)
        println(compressed.inflate64())
    }
}