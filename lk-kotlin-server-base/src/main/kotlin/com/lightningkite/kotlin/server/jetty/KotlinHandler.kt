package com.lightningkite.kotlin.server.jetty

import lk.kotlin.jvm.utils.exception.stackTraceString
import org.eclipse.jetty.http.HttpMethod
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class KotlinHandler() : KotlinHandlerAddPaths {
    val map = HashMap<String, KHandler>()

    val handler = object : AbstractHandler() {
        override fun handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse) {
            val handle = RequestHandle(target, baseRequest, request, response)
            try {
                val lookup = request.method.plus("|").plus(target.toLowerCase()).let {
                    if (it.endsWith('/'))
                        it.dropLast(1)
                    else it
                }
                map[lookup]?.invoke(handle)
            } catch (e: Exception) {
                //TODO: Log better
                e.printStackTrace()
                val error = e.stackTraceString()
                handle.respondHtml("<!DOCTYPE html><html><head><meta charset=\"utf-8\"/><link rel=\"stylesheet\" href=\"/style.css\"/></head><body><h1>Server Error</h1><code>$error</code></body></html>")
            }
        }
    }

    override fun get(url: String, handler: KHandler) {
        map[HttpMethod.GET.asString() + "|/" + url.toLowerCase()] = handler
    }

    override fun post(url: String, handler: KHandler) {
        map[HttpMethod.POST.asString() + "|/" + url.toLowerCase()] = handler
    }

    override fun put(url: String, handler: KHandler) {
        map[HttpMethod.PUT.asString() + "|/" + url.toLowerCase()] = handler
    }

    override fun patch(url: String, handler: KHandler) {
        map["PATCH|/$url"] = handler
    }

    override fun delete(url: String, handler: KHandler) {
        map[HttpMethod.DELETE.asString() + "|/" + url.toLowerCase()] = handler
    }
}