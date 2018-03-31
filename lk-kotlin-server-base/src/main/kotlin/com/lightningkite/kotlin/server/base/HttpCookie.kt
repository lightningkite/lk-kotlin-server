package com.lightningkite.kotlin.server.base

class HttpCookie(
        var name: String,
        var value: String,
        var comment: String = "",
        var domain: String = "",
        var maxAge: Int = -1,
        var path: String = "",
        var secure: Boolean = false,
        var version: Int = 0,
        var isHttpOnly: Boolean = false
) {
}