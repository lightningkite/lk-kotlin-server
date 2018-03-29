package com.lightningkite.kotlin.server.base

import lk.kotlin.reflect.*
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

typealias HtmlFormRenderer<T> = (
        context: DefaultHtmlConverter,
        type: TypeInformation,
        request: HttpServletRequest,
        property: KProperty1<*, *>?,
        name: String,
        data: T?,
        to: Appendable
) -> Unit


typealias HtmlSubRenderer<T> = (
        context: DefaultHtmlConverter,
        type: TypeInformation,
        request: HttpServletRequest,
        property: KProperty1<*, *>?,
        data: T?,
        to: Appendable
) -> Unit


typealias HtmlSubParser<T> = (
        context: DefaultHtmlConverter,
        type: TypeInformation,
        request: HttpServletRequest,
        property: KProperty1<*, *>?,
        name: String
) -> T?

class DefaultHtmlConverter : Parser, Renderer {
    var pageWrapperPrepend = """<!DOCTYPE html><html><head><meta charset="utf-8"/><link rel="stylesheet" href="/style.css"/></head><body>"""
    var pageWrapperAppend = """</body></html>"""


    val subFormRenderers = HashMap<KClass<*>, HtmlFormRenderer<*>>()
    val subRenderers = HashMap<KClass<*>, HtmlSubRenderer<*>>()
    val subParserers = HashMap<KClass<*>, HtmlSubParser<*>>()

    private fun findSuitableType(type: KClass<*>, map: Map<KClass<*>, *>): KClass<*> {
        if (map.containsKey(type)) return type
        val queue = arrayListOf(type)
        while (queue.isNotEmpty()) {
            val next = queue.removeAt(0)
            if (next == Any::class) continue
            if (map.containsKey(next)) return next
            //add all supertypes
            queue.addAll(next.fastSuperclasses)
        }
        return Any::class
    }

    override fun <T> parse(type: TypeInformation, request: HttpServletRequest): T {
        println(request.parameterMap)
        val kclass = findSuitableType(type.kclass, subParserers)
        val parser = subParserers[kclass]!! as HtmlSubParser<T>
        return parser.invoke(this, type, request, null, "value") as T
    }

    override fun <T> render(type: TypeInformation, data: T, request: HttpServletRequest, response: HttpServletResponse) {
        val kclass = findSuitableType(type.kclass, subRenderers)
        val renderer = subRenderers[kclass]!! as HtmlSubRenderer<Any?>
        response.writer.use {
            it.append(pageWrapperPrepend)
            renderer.invoke(this, type, request, null, data, it)
            it.append(pageWrapperAppend)
            it.flush()
        }
    }

    fun <T> subparse(
            type: TypeInformation,
            request: HttpServletRequest,
            property: KProperty1<*, *>?,
            field: String
    ): T? {
        val kclass = findSuitableType(type.kclass, subParserers)
        val parser = subParserers[kclass]!! as HtmlSubParser<T>
        return parser.invoke(this, type, request, null, field)
    }

    fun <T> subrender(
            type: TypeInformation,
            request: HttpServletRequest,
            property: KProperty1<*, *>?,
            data: T?,
            to: Appendable
    ) {
        val kclass = findSuitableType(type.kclass, subRenderers)
        val renderer = subRenderers[kclass]!! as HtmlSubRenderer<T>
        return renderer.invoke(this, type, request, property, data, to)
    }

    fun <T> subrenderForm(
            type: TypeInformation,
            request: HttpServletRequest,
            property: KProperty1<*, *>?,
            name: String,
            data: T?,
            to: Appendable
    ) {
        val kclass = findSuitableType(type.kclass, subFormRenderers)
        val renderer = subFormRenderers[kclass]!! as HtmlFormRenderer<T>
        return renderer.invoke(this, type, request, property, name, data, to)
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
            request.getParameter(name).toIntOrNull()
        }


        addRenderer<Long> { context, type, request, field, data, to ->
            to.append(data.toString())
        }
        addFormRenderer<Long> { context, type, request, field, name, data, to ->
            to.append("""<input type="number" name="$name" value="$data"/>""")
        }
        addParser<Long> { context, type, request, field, name ->
            request.getParameter(name).toLongOrNull()
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
            request.getParameter(name).toFloatOrNull()
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
            request.getParameter(name).toDoubleOrNull()
        }


        addRenderer<String> { context, type, request, field, data, to ->
            to.append(data)
        }
        addFormRenderer<String> { context, type, request, field, name, data, to ->
            val inputType = if (name.contains("password", false)) "password" else "text"
            to.append("""<input type="$inputType" name="$name" value="$data"/>""")
        }
        addParser<String> { context, type, request, field, name ->
            request.getParameter(name)
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
            request.getParameter(name)?.let { format.parse(it) }
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
            val count = request.getParameter(name).toIntOrNull() ?: return@addParser null
            val result = ArrayList<Any?>()
            for (index in 0 until count) {
                val subname = "$name[$index]"
                result += subparse<Any?>(type.typeParameters[0], request, field, subname)
            }
            result
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
                for (field in type.kclass.fastMutableProperties) {
                    val properName = field.value.friendlyName
                    to.append("<dt>$properName</dt>")
                    to.append("<dd>")
                    context.subrender(field.value.fastType, request, field.value, field.value.reflectAsmGet(data), to)
                    to.append("</dd>")
                }
                to.append("</dl>")
                to.append("</div>")
            }
        }
        addFormRenderer<Any> { context, type, request, field, name, data, to ->
            to.append("""<div class="object">""")
            to.append("""<p class="object-type">${type.kclass.friendlyName}</p>""")
            for (field in type.kclass.fastMutableProperties) {
                val properName = field.value.friendlyName
                val fieldName = name + "." + field.key
                to.append("<p>$properName ")
                if (field.value.findAnnotation<Meta.DoNotModify>() == null) {
                    context.subrenderForm(field.value.fastType, request, field.value, fieldName, data?.let { field.value.reflectAsmGet(it) }, to)
                } else {
                    context.subrender(field.value.fastType, request, field.value, data?.let { field.value.reflectAsmGet(it) }, to)
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