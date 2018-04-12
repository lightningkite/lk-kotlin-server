package lk.kotlin.server.base

interface HttpRequestHandlerBuilder {
    fun method(method: String, url: String, handler: HttpRequest.() -> Unit)
    fun method(method: HttpMethod, url: String, handler: HttpRequest.() -> Unit) = method(method.name, url, handler)
    fun get(url: String, handler: HttpRequest.() -> Unit) = method(HttpMethod.GET, url, handler)
    fun post(url: String, handler: HttpRequest.() -> Unit) = method(HttpMethod.POST, url, handler)
    fun put(url: String, handler: HttpRequest.() -> Unit) = method(HttpMethod.PUT, url, handler)
    fun patch(url: String, handler: HttpRequest.() -> Unit) = method(HttpMethod.PATCH, url, handler)
    fun delete(url: String, handler: HttpRequest.() -> Unit) = method(HttpMethod.DELETE, url, handler)
    fun path(prepend: String, setup: HttpRequestHandlerBuilder.() -> Unit) = object : HttpRequestHandlerBuilder {
        override fun method(method: String, url: String, handler: HttpRequest.() -> Unit) = this@HttpRequestHandlerBuilder.method(method, "$prepend/$url", handler)
    }.apply(setup)
}