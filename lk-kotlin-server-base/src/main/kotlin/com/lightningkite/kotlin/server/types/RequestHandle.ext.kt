package com.lightningkite.kotlin.server.types

import com.lightningkite.kotlin.server.base.HttpCookie
import com.lightningkite.kotlin.server.base.HttpRequest
import com.lightningkite.kotlin.server.base.accepts
import com.lightningkite.kotlin.server.base.contentType
import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.typeInformation

inline fun <reified T : Any> HttpRequest.inputAs(): T = inputAs(typeInformation<T>())
fun <T> HttpRequest.inputAs(typeInformation: TypeInformation): T {
    val parser = CentralContentTypeMap.parsers[contentType()?.parameterless()]
            ?: throw IllegalArgumentException("Content type ${contentType()} not understood.")
    return parser.parse(
            type = typeInformation,
            httpRequest = this
    )
}

inline fun <reified T : Any> HttpRequest.respondWith(
        code: Int = 200,
        headers: Map<String, List<String>> = mapOf(),
        addCookies: List<HttpCookie> = listOf(),
        output: T
) = respondWith(
        code = code,
        headers = headers,
        addCookies = addCookies,
        typeInformation = typeInformation<T>(),
        output = output
)

fun <T> HttpRequest.respondWith(
        code: Int = 200,
        headers: Map<String, List<String>> = mapOf(),
        addCookies: List<HttpCookie> = listOf(),
        typeInformation: TypeInformation,
        output: T
) {
    val rendererType = this.accepts().firstOrNull {
        CentralContentTypeMap.renderers.containsKey(it.parameterless())
    } ?: throw IllegalArgumentException("Content types ${accepts().joinToString()} not understood.")
    respond(
            code = code,
            headers = headers,
            addCookies = addCookies,
            contentType = rendererType,
            output = {
                CentralContentTypeMap.renderers[rendererType.parameterless()]!!.render(
                        type = typeInformation,
                        data = output,
                        httpRequest = this,
                        out = it
                )
            }
    )
}