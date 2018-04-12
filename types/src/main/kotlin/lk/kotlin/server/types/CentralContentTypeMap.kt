package lk.kotlin.server.types

import lk.kotlin.server.base.ContentType
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
}

