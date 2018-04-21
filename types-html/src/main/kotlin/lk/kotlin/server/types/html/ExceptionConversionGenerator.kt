package lk.kotlin.server.types.html

import lk.kotlin.jvm.utils.exception.stackTraceString
import lk.kotlin.server.base.ServerSettings
import kotlin.reflect.full.allSuperclasses

val ExceptionConversionGenerator:HtmlSubConverterGenerator = sub@{ kclass ->
    if (!kclass.allSuperclasses.contains(Exception::class) && kclass != Exception::class) return@sub null
    //Any!
    return@sub object : HtmlSubConverterNullHandling<Exception> {

        override fun renderNonNull(info: ItemConversionInfo<Exception>, to: Appendable) {
            to.append("<h1>${info.data?.javaClass?.simpleName}</h1>")
            to.append("<p>")
            to.append(info.data?.message)
            to.append("</p>")
            if (ServerSettings.debugMode) {
                to.append("<code>")
                to.append(info.data?.stackTraceString())
                to.append("</code>")
            }
        }

        override fun renderFormNonNull(info: ItemConversionInfo<Exception>, to: Appendable) {
        }

        override fun parseNonNull(info: ItemConversionInfo<Exception>): Exception? {
            return null
        }

        override fun renderParametersNonNull(info: ItemConversionInfo<Exception>, to: MutableMap<String, String>) {
        }

    }
}