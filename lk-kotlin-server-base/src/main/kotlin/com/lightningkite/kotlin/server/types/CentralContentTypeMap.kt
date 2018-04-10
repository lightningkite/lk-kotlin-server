package com.lightningkite.kotlin.server.types

import com.lightningkite.kotlin.server.base.ContentType
import java.util.*

object CentralContentTypeMap {
    val parsers = HashMap<String, Parser>()
    val renderers = HashMap<String, Renderer>()
    var defaultRender = ContentType.Application.Json.parameterless()

    val json = JacksonConverter()
    val html = HtmlConverter()

    init {
        parsers[ContentType.Application.Json.parameterless()] = json
        renderers[ContentType.Application.Json.parameterless()] = json

        parsers[ContentType.Application.FormUrlEncoded.parameterless()] = html
        parsers[ContentType.Multipart.parameterless()] = html
        renderers[ContentType.Text.Html.parameterless()] = html
    }
}

