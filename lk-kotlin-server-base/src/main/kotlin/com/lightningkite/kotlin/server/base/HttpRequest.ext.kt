package com.lightningkite.kotlin.server.base

import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter

fun HttpRequest.parameters(key: String): List<String> = parameters[key.toLowerCase()] ?: listOf()
fun HttpRequest.headers(key: String): List<String> = headers[key.toLowerCase()] ?: listOf()
fun HttpRequest.parameter(key: String): String? = parameters[key.toLowerCase()]?.firstOrNull()
fun HttpRequest.header(key: String): String? = headers[key.toLowerCase()]?.firstOrNull()
fun HttpRequest.cookie(key: String): HttpCookie? = cookies[key.toLowerCase()]

fun HttpRequest.accepts(): List<ContentType> =
        headers("Accept").asSequence().flatMap { it.split(',').asSequence().map { ContentType(it) } }.toList()

fun HttpRequest.contentType(): ContentType? = headers("content-type").firstOrNull()?.let { ContentType(it) }

fun HttpRequest.respondHtml(
        code: Int = 200,
        headers: Map<String, List<String>> = mapOf(),
        addCookies: List<HttpCookie> = listOf(),
        html: String
) = respond(
        code = code,
        headers = headers,
        addCookies = addCookies,
        contentType = ContentType.Text.Html,
        data = html.toByteArray()
)

inline fun HttpRequest.respondHtml(
        code: Int = 200,
        headers: Map<String, List<String>> = mapOf(),
        addCookies: List<HttpCookie> = listOf(),
        html: Appendable.() -> Unit
) = respond(
        code = code,
        headers = headers,
        addCookies = addCookies,
        contentType = ContentType.Text.Html,
        data = ByteArrayOutputStream().also { OutputStreamWriter(it).apply(html).flush() }.toByteArray()
)