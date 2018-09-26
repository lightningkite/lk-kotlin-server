package lk.kotlin.server.types

import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.typeInformation
import lk.kotlin.server.base.*
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.*

inline fun <reified T : Any> HttpRequest.inputAs(context: Context = mutableMapOf(), user: Any? = null): T = inputAs(context, user, typeInformation<T>())
fun <T> HttpRequest.inputAs(
        context: Context = mutableMapOf(),
        user: Any? = null,
        typeInformation: TypeInformation
): T {
    return CentralContentTypeMap.parse(
            request = this,
            context = context,
            user = user,
            typeInformation = typeInformation
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

fun <T : Any> HttpRequest.respondWithImplied(
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
        typeInformation = TypeInformation(kclass = output.javaClass.kotlin),
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
) = CentralContentTypeMap.render(
        request = this,
        context = context,
        user = user,
        code = code,
        headers = headers,
        addCookies = addCookies,
        typeInformation = typeInformation,
        output = output
)