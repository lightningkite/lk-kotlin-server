package lk.kotlin.server.base

import java.io.InputStream
import java.io.OutputStream

interface HttpRequest {
    val path: String
    val method: String
    val headers: Map<String, List<String>>
    val parameters: Map<String, List<String>>
    val cookies: Map<String, HttpCookie>
    val input: InputStream

    fun respond(
            code: Int,
            headers: Map<String, List<String>> = mapOf(),
            addCookies: List<HttpCookie> = listOf(),
            contentType: ContentType,
            output: (OutputStream) -> Unit
    )

    fun respond(
            code: Int,
            headers: Map<String, List<String>> = mapOf(),
            addCookies: List<HttpCookie> = listOf(),
            contentType: ContentType,
            data: ByteArray
    )

    fun redirect(to: String)
}

