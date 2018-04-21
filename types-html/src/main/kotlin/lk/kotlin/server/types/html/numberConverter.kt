package lk.kotlin.server.types.html

import lk.kotlin.server.base.parameter


inline fun <T : Number> numberConverter(crossinline stringToNum: (String) -> T?) = object : HtmlSubConverterNullHandling<T> {
    override fun renderNonNull(info: ItemConversionInfo<T>, to: Appendable) {
        to.append(info.data.toString())
    }

    override fun renderFormNonNull(info: ItemConversionInfo<T>, to: Appendable) {
        to.append("""<input type="number" name="${info.name}" value="${info.data}"/>""")
    }

    override fun parseNonNull(info: ItemConversionInfo<T>): T? {
        return info.context.httpRequest.parameter(info.name)?.let(stringToNum)
    }

    override fun renderParametersNonNull(info: ItemConversionInfo<T>, to: MutableMap<String, String>) {
        info.data?.toString()?.let { to[info.name] = it }
    }
}