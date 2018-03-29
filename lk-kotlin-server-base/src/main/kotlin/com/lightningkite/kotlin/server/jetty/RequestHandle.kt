package com.lightningkite.kotlin.server.jetty

import com.lightningkite.kotlin.server.base.CentralContentTypeMap
import com.lightningkite.kotlin.server.javax.ContentType
import com.lightningkite.kotlin.server.javax.accepts
import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.typeInformation
import org.eclipse.jetty.server.Request
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RequestHandle(
        var target: String,
        var baseRequest: Request,
        var request: HttpServletRequest,
        var response: HttpServletResponse
) {
    inline fun <reified T : Any> requestAs() = requestAs<T>(typeInformation<T>())
    fun <T> requestAs(type: TypeInformation): T {
        println("Content Type: ${request.contentType}")
        val parser = CentralContentTypeMap.parsers[request.contentType]
                ?: throw IllegalArgumentException("Content type ${request.contentType} not understood.")
        return parser.parse(
                type = type,
                request = request
        )
    }

    inline fun <reified T : Any> respondWith(data: T?, code: Int = HttpServletResponse.SC_OK) = respondWith(lk.kotlin.reflect.typeInformation<T>(), data, code)
    fun <T> respondWith(type: TypeInformation, data: T, code: Int = HttpServletResponse.SC_OK) {
        val rendererType = request.accepts().firstOrNull {
            CentralContentTypeMap.renderers.containsKey(it.parameterless())
        } ?: throw IllegalArgumentException("Content types ${request.accepts().joinToString()} not understood.")
        CentralContentTypeMap.renderers[rendererType.parameterless()]!!.render(
                type = type,
                data = data,
                request = request,
                response = response
        )
        response.status = code
        response.contentType = rendererType.toString()
        baseRequest.isHandled = true
    }

    fun respondHtml(html: String) {
        response.status = 200
        response.contentType = ContentType.Text.Html.toString()
        response.outputStream.use {
            it.write(html.toByteArray(Charsets.UTF_8))
            it.flush()
        }
    }
}