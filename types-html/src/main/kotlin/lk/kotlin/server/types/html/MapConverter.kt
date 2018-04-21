package lk.kotlin.server.types.html

import lk.kotlin.server.base.parameter

class MapConverter() : HtmlSubConverterNullHandling<Map<*, *>> {
    override fun renderNonNull(info: ItemConversionInfo<Map<*, *>>, to: Appendable) {
        if (info.data == null) to.append("null list")
        else {
            to.append("<dl>")
            info.data.entries.forEachIndexed { index, (key, value) ->
                to.append("<dt>")
                val subKey = ItemConversionInfo(
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
                val subValue = ItemConversionInfo(
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

    override fun renderFormNonNull(info: ItemConversionInfo<Map<*, *>>, to: Appendable) {
        to.append("""<input name="${info.name}" type="hidden" value="${info.data?.size}"/>""")
        if (info.data == null) to.append("null list")
        else {
            to.append("<dl>")
            info.data.entries.forEachIndexed { index, (key, value) ->
                to.append("<dt>")
                val subKey = ItemConversionInfo(
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
                val subValue = ItemConversionInfo(
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

    override fun parseNonNull(info: ItemConversionInfo<Map<*, *>>): Map<*, *>? {
        val count = info.context.httpRequest.parameter(info.name)?.toIntOrNull()
                ?: return null
        val result = LinkedHashMap<Any?, Any?>()
        for (index in 0 until count) {
            val subKey = ItemConversionInfo(
                    context = info.context,
                    depth = info.depth + 1,
                    callDepth = info.callDepth,
                    type = info.type.typeParameters[0],
                    property = info.property,
                    name = "${info.name}[$index].key",
                    data = null
            )
            val subValue = ItemConversionInfo(
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

    override fun renderParametersNonNull(info: ItemConversionInfo<Map<*, *>>, to: MutableMap<String, String>) {
        info.data?.let {
            it.entries.forEachIndexed { index, (key, value) ->
                val subKey = ItemConversionInfo(
                        context = info.context,
                        depth = info.depth + 1,
                        callDepth = info.callDepth,
                        type = info.type.typeParameters[0],
                        property = info.property,
                        name = "${info.name}[$index].key",
                        data = key
                )
                info.context.htmlConverter.retrieveAny(info.type.typeParameters[0].kclass).renderParametersSafe(subKey, to)

                val subValue = ItemConversionInfo(
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
}