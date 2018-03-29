package com.lightningkite.kotlin.server.javax

data class ContentType(
        val type: String,
        val subtype: String,
        val parameters: Map<String, String> = mapOf()
) {
    constructor(whole: String) : this(
            type = whole.substringBefore('/', "").toLowerCase(),
            subtype = whole.substringAfter('/', "").let { it.substringBefore(';', it) }.toLowerCase(),
            parameters = whole.substringAfter(';', "").split(';').associate { it.substringBefore('=', "") to it.substringAfter('=', "") }
    )

    override fun toString(): String {
        return "$type/$subtype;${parameters.entries.joinToString(";") { "${it.key}=${it.value}" }}"
    }

    fun parameterless(): String = "$type/$subtype"

    infix fun matches(other: ContentType): Boolean = type == other.type && subtype == other.subtype

    companion object {
        fun parameterless(full: String): String? = full.split(';').firstOrNull()

        val Multipart = ContentType("multipart", "form-data")
    }

    object Application {
        val Json = ContentType("application", "json")
        val Bson = ContentType("application", "bson")
        val MessagePack = ContentType("application", "vnd.msgpack")
        val FormUrlEncoded = ContentType("application", "x-www-form-urlencoded")
    }

    object Text {
        val Plain = ContentType("text", "plain")
        val Html = ContentType("text", "html")
        val Css = ContentType("text", "css")
    }

    object Image {
        val Png = ContentType("image", "png")
        val Jpeg = ContentType("image", "jpeg")
        val Gif = ContentType("image", "gif")
    }
}