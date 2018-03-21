package com.lightningkite.kotlin.server.base

import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.reflectAsmConstruct
import java.io.InputStream
import java.io.OutputStream

object CentralContentTypeMap {
    val parsers = HashMap<String, Parser>()
    val renderers = HashMap<String, Renderer>()
    var defaultRender = ContentType.Application.Json.parameterless()

    fun <T> parse(contentType: String, options: Map<String, String>, type: TypeInformation, stream: InputStream) = parsers[contentType]!!.parse<T>(options, type, stream)

    fun <T> render(contentTypes: List<ContentType>, options: Map<String, String>, type: TypeInformation, data: T, stream: OutputStream) {
        for (contentType in contentTypes) {
            val renderer = renderers[contentType.parameterless()] ?: continue
            renderer.render<T>(options, type, data, stream)
            return
        }
        throw IllegalArgumentException("No renderer for any of the accepted types, ${contentTypes.joinToString { it.parameterless() }}")
    }

    init {
        val json = JacksonConverter()
        parsers[ContentType.Application.Json.parameterless()] = json
        renderers[ContentType.Application.Json.parameterless()] = json

        val defaultText = DefaultTextConverter()
        parsers[ContentType.Text.Plain.parameterless()] = defaultText
        renderers[ContentType.Text.Plain.parameterless()] = defaultText

        val defaultHtml = DefaultHtmlConverter()
        parsers[ContentType.Text.Html.parameterless()] = defaultHtml
        renderers[ContentType.Text.Html.parameterless()] = defaultHtml
    }
}

class DefaultTextConverter : Parser, Renderer {
    //TODO
    override fun <T> parse(options: Map<String, String>, type: TypeInformation, stream: InputStream): T = type.kclass.reflectAsmConstruct() as T

    //TODO more fully
    override fun <T> render(options: Map<String, String>, type: TypeInformation, data: T, stream: OutputStream) {
        stream.write(data.toString().toByteArray())
    }

}

class DefaultHtmlConverter : Parser, Renderer {
    //TODO
    override fun <T> parse(options: Map<String, String>, type: TypeInformation, stream: InputStream): T = type.kclass.reflectAsmConstruct() as T

    //TODO more fully
    override fun <T> render(options: Map<String, String>, type: TypeInformation, data: T, stream: OutputStream) {
        stream.write(data.toString().toByteArray())
    }

}