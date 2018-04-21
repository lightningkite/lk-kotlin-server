package lk.kotlin.server.types.html

import lk.kotlin.server.base.parameter
import kotlin.reflect.full.createInstance

interface HtmlSubConverterNullHandling<T> : HtmlSubConverter<T> {

    fun renderNonNull(info: ItemConversionInfo<T>, to: Appendable)
    fun renderFormNonNull(info: ItemConversionInfo<T>, to: Appendable)
    fun parseNonNull(info: ItemConversionInfo<T>): T?
    fun renderParametersNonNull(info: ItemConversionInfo<T>, to: MutableMap<String, String>)

    override fun render(info: ItemConversionInfo<T>, to: Appendable) {
        if(info.data == null)
            to.append("null")
        else
            renderNonNull(info, to)
    }

    override fun renderForm(info: ItemConversionInfo<T>, to: Appendable) {
        if (info.type.nullable) {
            to.append("""<p>Null?  <input type="checkbox" name="${info.name}.isNull" value="true"/></p>""")
            renderFormNonNull(info.copy(data = info.type.kclass.createInstance() as T), to)
        } else {
            renderFormNonNull(info, to)
        }
    }

    override fun parse(info: ItemConversionInfo<T>): T? {
        return if (info.type.nullable) {
            if (info.context.httpRequest.parameter("${info.name}.isNull") == "true")
                null
            else
                parseNonNull(info)
        } else {
            parseNonNull(info)
        }
    }

    override fun renderParameters(info: ItemConversionInfo<T>, to: MutableMap<String, String>) {
        if (info.type.nullable) {
            if (info.data == null)
                to["${info.name}.isNull"] = "true"
            else
                renderParametersNonNull(info, to)
        } else {
            renderParametersNonNull(info, to)
        }
    }
}