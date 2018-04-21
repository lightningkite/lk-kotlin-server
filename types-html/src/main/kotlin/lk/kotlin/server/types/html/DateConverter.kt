package lk.kotlin.server.types.html

import lk.kotlin.server.base.parameter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DateConverter : HtmlSubConverterNullHandling<Date> {
    override fun renderNonNull(info: ItemConversionInfo<Date>, to: Appendable) {
        to.append(info.data?.let { DateFormat.getDateTimeInstance().format(it) })
    }

    override fun renderFormNonNull(info: ItemConversionInfo<Date>, to: Appendable) {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").apply { timeZone = TimeZone.getTimeZone("MDT") }
        to.append("""<input type="datetime-local" name="${info.name}" value="${info.data?.let { format.format(it) }}"/>""")
    }

    override fun parseNonNull(info: ItemConversionInfo<Date>): Date? {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").apply { timeZone = TimeZone.getTimeZone("MDT") }
        return info.context.httpRequest.parameter(info.name)?.let { format.parse(it) }
    }

    override fun renderParametersNonNull(info: ItemConversionInfo<Date>, to: MutableMap<String, String>) {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").apply { timeZone = TimeZone.getTimeZone("MDT") }
        info.data?.let { format.format(it) }?.let { to[info.name] = it }
    }
}