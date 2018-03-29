package com.lightningkite.kotlin.server.javax

import javax.servlet.http.HttpServletRequest


fun HttpServletRequest.headers(): Map<String, String> = headerNames.asSequence().associate { it to getHeader(it) }
fun HttpServletRequest.contentType(): ContentType = ContentType(contentType)
fun HttpServletRequest.accepts(): List<ContentType> {
    return getHeaders("Accept").asSequence().flatMap { it.split(',').asSequence().map { ContentType(it) } }.toList()
}