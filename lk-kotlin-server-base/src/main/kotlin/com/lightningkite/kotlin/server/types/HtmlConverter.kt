package com.lightningkite.kotlin.server.types

import com.lightningkite.kotlin.server.base.*
import com.lightningkite.kotlin.server.types.annotations.getFromId
import com.lightningkite.kotlin.server.types.annotations.getPrimaryKeyValue
import com.lightningkite.kotlin.server.types.annotations.query
import lk.kotlin.jackson.MyJackson
import lk.kotlin.jackson.jacksonToString
import lk.kotlin.jvm.utils.exception.stackTraceString
import lk.kotlin.reflect.*
import lk.kotlin.reflect.annotations.estimatedLength
import lk.kotlin.reflect.annotations.friendlyName
import lk.kotlin.reflect.annotations.hidden
import lk.kotlin.reflect.annotations.password
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.URLEncoder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.createInstance

class HtmlConverter : Parser, Renderer {
    var pageWrapperPrepend = """<!DOCTYPE html><html><head><meta charset="utf-8"/><link rel="stylesheet" href="/style.css"/></head><body>"""
    var pageWrapperAppend = """</body></html>"""

    data class Context(
            val htmlConverter: HtmlConverter,
            val httpRequest: HttpRequest,
            val getTransaction: () -> Transaction
    )

    data class LevelInfo<out T>(
            val context: Context,
            val depth: Int,
            val callDepth: Int,
            val type: TypeInformation,
            val property: KProperty1<*, *>?,
            val name: String,
            val data: T?
    )

    interface HtmlSubConverter<T> {
        fun render(info: LevelInfo<T>, to: Appendable)
        fun renderForm(info: LevelInfo<T>, to: Appendable)
        fun parse(info: LevelInfo<T>): T?
        fun renderParameters(info: LevelInfo<T>, to: MutableMap<String, String>)
        fun renderSafe(info: LevelInfo<T>, to: Appendable) = try {
            render(info, to)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        fun renderFormSafe(info: LevelInfo<T>, to: Appendable) = try {
            renderForm(info, to)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        fun parseSafe(info: LevelInfo<T>): T? = try {
            parse(info)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        fun renderParametersSafe(info: LevelInfo<T>, to: MutableMap<String, String>) = try {
            renderParameters(info, to)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val subGenerators = ArrayList<(KClass<*>) -> HtmlSubConverter<*>?>()
    private val subs = HashMap<KClass<*>, HtmlSubConverter<*>>()
    fun <T : Any> register(kclass: KClass<T>, subConverter: HtmlSubConverter<T>) {
        subs[kclass] = subConverter
    }

    fun <T : Any> retrieve(kclass: KClass<T>): HtmlSubConverter<T> = subs.getOrPut(kclass) {
        subGenerators.asSequence().mapNotNull { it.invoke(kclass) }.firstOrNull()
                ?: throw IllegalArgumentException("Sub for ${kclass.qualifiedName} not found!")
    } as HtmlSubConverter<T>

    fun retrieveAny(kclass: KClass<*>): HtmlSubConverter<Any> = retrieve(kclass) as HtmlSubConverter<Any>

    var maxCallDepth = 0

    override fun <T> parse(type: TypeInformation, httpRequest: HttpRequest, getTransaction: () -> Transaction): T {
        val context = Context(this, httpRequest, getTransaction)
        val info = LevelInfo(context, 0, 0, type, null, "value", null)
        return retrieve(type.kclass).parseSafe(info) as T
    }

    override fun <T> render(type: TypeInformation, data: T, httpRequest: HttpRequest, getTransaction: () -> Transaction, out: OutputStream) {
        val renderer = retrieve(type.kclass) as HtmlSubConverter<Any?>
        val context = Context(this, httpRequest, getTransaction)
        val info = LevelInfo(context, 0, 0, type, null, "value", data)
        OutputStreamWriter(out).use {
            it.append(pageWrapperPrepend)
            renderer.renderSafe(info, it)
            it.append(pageWrapperAppend)
        }
    }

//    class Context(
//            val htmlConverter: HtmlConverter,
//            val httpRequest: HttpRequest,
//            val getTransaction:()-> Transaction
//    ){
//
//        fun <T> subparse(
//                type: TypeInformation,
//                httpRequest: HttpRequest,
//                property: KProperty1<*, *>?,
//                field: String
//        ): T? {
//            val kclass = htmlConverter.subParserers.keys.closest(type.kclass)
//            val parser = htmlConverter.subParserers[kclass]!! as HtmlSubParser<T>
//            return parser.invoke(this, type, httpRequest, null, field)
//        }
//
//        fun <T> subrender(
//                type: TypeInformation,
//                httpRequest: HttpRequest,
//                property: KProperty1<*, *>?,
//                data: T?,
//                to: Appendable
//        ) {
//            val kclass = htmlConverter.subRenderers.keys.closest(type.kclass)
//            val renderer = htmlConverter.subRenderers[kclass]!! as HtmlSubRenderer<T>
//            return renderer.invoke(this, type, httpRequest, property, data, to)
//        }
//
//        fun <T> subrenderForm(
//                type: TypeInformation,
//                httpRequest: HttpRequest,
//                property: KProperty1<*, *>?,
//                name: String,
//                data: T?,
//                to: Appendable
//        ) {
//            val kclass = htmlConverter.subFormRenderers.keys.closest(type.kclass)
//            val renderer = htmlConverter.subFormRenderers[kclass]!! as HtmlFormRenderer<T>
//            return renderer.invoke(this, type, httpRequest, property, name, data, to)
//        }
//    }


    //Some defaults

    inline fun <T : Number> numberConverter(crossinline stringToNum: (String) -> T?) = object : HtmlSubConverter<T> {
        override fun render(info: LevelInfo<T>, to: Appendable) {
            to.append(info.data.toString())
        }

        override fun renderForm(info: LevelInfo<T>, to: Appendable) {
            to.append("""<input type="number" name="${info.name}" value="${info.data}"/>""")
        }

        override fun parse(info: LevelInfo<T>): T? {
            return info.context.httpRequest.parameter(info.name)?.let(stringToNum)
        }

        override fun renderParameters(info: LevelInfo<T>, to: MutableMap<String, String>) {
            info.data?.toString()?.let { to[info.name] = it }
        }
    }

    init {
        register(Byte::class, numberConverter { it.toByteOrNull() })
        register(Short::class, numberConverter { it.toShortOrNull() })
        register(Int::class, numberConverter { it.toIntOrNull() })
        register(Long::class, numberConverter { it.toLongOrNull() })
        register(Float::class, numberConverter { it.toFloatOrNull() })
        register(Double::class, numberConverter { it.toDoubleOrNull() })

        register(Boolean::class, object : HtmlSubConverter<Boolean> {
            override fun render(info: LevelInfo<Boolean>, to: Appendable) {
                when (info.data) {
                    true -> to.append('☑')
                    false -> to.append('☐')
                    null -> to.append('?')
                }
            }

            override fun renderForm(info: LevelInfo<Boolean>, to: Appendable) {
                to.append("""<input type="checkbox" name="${info.name}" value="true"/>""")
            }

            override fun parse(info: LevelInfo<Boolean>): Boolean? {
                return info.context.httpRequest.parameter(info.name)?.toBoolean()
            }

            override fun renderParameters(info: LevelInfo<Boolean>, to: MutableMap<String, String>) {
                info.data?.toString()?.let { to[info.name] = it }
            }
        })

        register(String::class, object : HtmlSubConverter<String> {
            override fun render(info: LevelInfo<String>, to: Appendable) {
                if ((info.type.estimatedLength ?: 0) < 255)
                    to.append(info.data)
                else {
                    to.append("<p>")
                    to.append(info.data)
                    to.append("</p>")
                }
            }

            override fun renderForm(info: LevelInfo<String>, to: Appendable) {
                if ((info.type.estimatedLength ?: 0) < 255) {
                    val inputType = if (info.type.password) "password" else "text"
                    to.append("""<input type="$inputType" name="${info.name}" value="${info.data}"/>""")
                } else {
                    to.append("""<textarea name="${info.name}" rows="4" cols="50">""")
                    to.append(info.data)
                    to.append("</textarea>")
                }
            }

            override fun parse(info: LevelInfo<String>): String? {
                return info.context.httpRequest.parameter(info.name)
            }

            override fun renderParameters(info: LevelInfo<String>, to: MutableMap<String, String>) {
                info.data?.let { to[info.name] = it }
            }
        })

        register(Date::class, object : HtmlSubConverter<Date> {
            override fun render(info: LevelInfo<Date>, to: Appendable) {
                to.append(info.data?.let { DateFormat.getDateTimeInstance().format(it) })
            }

            override fun renderForm(info: LevelInfo<Date>, to: Appendable) {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
                to.append("""<input type="datetime-local" name="${info.name}" value="${info.data?.let { format.format(it) }}"/>""")
            }

            override fun parse(info: LevelInfo<Date>): Date? {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
                return info.context.httpRequest.parameter(info.name)?.let { format.parse(it) }
            }

            override fun renderParameters(info: LevelInfo<Date>, to: MutableMap<String, String>) {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
                info.data?.let { format.format(it) }?.let { to[info.name] = it }
            }
        })

        subGenerators += sub@{ kclass ->
            if (!kclass.allSuperclasses.contains(Enum::class)) return@sub null
            return@sub object : HtmlSubConverter<Enum<*>> {

                val enumValues = kclass.enumValues

                override fun render(info: LevelInfo<Enum<*>>, to: Appendable) {
                    to.append(info.data?.name?.nameify())
                }

                override fun renderForm(info: LevelInfo<Enum<*>>, to: Appendable) {
                    to.append("<select name=\"${info.name}\">")
                    for (item in enumValues) {
                        to.append("<option ")
                        if (item.value == info.data) {
                            to.append("selected=\"selected\" ")
                        }
                        to.append("value=\"${item.key}\">")
                        to.append(item.value.name.nameify())
                        to.append("</option>")
                    }
                    to.append("</select>")
                }

                override fun parse(info: LevelInfo<Enum<*>>): Enum<*>? {
                    return info.type.kclass.enumValues[info.context.httpRequest.parameter(info.name)]
                }

                override fun renderParameters(info: LevelInfo<Enum<*>>, to: MutableMap<String, String>) {
                    info.data?.name?.let { to[info.name] = it }
                }
            }
        }

        register(List::class, object : HtmlSubConverter<List<*>> {
            override fun render(info: LevelInfo<List<*>>, to: Appendable) {
                if (info.data == null) to.append("null list")
                else {
                    to.append("<ul>")
                    info.data.forEachIndexed { index, item ->
                        to.append("<li>")
                        val sub = LevelInfo(
                                context = info.context,
                                depth = info.depth + 1,
                                callDepth = info.callDepth,
                                type = info.type.typeParameters[0],
                                property = info.property,
                                name = "${info.name}[$index]",
                                data = item
                        )
                        info.context.htmlConverter.retrieveAny(info.type.typeParameters[0].kclass).renderSafe(sub, to)
                        to.append("</li>")
                    }
                    to.append("</ul>")
                }
            }

            override fun renderForm(info: LevelInfo<List<*>>, to: Appendable) {
                to.append("""<input name="${info.name} type="hidden" value="${info.data?.size}"/>""")
                if (info.data == null) to.append("null list")
                else {
                    to.append("<ul>")
                    info.data.forEachIndexed { index, item ->
                        to.append("<li>")
                        val sub = LevelInfo(
                                context = info.context,
                                depth = info.depth + 1,
                                callDepth = info.callDepth,
                                type = info.type.typeParameters[0],
                                property = info.property,
                                name = "${info.name}[$index]",
                                data = item
                        )
                        info.context.htmlConverter.retrieveAny(info.type.typeParameters[0].kclass).renderFormSafe(sub, to)
                        to.append("</li>")
                    }
                    to.append("</ul>")
                }
            }

            override fun parse(info: LevelInfo<List<*>>): List<*>? {
                val count = info.context.httpRequest.parameter(info.name)?.toIntOrNull() ?: return null
                val result = ArrayList<Any?>()
                for (index in 0 until count) {
                    val sub = LevelInfo(
                            context = info.context,
                            depth = info.depth + 1,
                            callDepth = info.callDepth,
                            type = info.type.typeParameters[0],
                            property = info.property,
                            name = "${info.name}[$index]",
                            data = null
                    )
                    result += info.context.htmlConverter.retrieveAny(info.type.typeParameters[0].kclass).parseSafe(sub)
                }
                return result
            }

            override fun renderParameters(info: LevelInfo<List<*>>, to: MutableMap<String, String>) {
                info.data?.let {
                    it.forEachIndexed { index, any ->
                        val sub = LevelInfo(
                                context = info.context,
                                depth = info.depth + 1,
                                callDepth = info.callDepth,
                                type = info.type.typeParameters[0],
                                property = info.property,
                                name = "${info.name}[$index]",
                                data = null
                        )
                        info.context.htmlConverter.retrieveAny(info.type.typeParameters[0].kclass).renderParametersSafe(sub, to)
                    }
                }
            }
        })

        //Should this exist?
        subGenerators += sub@{ kclass ->
            if (!kclass.allSuperclasses.contains(MutableList::class) && kclass != MutableList::class) return@sub null
            return@sub object : HtmlSubConverter<List<*>> {

                override fun render(info: LevelInfo<List<*>>, to: Appendable) {
                    if (info.data == null) to.append("null list")
                    else {
                        to.append("<ul>")
                        info.data.forEachIndexed { index, item ->
                            to.append("<li>")
                            val sub = LevelInfo(
                                    context = info.context,
                                    depth = info.depth + 1,
                                    callDepth = info.callDepth,
                                    type = info.type.typeParameters[0],
                                    property = info.property,
                                    name = "${info.name}[$index]",
                                    data = item
                            )
                            info.context.htmlConverter.retrieveAny(info.type.typeParameters[0].kclass).renderSafe(sub, to)
                            to.append("</li>")
                        }
                        to.append("</ul>")
                    }
                }

                override fun renderForm(info: LevelInfo<List<*>>, to: Appendable) {
                    to.append("""<input name="${info.name} type="hidden" value="${info.data?.size}"/>""")
                    if (info.data == null) to.append("null list")
                    else {
                        to.append("<ul>")
                        info.data.forEachIndexed { index, item ->
                            to.append("<li>")
                            val sub = LevelInfo(
                                    context = info.context,
                                    depth = info.depth + 1,
                                    callDepth = info.callDepth,
                                    type = info.type.typeParameters[0],
                                    property = info.property,
                                    name = "${info.name}[$index]",
                                    data = item
                            )
                            info.context.htmlConverter.retrieveAny(info.type.typeParameters[0].kclass).renderFormSafe(sub, to)
                            to.append("</li>")
                        }
                        to.append("</ul>")
                    }
                }

                override fun parse(info: LevelInfo<List<*>>): List<*>? {
                    val count = info.context.httpRequest.parameter(info.name)?.toIntOrNull() ?: return null
                    val result = if (kclass.isAbstract) ArrayList<Any?>() else kclass.createInstance() as MutableList<Any?>
                    for (index in 0 until count) {
                        val sub = LevelInfo(
                                context = info.context,
                                depth = info.depth + 1,
                                callDepth = info.callDepth,
                                type = info.type.typeParameters[0],
                                property = info.property,
                                name = "${info.name}[$index]",
                                data = null
                        )
                        result += info.context.htmlConverter.retrieveAny(info.type.typeParameters[0].kclass).parseSafe(sub)
                    }
                    return result
                }

                override fun renderParameters(info: LevelInfo<List<*>>, to: MutableMap<String, String>) {
                    info.data?.let {
                        it.forEachIndexed { index, any ->
                            val sub = LevelInfo(
                                    context = info.context,
                                    depth = info.depth + 1,
                                    callDepth = info.callDepth,
                                    type = info.type.typeParameters[0],
                                    property = info.property,
                                    name = "${info.name}[$index]",
                                    data = null
                            )
                            info.context.htmlConverter.retrieveAny(info.type.typeParameters[0].kclass).renderParametersSafe(sub, to)
                        }
                    }
                }
            }
        }

        register(Map::class, object : HtmlSubConverter<Map<*, *>> {
            override fun render(info: LevelInfo<Map<*, *>>, to: Appendable) {
                if (info.data == null) to.append("null list")
                else {
                    to.append("<dl>")
                    info.data.entries.forEachIndexed { index, (key, value) ->
                        to.append("<dt>")
                        val subKey = LevelInfo(
                                context = info.context,
                                depth = info.depth + 1,
                                callDepth = info.callDepth,
                                type = info.type.typeParameters[0],
                                property = info.property,
                                name = "${info.name}[$index].key",
                                data = key
                        )
                        info.context.htmlConverter.retrieveAny(info.type.typeParameters[0].kclass).renderSafe(subKey, to)
                        to.append("</dt>")
                        to.append("<dd>")
                        val subValue = LevelInfo(
                                context = info.context,
                                depth = info.depth + 1,
                                callDepth = info.callDepth,
                                type = info.type.typeParameters[1],
                                property = info.property,
                                name = "${info.name}[$index].value",
                                data = value
                        )
                        info.context.htmlConverter.retrieveAny(info.type.typeParameters[1].kclass).renderSafe(subValue, to)
                        to.append("</dd>")
                    }
                    to.append("</dl>")
                }
            }

            override fun renderForm(info: LevelInfo<Map<*, *>>, to: Appendable) {
                to.append("""<input name="${info.name} type="hidden" value="${info.data?.size}"/>""")
                if (info.data == null) to.append("null list")
                else {
                    to.append("<dl>")
                    info.data.entries.forEachIndexed { index, (key, value) ->
                        to.append("<dt>")
                        val subKey = LevelInfo(
                                context = info.context,
                                depth = info.depth + 1,
                                callDepth = info.callDepth,
                                type = info.type.typeParameters[0],
                                property = info.property,
                                name = "${info.name}[$index].key",
                                data = key
                        )
                        info.context.htmlConverter.retrieveAny(info.type.typeParameters[0].kclass).renderFormSafe(subKey, to)
                        to.append("</dt>")
                        to.append("<dd>")
                        val subValue = LevelInfo(
                                context = info.context,
                                depth = info.depth + 1,
                                callDepth = info.callDepth,
                                type = info.type.typeParameters[1],
                                property = info.property,
                                name = "${info.name}[$index].value",
                                data = value
                        )
                        info.context.htmlConverter.retrieveAny(info.type.typeParameters[1].kclass).renderFormSafe(subValue, to)
                        to.append("</dd>")
                    }
                    to.append("</dl>")
                }
            }

            override fun parse(info: LevelInfo<Map<*, *>>): Map<*, *>? {
                val count = info.context.httpRequest.parameter(info.name)?.toIntOrNull() ?: return null
                val result = LinkedHashMap<Any?, Any?>()
                for (index in 0 until count) {
                    val subKey = LevelInfo(
                            context = info.context,
                            depth = info.depth + 1,
                            callDepth = info.callDepth,
                            type = info.type.typeParameters[0],
                            property = info.property,
                            name = "${info.name}[$index].key",
                            data = null
                    )
                    val subValue = LevelInfo(
                            context = info.context,
                            depth = info.depth + 1,
                            callDepth = info.callDepth,
                            type = info.type.typeParameters[1],
                            property = info.property,
                            name = "${info.name}[$index].value",
                            data = null
                    )
                    result.put(
                            info.context.htmlConverter.retrieveAny(info.type.typeParameters[0].kclass).parseSafe(subKey),
                            info.context.htmlConverter.retrieveAny(info.type.typeParameters[1].kclass).parseSafe(subValue)
                    )
                }
                return result
            }

            override fun renderParameters(info: LevelInfo<Map<*, *>>, to: MutableMap<String, String>) {
                info.data?.let {
                    it.entries.forEachIndexed { index, (key, value) ->
                        val subKey = LevelInfo(
                                context = info.context,
                                depth = info.depth + 1,
                                callDepth = info.callDepth,
                                type = info.type.typeParameters[0],
                                property = info.property,
                                name = "${info.name}[$index].key",
                                data = key
                        )
                        info.context.htmlConverter.retrieveAny(info.type.typeParameters[0].kclass).renderParametersSafe(subKey, to)

                        val subValue = LevelInfo(
                                context = info.context,
                                depth = info.depth + 1,
                                callDepth = info.callDepth,
                                type = info.type.typeParameters[1],
                                property = info.property,
                                name = "${info.name}[$index].value",
                                data = value
                        )
                        info.context.htmlConverter.retrieveAny(info.type.typeParameters[1].kclass).renderParametersSafe(subValue, to)
                    }
                }
            }
        })

        register(Pointer::class, object : HtmlSubConverter<Pointer<*, *>> {
            override fun render(info: LevelInfo<Pointer<*, *>>, to: Appendable) {
                if (info.data == null)
                    to.append("No Pointer")
                else {
                    val max = info.context.htmlConverter.maxCallDepth
                    val depth = info.callDepth
                    when {
                        depth < max -> {
                            val value = info.context.getTransaction.invoke().use {
                                info.type.typeParameters.first().kclass.getFromId(info.data!!)!!.invoke(it)
                            }
                            val sub = LevelInfo(
                                    context = info.context,
                                    depth = info.depth + 1,
                                    callDepth = info.callDepth + 1,
                                    type = info.type.typeParameters[0],
                                    property = info.property,
                                    name = "${info.name}.value",
                                    data = value
                            )
                            info.context.htmlConverter.retrieveAny(info.type.typeParameters.first().kclass)
                                    .renderSafe(sub, to)
                        }
                        depth == max -> {
                            //Todo: Convert to link
                            val linkTo = info.type.typeParameters.first().kclass.getFromId(info.data!!)!!
                            val sub = LevelInfo(
                                    context = info.context,
                                    depth = info.depth + 1,
                                    callDepth = info.callDepth,
                                    type = TypeInformation(linkTo.javaClass.kotlin),
                                    property = info.property,
                                    name = "value",
                                    data = linkTo
                            )
                            val params = HashMap<String, String>()
                            info.context.htmlConverter.retrieveAny(linkTo.javaClass.kotlin)
                                    .renderParametersSafe(sub, params)
                            val paramString = paramsToQueryString(params)
                            to.append("""<a href="${linkTo.javaClass.kotlin.urlName() + paramString}">""")
                            to.append(info.type.typeParameters.first().kclass.friendlyName)
                            to.append(" (")
                            to.append(info.data.key?.toString())
                            to.append(")")
                            to.append("</a>")
                        }
                        else -> {
                            val linkTo = info.type.typeParameters.first().kclass.getFromId(info.data!!)!!
                            val sub = LevelInfo(
                                    context = info.context,
                                    depth = info.depth + 1,
                                    callDepth = info.callDepth,
                                    type = info.type.typeParameters[1],
                                    property = info.property,
                                    name = "${info.name}.key",
                                    data = linkTo
                            )
                            to.append("Key: ")
                            info.context.htmlConverter.retrieveAny(info.type.typeParameters[1].kclass)
                                    .renderSafe(sub, to)
                        }
                    }
                }

            }

            override fun renderForm(info: LevelInfo<Pointer<*, *>>, to: Appendable) {
                val options = info.context.getTransaction.invoke().use {
                    info.type.typeParameters.first().kclass.query()?.invoke(it)
                } ?: listOf<Any>()
                val pointType = info.type.typeParameters[0]
                to.append("<select name=\"${info.name}\">")
                for (item in options) {
                    val key = item?.let { pointType.kclass.getPrimaryKeyValue(it) }
                    val keyString = keyToString(key)
                    to.append("<option ")
                    if (key == info.data?.key) {
                        to.append("selected=\"selected\" ")
                    }
                    to.append("value=\"$keyString\">")
                    to.append(item?.toString() ?: "None")
                    to.append("</option>")
                }
                to.append("</select>")
            }

            override fun parse(info: LevelInfo<Pointer<*, *>>): Pointer<*, *>? {
                val stringValue = info.context.httpRequest.parameter(info.name)
                @Suppress("IMPLICIT_CAST_TO_ANY")
                val key = when (info.type.typeParameters[1].kclass) {
                    Byte::class -> stringValue?.toByteOrNull()
                    Short::class -> stringValue?.toShortOrNull()
                    Int::class -> stringValue?.toIntOrNull()
                    Long::class -> stringValue?.toLongOrNull()
                    Float::class -> stringValue?.toFloatOrNull()
                    Double::class -> stringValue?.toDoubleOrNull()
                    String::class -> stringValue
                    else -> try {
                        MyJackson.mapper.readerFor(info.type.typeParameters[1].toJavaType()).readValue<Any?>(stringValue)
                    } catch (e: Exception) {
                        null
                    }
                }
                return key?.let { Pointer<Any, Any?>(it) }
            }

            fun keyToString(key: Any?): String? {
                return when (key) {
                    null -> null
                    is Byte,
                    is Short,
                    is Int,
                    is Long,
                    is Float,
                    is Double,
                    is String -> key.toString()
                    else -> key.jacksonToString()
                }
            }

            override fun renderParameters(info: LevelInfo<Pointer<*, *>>, to: MutableMap<String, String>) {
                keyToString(info.data?.key)?.let { to[info.name] = it }
            }
        })

        subGenerators += sub@{ kclass ->
            if (!kclass.allSuperclasses.contains(Exception::class) && kclass != Exception::class) return@sub null
            //Any!
            return@sub object : HtmlSubConverter<Exception> {

                override fun render(info: LevelInfo<Exception>, to: Appendable) {
                    to.append("<h1>${info.data?.javaClass?.simpleName}</h1>")
                    to.append("<p>")
                    to.append(info.data?.message)
                    to.append("</p>")
                    if (ServerSettings.debugMode) {
                        to.append("<code>")
                        to.append(info.data?.stackTraceString())
                        to.append("</code>")
                    }
                }

                override fun renderForm(info: LevelInfo<Exception>, to: Appendable) {
                }

                override fun parse(info: LevelInfo<Exception>): Exception? {
                    return null
                }

                override fun renderParameters(info: LevelInfo<Exception>, to: MutableMap<String, String>) {
                }

            }
        }

        subGenerators += sub@{ kclass ->
            //Any!
            return@sub object : HtmlSubConverter<Any> {

                val subPropertiesHidden = kclass.fastMutableProperties.values.filter { it.hidden }.map {
                    SubField(
                            property = it as KMutableProperty1<Any, Any?>,
                            subConverter = this@HtmlConverter.retrieveAny(it.fastType.kclass)
                    )
                }.toTypedArray()
                val subProperties = kclass.fastMutableProperties.values.filter { !it.hidden }.map {
                    SubField(
                            property = it as KMutableProperty1<Any, Any?>,
                            subConverter = this@HtmlConverter.retrieveAny(it.fastType.kclass)
                    )
                }.toTypedArray()

                override fun render(info: LevelInfo<Any>, to: Appendable) {
                    if (info.data is ServerFunction<*>) {
                        to.append("""<form method="post" action="#">""")
                        renderForm(info, to)
                        to.append("""<input class="submit" type="submit" value="Submit"/>""")
                        to.append("""</form>""")
                        return
                    }
                    to.append("""<div class="object">""")
                    to.append("""<p class="object-type">${info.type.kclass.friendlyName}</p>""")
                    if (info.data == null) {
                        to.append("<p>null</p>")
                    } else {
                        to.append("<dl>")
                        for (prop in subProperties) {
                            val properName = prop.property.friendlyName
                            to.append("<dt>$properName</dt>")
                            to.append("<dd>")
                            val subInfo = LevelInfo(
                                    context = info.context,
                                    depth = info.depth + 1,
                                    callDepth = info.callDepth,
                                    type = prop.type,
                                    property = prop.property,
                                    name = info.name + "." + prop.name,
                                    data = prop.property.get(info.data!!)
                            )
                            prop.subConverter.renderSafe(subInfo, to)
                            to.append("</dd>")
                        }
                        to.append("</dl>")
                        to.append("</div>")
                    }
                }

                override fun renderForm(info: LevelInfo<Any>, to: Appendable) {
                    to.append("""<div class="object">""")
                    to.append("""<p class="object-type">${info.type.kclass.friendlyName}</p>""")
                    if (info.data == null) {
                        to.append("<p>null</p>")
                    } else {
                        for (prop in subPropertiesHidden) {
                            val subInfo = LevelInfo(
                                    context = info.context,
                                    depth = info.depth + 1,
                                    callDepth = info.callDepth,
                                    type = prop.type,
                                    property = prop.property,
                                    name = info.name + "." + prop.name,
                                    data = prop.property.get(info.data!!)
                            )
                            val toMap = HashMap<String, String>()
                            prop.subConverter.renderParametersSafe(subInfo, toMap)
                            for ((key, value) in toMap) {
                                to.append("""<input name="$key" type="hidden" value="$value"/>""")
                            }
                        }
                        to.append("<dl>")
                        for (prop in subProperties) {
                            val properName = prop.property.friendlyName
                            to.append("<dt>$properName</dt>")
                            to.append("<dd>")
                            val subInfo = LevelInfo(
                                    context = info.context,
                                    depth = info.depth + 1,
                                    callDepth = info.callDepth,
                                    type = prop.type,
                                    property = prop.property,
                                    name = info.name + "." + prop.name,
                                    data = prop.property.get(info.data!!)
                            )
                            prop.subConverter.renderFormSafe(subInfo, to)
                            to.append("</dd>")
                        }
                        to.append("</dl>")
                        to.append("</div>")
                    }
                }

                override fun parse(info: LevelInfo<Any>): Any? {
                    val instance = info.type.kclass.createInstance()
                    for (prop in subProperties) {
                        try {
                            val subInfo = LevelInfo(
                                    context = info.context,
                                    depth = info.depth + 1,
                                    callDepth = info.callDepth,
                                    type = prop.type,
                                    property = prop.property,
                                    name = info.name + "." + prop.name,
                                    data = null
                            )
                            val value = prop.subConverter.parseSafe(subInfo)
                            if (value != null || prop.type.nullable)
                                prop.property.setUntyped(instance, value)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    for (prop in subPropertiesHidden) {
                        try {
                            val subInfo = LevelInfo(
                                    context = info.context,
                                    depth = info.depth + 1,
                                    callDepth = info.callDepth,
                                    type = prop.type,
                                    property = prop.property,
                                    name = info.name + "." + prop.name,
                                    data = null
                            )
                            val value = prop.subConverter.parseSafe(subInfo)
                            if (value != null || prop.type.nullable)
                                prop.property.setUntyped(instance, value)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    return instance
                }

                override fun renderParameters(info: LevelInfo<Any>, to: MutableMap<String, String>) {
                    if (info.data == null) return
                    for (prop in subProperties) {
                        val value = prop.property.get(info.data)
                        val subInfo = LevelInfo(
                                context = info.context,
                                depth = info.depth + 1,
                                callDepth = info.callDepth,
                                type = prop.type,
                                property = prop.property,
                                name = info.name + "." + prop.name,
                                data = value
                        )
                        prop.subConverter.renderParametersSafe(subInfo, to)
                    }
                    for (prop in subPropertiesHidden) {
                        val value = prop.property.get(info.data)
                        val subInfo = LevelInfo(
                                context = info.context,
                                depth = info.depth + 1,
                                callDepth = info.callDepth,
                                type = prop.type,
                                property = prop.property,
                                name = info.name + "." + prop.name,
                                data = value
                        )
                        prop.subConverter.renderParametersSafe(subInfo, to)
                    }
                }

            }
        }
    }

    data class SubField(
            val property: KMutableProperty1<Any, Any?>,
            val subConverter: HtmlSubConverter<Any>
    ) {
        val name = property.name
        val type = property.fastType
    }

    companion object {

        fun paramsToQueryString(params: Map<String, String>): String {
            return params.entries.joinToString("&", "?") {
                URLEncoder.encode(it.key, Charsets.UTF_8.name()) +
                        "=" +
                        URLEncoder.encode(it.value, Charsets.UTF_8.name())
            }
        }

        fun setCookie(to: Appendable, name: String, value: String) {
            to.append("""<script>document.cookie = "$name=$value"</script>""")
        }
    }
}