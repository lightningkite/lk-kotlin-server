package com.lightningkite.kotlin.server.types

import com.lightningkite.kotlin.server.base.*
import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.fastMutableProperties
import lk.kotlin.reflect.reflectAsmConstruct
import kotlin.reflect.KClass

fun HttpRequestHandlerBuilder.rpc(url: String, context: Context, functionList: List<TypeInformation>) {
    for (functionType in functionList) {
        val funcUrl = url + "/" + functionType.kclass.urlName
        println("funcUrl $funcUrl")

        if (functionType.kclass.fastMutableProperties.isEmpty()) {
            get(funcUrl) {
                val request = functionType.kclass.reflectAsmConstruct() as ServerFunction<*>
                val result = Transaction(context).use {
                    request.invoke(it)
                }
                @Suppress("UNCHECKED_CAST")
                respondWith(typeInformation = (functionType.kclass as KClass<out ServerFunction<*>>).returnType, output = result)
            }
        } else {
            get(funcUrl) {
                respondWith(typeInformation = functionType, output = functionType.kclass.reflectAsmConstruct())
            }
        }
        post(funcUrl) {
            val request = inputAs<ServerFunction<*>>(functionType)
            val result = Transaction(context).use {
                request.invoke(it)
            }
            @Suppress("UNCHECKED_CAST")
            respondWith(typeInformation = (functionType.kclass as KClass<out ServerFunction<*>>).returnType, output = result)
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
            append("<ul>")
            for (it in functionList) {
                append("<li><a href=\"${it.kclass.urlName}\">${it.kclass.friendlyName}</a></li>")
            }
            append("</ul>")
            append("</body>")
            append("</html>")
        }
    }
    post(url) {
        val request = inputAs<ServerFunction<*>>()
        val result = Transaction(context).use {
            request.invoke(it)
        }
        respondWith(typeInformation = request.javaClass.kotlin.returnType, output = result)
    }
}