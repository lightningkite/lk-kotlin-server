package lk.kotlin.server.types.html

import lk.kotlin.reflect.enumValues
import lk.kotlin.reflect.nameify
import lk.kotlin.server.base.parameter
import kotlin.reflect.full.allSuperclasses

val EnumConversionGenerator: HtmlSubConverterGenerator = sub@{ kclass ->
    if (!kclass.allSuperclasses.contains(Enum::class)) return@sub null
    return@sub object : HtmlSubConverterNullHandling<Enum<*>> {

        val enumValues = kclass.enumValues

        override fun renderNonNull(info: ItemConversionInfo<Enum<*>>, to: Appendable) {
            to.append(info.data?.name?.nameify())
        }

        override fun renderFormNonNull(info: ItemConversionInfo<Enum<*>>, to: Appendable) {
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

        override fun parseNonNull(info: ItemConversionInfo<Enum<*>>): Enum<*>? {
            return info.type.kclass.enumValues[info.context.httpRequest.parameter(info.name)]
        }

        override fun renderParametersNonNull(info: ItemConversionInfo<Enum<*>>, to: MutableMap<String, String>) {
            info.data?.name?.let { to[info.name] = it }
        }
    }
}