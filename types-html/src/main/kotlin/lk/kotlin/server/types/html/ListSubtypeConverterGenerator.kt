package lk.kotlin.server.types.html

import lk.kotlin.reflect.enumValues
import lk.kotlin.reflect.nameify
import lk.kotlin.server.base.parameter
import java.util.ArrayList
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.createInstance

val ListSubtypeConverterGenerator: HtmlSubConverterGenerator = sub@{ kclass ->
    if (!kclass.allSuperclasses.contains(MutableList::class) && kclass != MutableList::class) return@sub null
    return@sub object : HtmlSubConverterNullHandling<List<*>> {

        override fun renderNonNull(info: ItemConversionInfo<List<*>>, to: Appendable) {
            if (info.data == null) to.append("null list")
            else {
                to.append("<ul>")
                info.data.forEachIndexed { index, item ->
                    to.append("<li>")
                    val sub = ItemConversionInfo(
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

        override fun renderFormNonNull(info: ItemConversionInfo<List<*>>, to: Appendable) {
            to.append("""<input name="${info.name}" type="hidden" value="${info.data?.size}"/>""")
            if (info.data == null) to.append("null list")
            else {
                to.append("<ul>")
                info.data.forEachIndexed { index, item ->
                    to.append("<li>")
                    val sub = ItemConversionInfo(
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

        override fun parseNonNull(info: ItemConversionInfo<List<*>>): List<*>? {
            val count = info.context.httpRequest.parameter(info.name)?.toIntOrNull()
                    ?: return null
            val result = if (kclass.isAbstract) ArrayList<Any?>() else kclass.createInstance() as MutableList<Any?>
            for (index in 0 until count) {
                val sub = ItemConversionInfo(
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

        override fun renderParametersNonNull(info: ItemConversionInfo<List<*>>, to: MutableMap<String, String>) {
            info.data?.let {
                it.forEachIndexed { index, any ->
                    val sub = ItemConversionInfo(
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