package lk.kotlin.server.types.html

import lk.kotlin.server.base.parameter

class BooleanConverter : HtmlSubConverterNullHandling<Boolean> {
    override fun renderNonNull(info: ItemConversionInfo<Boolean>, to: Appendable) {
        when (info.data) {
            true -> to.append('☑')
            false -> to.append('☐')
            null -> to.append('?')
        }
    }

    override fun renderFormNonNull(info: ItemConversionInfo<Boolean>, to: Appendable) {
        to.append("""<input type="checkbox" name="${info.name}" value="true"/>""")
    }

    override fun parseNonNull(info: ItemConversionInfo<Boolean>): Boolean? {
        return info.context.httpRequest.parameter(info.name)?.toBoolean()
    }

    override fun renderParametersNonNull(info: ItemConversionInfo<Boolean>, to: MutableMap<String, String>) {
        info.data?.toString()?.let { to[info.name] = it }
    }
}