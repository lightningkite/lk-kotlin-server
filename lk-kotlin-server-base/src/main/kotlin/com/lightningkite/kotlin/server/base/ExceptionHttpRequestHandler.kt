package com.lightningkite.kotlin.server.base

class ExceptionHttpRequestHandler() : HttpRequestHandler() {
    override fun handle(request: HttpRequest): Boolean {
        val lookup = request.method.plus("|").plus(request.path).let {
            if (it.endsWith('/'))
                it.dropLast(1)
            else it
        }
        val callback = map[lookup] ?: return false
        try {
            callback.invoke(request)
        } catch (e: HttpException) {
            request.respondHtml(
                    code = e.httpError.code
            ) {
                append("<!DOCTYPE html>")
                append("<html>")
                append("<head>")
                append("<meta charset=\"utf-8\"/>")
                append("<link rel=\"stylesheet\" href=\"/style.css\"/>")
                append("</head>")
                append("<body>")
                append("<h1>Error (${e.httpError.code})</h1>")
                append("<p>${e.httpError.message.filter { it != '<' && it != '>' }}</p>")
                append("</body>")
                append("</html>")
            }
        }
        return true
    }

}