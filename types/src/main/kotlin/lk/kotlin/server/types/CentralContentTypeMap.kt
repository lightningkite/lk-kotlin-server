package lk.kotlin.server.types

import lk.kotlin.reflect.TypeInformation
import lk.kotlin.server.base.*
import java.util.*

object CentralContentTypeMap {
    val parsers = HashMap<String, Parser>()
    val renderers = HashMap<String, Renderer>()
    var defaultRender = ContentType.Application.Json.parameterless()

    val json = JacksonConverter()

    init {
        parsers[ContentType.Application.Json.parameterless()] = json
        renderers[ContentType.Application.Json.parameterless()] = json
    }

    fun <T> parse(
            request: HttpRequest,
            typeInformation: TypeInformation,
            context: Context,
            user: Any?
    ): T {
        val contentType = request.contentType()?.parameterless()
                ?: ContentType.Application.FormUrlEncoded.parameterless()
        val parser = this.parsers[contentType]
                ?: throw IllegalArgumentException("Content type ${request.contentType()} not understood.")
        return parser.parse(
                type = typeInformation,
                httpRequest = request,
                getTransaction = { Transaction(context, user) }
        )
    }

    fun <T> render(
            request: HttpRequest,
            context: Context = mutableMapOf(),
            user: Any? = null,
            code: Int = 200,
            headers: Map<String, List<String>> = mapOf(),
            addCookies: List<HttpCookie> = listOf(),
            typeInformation: TypeInformation,
            output: T
    ) {
        val rendererType = request.header("x-override-accept")?.let { ContentType(it) }
                ?: request.parameter("x-override-accept")?.let { ContentType(it) }
                ?: request.accepts().firstOrNull {
                    CentralContentTypeMap.renderers.containsKey(it.parameterless())
                }
                ?: ContentType.Application.Json

        request.respond(
                code = code,
                headers = headers,
                addCookies = addCookies,
                contentType = rendererType,
                output = {
                    this.renderers[rendererType.parameterless()]!!.render(
                            type = typeInformation,
                            data = output,
                            httpRequest = request,
                            out = it,
                            getTransaction = { Transaction(context, user) }
                    )
                }
        )
    }
}

