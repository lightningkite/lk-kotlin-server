package com.lightningkite.kotlin.server.types

import com.lightningkite.kotlin.server.base.*
import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.typeInformation

inline fun <reified T : Any> HttpRequest.inputAs(context: Context = mutableMapOf(), user: Any? = null): T = inputAs(context, user, typeInformation<T>())
fun <T> HttpRequest.inputAs(context: Context = mutableMapOf(), user: Any? = null, typeInformation: TypeInformation): T {
    val parser = CentralContentTypeMap.parsers[contentType()?.parameterless()]
            ?: throw IllegalArgumentException("Content type ${contentType()} not understood.")
    return parser.parse(
            type = typeInformation,
            httpRequest = this,
            getTransaction = { Transaction(context, user) }
    )
}

inline fun <reified T : Any> HttpRequest.respondWith(
        context: Context = mutableMapOf(),
        user: Any? = null,
        code: Int = 200,
        headers: Map<String, List<String>> = mapOf(),
        addCookies: List<HttpCookie> = listOf(),
        output: T
) = respondWith(
        context = context,
        user = user,
        code = code,
        headers = headers,
        addCookies = addCookies,
        typeInformation = typeInformation<T>(),
        output = output
)

fun <T> HttpRequest.respondWith(
        context: Context = mutableMapOf(),
        user: Any? = null,
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
                        out = it,
                        getTransaction = { Transaction(context, user) }
                )
            }
    )
}