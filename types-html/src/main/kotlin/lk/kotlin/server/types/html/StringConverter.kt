package lk.kotlin.server.types.html

import lk.kotlin.reflect.annotations.estimatedLength
import lk.kotlin.reflect.annotations.password
import lk.kotlin.server.base.parameter

class StringConverter : HtmlSubConverterNullHandling<String> {
    override fun renderNonNull(info: ItemConversionInfo<String>, to: Appendable) {
        if ((info.type.estimatedLength ?: 0) < 255)
            to.append(info.data)
        else {
            to.append("<p>")
            to.append(info.data)
            to.append("</p>")
        }
    }

    override fun renderFormNonNull(info: ItemConversionInfo<String>, to: Appendable) {
        if ((info.type.estimatedLength ?: 0) < 255) {
            val inputType = if (info.property?.password ?: false) "password" else "text"
            to.append("""<input type="$inputType" name="${info.name}" value="${info.data}"/>""")
        } else {
            to.append("""<textarea name="${info.name}" rows="4" cols="50">""")
            to.append(info.data)
            to.append("</textarea>")
        }
    }

    override fun parseNonNull(info: ItemConversionInfo<String>): String? {
        return info.context.httpRequest.parameter(info.name)
    }

    override fun renderParametersNonNull(info: ItemConversionInfo<String>, to: MutableMap<String, String>) {
        info.data?.let { to[info.name] = it }
    }
}