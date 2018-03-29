package com.lightningkite.kotlin.server.jetty

import com.lightningkite.kotlin.server.base.*
import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.fastMutableProperties
import lk.kotlin.reflect.reflectAsmConstruct
import kotlin.reflect.KClass

fun KotlinHandlerAddPaths.rpc(url: String, context: Context, functionList: List<TypeInformation>) {
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
                respondWith((functionType.kclass as KClass<out ServerFunction<*>>).returnType, result)
            }
        } else {
            get(funcUrl) {
                respondWith(functionType, functionType.kclass.reflectAsmConstruct())
            }
        }
        post(funcUrl) {
            val request = requestAs<ServerFunction<*>>(functionType)
            val result = Transaction(context).use {
                request.invoke(it)
            }
            @Suppress("UNCHECKED_CAST")
            respondWith((functionType.kclass as KClass<out ServerFunction<*>>).returnType, result)
        }
    }
    get("$url/index") {
        val list = functionList.joinToString("", "<ul>", "</ul>") {
            "<li><a href=\"${it.kclass.urlName}\">${it.kclass.friendlyName}</a></li>"
        }
        respondHtml("""<!DOCTYPE html><html><head><meta charset="utf-8"/><link rel="stylesheet" href="/style.css"/></head><body><h1>Available Functions</h1>$list</body></html>""")
    }
    post(url) {
        val request = requestAs<ServerFunction<*>>()
        val result = Transaction(context).use {
            request.invoke(it)
        }
        respondWith(request.javaClass.kotlin.returnType, result)
    }
}