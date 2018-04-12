package lk.kotlin.server.types

import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.annotations.friendlyName
import lk.kotlin.reflect.fastMutableProperties
import lk.kotlin.server.base.*
import lk.kotlin.server.types.common.ServerFunction
import lk.kotlin.server.types.log.HistoricalServerFunction
import lk.kotlin.server.types.log.ServerFunctionLogger
import kotlin.reflect.KClass

fun KClass<*>.urlName() = friendlyName.toLowerCase().replace(' ', '-').replace(Regex("-*"), "")

fun HttpRequestHandlerBuilder.rpc(
        url: String,
        context: Context,
        getUser: (HttpRequest) -> Any? = { null },
        logger: ServerFunctionLogger? = null,
        functionList: List<TypeInformation>
) {
    for (functionType in functionList) {
        val funcUrl = url + "/" + functionType.kclass.urlName()
        println("funcUrl $funcUrl")

        if (functionType.kclass.fastMutableProperties.isEmpty()) {
            get(funcUrl) {
                val user = getUser(this)
                val request = inputAs<ServerFunction<*>>(context = context, user = user, typeInformation = functionType)
                val result = Transaction(context, user).use {
                    request.invoke(it)
                }
                logger?.log(HistoricalServerFunction(userIdentifier = user, call = request, result = result))
                @Suppress("UNCHECKED_CAST")
                respondWith(context = context, user = user, typeInformation = (functionType.kclass as KClass<out ServerFunction<*>>).returnType, output = result)
            }
        } else {
            get(funcUrl) {
                val user = getUser(this)
                val request = inputAs<ServerFunction<*>>(context = context, user = user, typeInformation = functionType)
                respondWith(context = context, user = null, typeInformation = functionType, output = request)
            }
        }
        post(funcUrl) {
            val user = getUser(this)
            val request = inputAs<ServerFunction<*>>(context = context, user = user, typeInformation = functionType)
            val result = Transaction(context, user).use {
                request.invoke(it)
            }
            logger?.log(HistoricalServerFunction(userIdentifier = user, call = request, result = result))
            @Suppress("UNCHECKED_CAST")
            respondWith(context = context, user = user, typeInformation = (functionType.kclass as KClass<out ServerFunction<*>>).returnType, output = result)
        }
    }
    get("$url/index") {
        respondHtml {
            append("<!DOCTYPE html>")
            append("<html>")
            append("<head>")
            append("<meta charset=\"utf-8\"/>")
            append("<link rel=\"stylesheet\" href=\"/style.css\"/>")
            append("</head>")
            append("<body>")
            append("<h1>Available Functions</h1>")
            append("<p>You are logged in as: ${getUser(this@get)}</p>")
            append("<ul>")
            for (it in functionList) {
                append("<li><a href=\"${it.kclass.urlName()}\">${it.kclass.friendlyName}</a></li>")
            }
            append("</ul>")
            append("</body>")
            append("</html>")
        }
    }
    post(url) {
        val user = getUser(this)
        val request = inputAs<ServerFunction<*>>(context = context, user = user)
        val result = Transaction(context, user).use {
            request.invoke(it)
        }
        logger?.log(HistoricalServerFunction(userIdentifier = user, call = request, result = result))
        respondWith(context = context, user = user, typeInformation = request.javaClass.kotlin.returnType, output = result)
    }
}