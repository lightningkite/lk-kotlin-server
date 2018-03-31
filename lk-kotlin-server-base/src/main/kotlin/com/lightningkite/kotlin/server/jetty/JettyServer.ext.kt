package com.lightningkite.kotlin.server.jetty

import com.lightningkite.kotlin.server.base.HttpRequestHandler
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

fun HttpRequestHandler.asJettyHandler() = object : AbstractHandler() {
    override fun handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse) {
        handle(JettyHttpRequest(target, baseRequest, request, response))
    }
}