package lk.kotlin.server.base

class HttpCookie(
        var name: String,
        var value: String,
        var comment: String? = null,
        var domain: String? = null,
        var maxAge: Int = -1,
        var path: String? = null,
        var secure: Boolean = false,
        var version: Int = 0,
        var isHttpOnly: Boolean = false
) {
}