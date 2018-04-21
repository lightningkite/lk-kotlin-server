package lk.kotlin.server.types

import lk.kotlin.jvm.utils.stream.toByteArray
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


fun String.deflate64(): String = Base64.getUrlEncoder().encodeToString(
        ByteArrayOutputStream().use { outputStream ->
            GZIPOutputStream(outputStream).use {
                it.write(this@deflate64.toByteArray(Charsets.UTF_8))
            }
            outputStream.toByteArray()
        }
)

fun String.inflate64(): String = Base64.getUrlDecoder().decode(this).let {
    GZIPInputStream(ByteArrayInputStream(it)).toByteArray().toString(Charsets.UTF_8)
}