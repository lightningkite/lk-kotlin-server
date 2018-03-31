package com.lightningkite.kotlin.server.jetty

import com.lightningkite.kotlin.server.base.ContentType
import com.lightningkite.kotlin.server.base.HttpCookie
import com.lightningkite.kotlin.server.base.HttpRequest
import org.eclipse.jetty.server.Request
import java.io.InputStream
import java.io.OutputStream
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JettyHttpRequest(
        path: String,
        var baseRequest: Request,
        var request: HttpServletRequest,
        var response: HttpServletResponse
) : HttpRequest {
    override val path: String = path.let {
        if (it.startsWith('/'))
            it.drop(1)
        else it
    }
    override val method: String = baseRequest.method.toLowerCase()
    override val headers: Map<String, List<String>> = request.headerNames.asSequence().associate { it.toLowerCase() to request.getHeaders(it).toList() }
    override val parameters: Map<String, List<String>> = request.parameterNames.asSequence().associate { it.toLowerCase() to request.getParameterValues(it).toList() }
    override val cookies: Map<String, HttpCookie> by lazy {
        request.cookies.associate {
            it.name.toLowerCase() to HttpCookie(
                    name = it.name,
                    value = it.value,
                    comment = it.comment,
                    domain = it.domain,
                    maxAge = it.maxAge,
                    path = it.path,
                    secure = it.secure,
                    version = it.version,
                    isHttpOnly = it.isHttpOnly
            )
        }
    }
    override val input: InputStream = request.inputStream

    private fun respondDefaults(
            code: Int,
            headers: Map<String, List<String>>,
            addCookies: List<HttpCookie>,
            contentType: ContentType
    ) {
        response.status = code
        response.contentType = contentType.toString()
        for ((key, subheaders) in headers) {
            for (subheader in subheaders) {
                response.addHeader(key, subheader)
            }
        }
        for (cookie in addCookies) {
            response.addCookie(Cookie(
                    cookie.name, cookie.value
            ).apply {
                comment = cookie.comment
                domain = cookie.domain
                maxAge = cookie.maxAge
                path = cookie.path
                secure = cookie.secure
                version = cookie.version
                isHttpOnly = cookie.isHttpOnly
            })
        }
    }

    override fun respond(
            code: Int,
            headers: Map<String, List<String>>,
            addCookies: List<HttpCookie>,
            contentType: ContentType,
            output: (OutputStream) -> Unit
    ) {
        respondDefaults(code = code, headers = headers, addCookies = addCookies, contentType = contentType)
        response.outputStream.use {
            output.invoke(it)
            it.flush()
        }
        baseRequest.isHandled = true
    }

    override fun respond(
            code: Int,
            headers: Map<String, List<String>>,
            addCookies: List<HttpCookie>,
            contentType: ContentType,
            data: ByteArray
    ) {
        respondDefaults(code = code, headers = headers, addCookies = addCookies, contentType = contentType)
        response.setContentLength(data.size)
        response.outputStream.use {
            it.write(data)
            it.flush()
        }
        baseRequest.isHandled = true
    }

    override fun redirect(to: String) {
        response.sendRedirect(to)
    }
}