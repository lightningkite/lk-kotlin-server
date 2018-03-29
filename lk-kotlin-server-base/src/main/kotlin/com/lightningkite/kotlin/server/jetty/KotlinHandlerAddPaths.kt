package com.lightningkite.kotlin.server.jetty

interface KotlinHandlerAddPaths {
    fun get(url: String, handler: KHandler)
    fun post(url: String, handler: KHandler)
    fun put(url: String, handler: KHandler)
    fun patch(url: String, handler: KHandler)
    fun delete(url: String, handler: KHandler)
    fun path(prepend: String, setup: KotlinHandlerAddPaths.() -> Unit) = object : KotlinHandlerAddPaths {
        override fun get(url: String, handler: KHandler) {
            this@KotlinHandlerAddPaths.get("$prepend/$url", handler)
        }

        override fun post(url: String, handler: KHandler) {
            this@KotlinHandlerAddPaths.post("$prepend/$url", handler)
        }

        override fun put(url: String, handler: KHandler) {
            this@KotlinHandlerAddPaths.put("$prepend/$url", handler)
        }

        override fun patch(url: String, handler: KHandler) {
            this@KotlinHandlerAddPaths.patch("$prepend/$url", handler)
        }

        override fun delete(url: String, handler: KHandler) {
            this@KotlinHandlerAddPaths.delete("$prepend/$url", handler)
        }
    }.apply(setup)
}