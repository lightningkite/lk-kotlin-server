package com.lightningkite.kotlin.server.base

import com.lightningkite.kotlin.server.javax.ContentType
import java.util.*

object CentralContentTypeMap {
    val parsers = HashMap<String, Parser>()
    val renderers = HashMap<String, Renderer>()
    var defaultRender = ContentType.Application.Json.parameterless()
    val errorMap = HashMap<Class<*>, (Any) -> HttpError>()

    val json = JacksonConverter()
    val text = DefaultTextRenderer()
    val html = DefaultHtmlConverter()

    init {
        parsers[ContentType.Application.Json.parameterless()] = json
        renderers[ContentType.Application.Json.parameterless()] = json

        renderers[ContentType.Text.Plain.parameterless()] = text

        parsers[ContentType.Application.FormUrlEncoded.parameterless()] = html
        parsers[ContentType.Multipart.parameterless()] = html
        renderers[ContentType.Text.Html.parameterless()] = html
    }
}

