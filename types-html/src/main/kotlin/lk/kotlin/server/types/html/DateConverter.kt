package lk.kotlin.server.types.html

import lk.kotlin.server.base.parameter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DateConverter : HtmlSubConverterNullHandling<Date> {

    val tz = TimeZone.getTimeZone("America/Denver")

    override fun renderNonNull(info: ItemConversionInfo<Date>, to: Appendable) {
        to.append(info.data?.let { DateFormat.getDateTimeInstance().apply { timeZone = tz }.format(it) })
    }

    override fun renderFormNonNull(info: ItemConversionInfo<Date>, to: Appendable) {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").apply { timeZone = tz }
        to.append("""<input type="datetime-local" name="${info.name}" value="${info.data?.let { format.format(it) }}"/>""")
    }

    override fun parseNonNull(info: ItemConversionInfo<Date>): Date? {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").apply { timeZone = tz }
        return info.context.httpRequest.parameter(info.name)?.let { format.parse(it) }
    }

    override fun renderParametersNonNull(info: ItemConversionInfo<Date>, to: MutableMap<String, String>) {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").apply { timeZone = tz }
        info.data?.let { format.format(it) }?.let { to[info.name] = it }
    }
}