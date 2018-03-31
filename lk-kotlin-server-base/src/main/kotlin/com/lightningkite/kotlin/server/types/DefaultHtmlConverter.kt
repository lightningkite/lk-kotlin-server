package com.lightningkite.kotlin.server.types

import com.lightningkite.kotlin.server.base.HttpRequest
import com.lightningkite.kotlin.server.base.ServerSettings
import com.lightningkite.kotlin.server.base.parameter
import lk.kotlin.jvm.utils.exception.stackTraceString
import lk.kotlin.reflect.*
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

typealias HtmlFormRenderer<T> = (
        context: DefaultHtmlConverter,
        type: TypeInformation,
        httpRequest: HttpRequest,
        property: KProperty1<*, *>?,
        name: String,
        data: T?,
        to: Appendable
) -> Unit


typealias HtmlSubRenderer<T> = (
        context: DefaultHtmlConverter,
        type: TypeInformation,
        httpRequest: HttpRequest,
        property: KProperty1<*, *>?,
        data: T?,
        to: Appendable
) -> Unit


typealias HtmlSubParser<T> = (
        context: DefaultHtmlConverter,
        type: TypeInformation,
        httpRequest: HttpRequest,
        property: KProperty1<*, *>?,
        name: String
) -> T?

class DefaultHtmlConverter : Parser, Renderer {
    var pageWrapperPrepend = """<!DOCTYPE html><html><head><meta charset="utf-8"/><link rel="stylesheet" href="/style.css"/></head><body>"""
    var pageWrapperAppend = """</body></html>"""


    val subFormRenderers = HashMap<KClass<*>, HtmlFormRenderer<*>>()
    val subRenderers = HashMap<KClass<*>, HtmlSubRenderer<*>>()
    val subParserers = HashMap<KClass<*>, HtmlSubParser<*>>()

    override fun <T> parse(type: TypeInformation, httpRequest: HttpRequest): T {
        val kclass = subParserers.closestKey(type.kclass)
        val parser = subParserers[kclass]!! as HtmlSubParser<T>
        return parser.invoke(this, type, httpRequest, null, "value") as T
    }

    override fun <T> render(type: TypeInformation, data: T, httpRequest: HttpRequest, out: OutputStream) {
        val kclass = subRenderers.closestKey(type.kclass)
        val renderer = subRenderers[kclass]!! as HtmlSubRenderer<Any?>
        OutputStreamWriter(out).use {
            it.append(pageWrapperPrepend)
            renderer.invoke(this, type, httpRequest, null, data, it)
            it.append(pageWrapperAppend)
        }
    }

    fun <T> subparse(
            type: TypeInformation,
            httpRequest: HttpRequest,
            property: KProperty1<*, *>?,
            field: String
    ): T? {
        val kclass = subParserers.closestKey(type.kclass)
        val parser = subParserers[kclass]!! as HtmlSubParser<T>
        return parser.invoke(this, type, httpRequest, null, field)
    }

    fun <T> subrender(
            type: TypeInformation,
            httpRequest: HttpRequest,
            property: KProperty1<*, *>?,
            data: T?,
            to: Appendable
    ) {
        val kclass = subRenderers.closestKey(type.kclass)
        val renderer = subRenderers[kclass]!! as HtmlSubRenderer<T>
        return renderer.invoke(this, type, httpRequest, property, data, to)
    }

    fun <T> subrenderForm(
            type: TypeInformation,
            httpRequest: HttpRequest,
            property: KProperty1<*, *>?,
            name: String,
            data: T?,
            to: Appendable
    ) {
        val kclass = subFormRenderers.closestKey(type.kclass)
        val renderer = subFormRenderers[kclass]!! as HtmlFormRenderer<T>
        return renderer.invoke(this, type, httpRequest, property, name, data, to)
    }

    //Some defaults
    inline fun <reified T : Any> addFormRenderer(
            noinline render: HtmlFormRenderer<T>
    ) {
        subFormRenderers[T::class] = render
    }

    inline fun <reified T : Any> addRenderer(
            noinline render: HtmlSubRenderer<T>
    ) {
        subRenderers[T::class] = render
    }

    inline fun <reified T : Any> addParser(
            noinline render: HtmlSubParser<T>
    ) {
        subParserers[T::class] = render
    }

    init {
        addRenderer<Int> { context, type, request, field, data, to ->
            to.append(data.toString())
        }
        addFormRenderer<Int> { context, type, request, field, name, data, to ->
            to.append("""<input type="number" name="$name" value="$data"/>""")
        }
        addParser<Int> { context, type, request, field, name ->
            request.parameter(name)?.toIntOrNull()
        }


        addRenderer<Long> { context, type, request, field, data, to ->
            to.append(data.toString())
        }
        addFormRenderer<Long> { context, type, request, field, name, data, to ->
            to.append("""<input type="number" name="$name" value="$data"/>""")
        }
        addParser<Long> { context, type, request, field, name ->
            request.parameter(name)?.toLongOrNull()
        }


        addRenderer<Float> { context, type, request, field, data, to ->
            val stringData = run {
                val formatter = DecimalFormat("#.${"#".repeat(field?.decimalPoints ?: 2)}")
                formatter.format(data)
            }
            to.append(stringData)
        }
        addFormRenderer<Float> { context, type, request, field, name, data, to ->
            to.append("""<input type="number" name="$name" step="any" value="$data"/>""")
        }
        addParser<Float> { context, type, request, field, name ->
            request.parameter(name)?.toFloatOrNull()
        }


        addRenderer<Double> { context, type, request, field, data, to ->
            val stringData = run {
                val formatter = DecimalFormat("#.${"#".repeat(field?.decimalPoints ?: 2)}")
                formatter.format(data)
            }
            to.append(stringData)
        }
        addFormRenderer<Double> { context, type, request, field, name, data, to ->
            to.append("""<input type="number" name="$name" step="any" value="$data"/>""")
        }
        addParser<Double> { context, type, request, field, name ->
            request.parameter(name)?.toDoubleOrNull()
        }


        addRenderer<String> { context, type, request, field, data, to ->
            to.append(data)
        }
        addFormRenderer<String> { context, type, request, field, name, data, to ->
            val inputType = if (name.contains("password", false)) "password" else "text"
            to.append("""<input type="$inputType" name="$name" value="$data"/>""")
        }
        addParser<String> { context, type, request, field, name ->
            request.parameter(name)
        }


        addRenderer<Date> { context, type, request, field, data, to ->
            to.append(DateFormat.getDateTimeInstance().format(data))
        }
        addFormRenderer<Date> { context, type, request, field, name, data, to ->
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
            to.append("""<input type="datetime-local" name="$name" value="${format.format(data)}"/>""")
        }
        addParser<Date> { context, type, request, field, name ->
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
            request.parameter(name)?.let { format.parse(it) }
        }


        addRenderer<List<*>> { context, type, request, field, data, to ->
            if (data == null) to.append("null list")
            else {
                to.append("<ul>")
                data.forEach { item ->
                    to.append("<li>")
                    subrender(type.typeParameters[0], request, field, item, to)
                    to.append("</li>")
                }
                to.append("</ul>")
            }
        }
        addFormRenderer<List<*>> { context, type, request, field, name, data, to ->
            to.append("""<input name="$name type="hidden" value="${data?.size}"/>""")
            if (data == null) to.append("null list")
            else {
                to.append("<ul>")
                data.forEachIndexed { index, item ->
                    to.append("<li>")
                    subrenderForm(type.typeParameters[0], request, field, "$name[$index]", item, to)
                    to.append("</li>")
                }
                to.append("</ul>")
            }
        }
        addParser<List<*>> { context, type, request, field, name ->
            val count = request.parameter(name)?.toIntOrNull() ?: return@addParser null
            val result = ArrayList<Any?>()
            for (index in 0 until count) {
                val subname = "$name[$index]"
                result += subparse<Any?>(type.typeParameters[0], request, field, subname)
            }
            result
        }


        addRenderer<Exception> { context, type, request, field, data, to ->
            to.append("<h1>${data?.javaClass?.simpleName}</h1>")
            to.append("<p>")
            to.append(data?.message)
            to.append("</p>")
            if (ServerSettings.debugMode) {
                to.append("<code>")
                to.append(data?.stackTraceString())
                to.append("</code>")
            }
        }


        addRenderer<ServerFunction<*>> { context, type, request, field, data, to ->
            to.append("""<form method="post" action="#">""")
            context.subFormRenderers[Any::class]!!
                    .let { it as HtmlFormRenderer<Any?> }
                    .invoke(context, type, request, null, "value", data, to)
            to.append("""<input class="submit" type="submit" value="Submit"/>""")
            to.append("""</form>""")
        }


        addRenderer<Any> { context, type, request, field, data, to ->
            to.append("""<div class="object">""")
            to.append("""<p class="object-type">${type.kclass.friendlyName}</p>""")
            if (data == null) {
                to.append("<p>null</p>")
            } else {
                to.append("<dl>")
                for (subfield in type.kclass.fastMutableProperties) {
                    val properName = subfield.value.friendlyName
                    to.append("<dt>$properName</dt>")
                    to.append("<dd>")
                    context.subrender(subfield.value.fastType, request, subfield.value, subfield.value.reflectAsmGet(data), to)
                    to.append("</dd>")
                }
                to.append("</dl>")
                to.append("</div>")
            }
        }
        addFormRenderer<Any> { context, type, request, field, name, data, to ->
            to.append("""<div class="object">""")
            to.append("""<p class="object-type">${type.kclass.friendlyName}</p>""")
            for (subfield in type.kclass.fastMutableProperties) {
                val properName = subfield.value.friendlyName
                val fieldName = name + "." + subfield.key
                to.append("<p>$properName ")
                if (subfield.value.findAnnotation<Meta.DoNotModify>() == null) {
                    context.subrenderForm(subfield.value.fastType, request, subfield.value, fieldName, data?.let { subfield.value.reflectAsmGet(it) }, to)
                } else {
                    context.subrender(subfield.value.fastType, request, subfield.value, data?.let { subfield.value.reflectAsmGet(it) }, to)
                }
                to.append("</p>")
            }
            to.append("</div>")
        }

        addParser<Any> { context, type, request, field, name ->
            val instance = type.kclass.reflectAsmConstruct()
            for (field in type.kclass.fastMutableProperties) {
                try {
                    val value = context.subparse<Any?>(field.value.fastType, request, field.value, name + "." + field.key)
                    field.value.reflectAsmSet(instance, value)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            instance
        }
    }
}