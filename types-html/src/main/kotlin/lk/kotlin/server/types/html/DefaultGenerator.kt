package lk.kotlin.server.types.html

import lk.kotlin.reflect.annotations.friendlyName
import lk.kotlin.reflect.annotations.hidden
import lk.kotlin.reflect.fastMutableProperties
import lk.kotlin.reflect.fastType
import lk.kotlin.reflect.setUntyped
import lk.kotlin.server.types.common.ServerFunction
import lk.kotlin.server.types.urlName
import java.util.HashMap
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createInstance

fun DefaultGenerator(htmlConverter: HtmlConverter):HtmlSubConverterGenerator = sub@{ kclass ->
    //Any!
    return@sub object : HtmlSubConverterNullHandling<Any> {

        val subPropertiesHidden = kclass.fastMutableProperties.values.filter { it.hidden }.map {
            HtmlConverter.SubField(
                    property = it as KMutableProperty1<Any, Any?>,
                    subConverter = htmlConverter.retrieveAny(it.fastType.kclass)
            )
        }.toTypedArray()
        val subProperties = kclass.fastMutableProperties.values.filter { !it.hidden }.map {
            HtmlConverter.SubField(
                    property = it as KMutableProperty1<Any, Any?>,
                    subConverter = htmlConverter.retrieveAny(it.fastType.kclass)
            )
        }.toTypedArray()

        override fun renderNonNull(info: ItemConversionInfo<Any>, to: Appendable) {
            if (info.data is ServerFunction<*>) {
                to.append("""<form method="post" action="#">""")
                renderForm(info, to)
                to.append("""<input class="submit" type="submit" value="Submit"/>""")
                to.append("""</form>""")
                return
            }
            if (info.data == null) {
                to.append("<p>null</p>")
            } else {
                to.append("""<div class="object ${info.type.kclass.urlName()}">""")
                to.append("""<p class="object-type">${info.type.kclass.friendlyName}</p>""")
                to.append("<dl>")
                for (prop in subProperties) {
                    val properName = prop.property.friendlyName
                    to.append("<dt>$properName</dt>")
                    to.append("<dd>")
                    val subInfo = ItemConversionInfo(
                            context = info.context,
                            depth = info.depth + 1,
                            callDepth = info.callDepth,
                            type = prop.type,
                            property = prop.property,
                            name = info.name + "." + prop.name,
                            data = prop.property.get(info.data!!)
                    )
                    prop.subConverter.renderSafe(subInfo, to)
                    to.append("</dd>")
                }
                to.append("</dl>")
                to.append("</div>")
            }
        }

        override fun renderFormNonNull(info: ItemConversionInfo<Any>, to: Appendable) {
            if (info.data == null) {
                to.append("<p>null</p>")
            } else {
                to.append("""<div class="object ${info.type.kclass.urlName()}">""")
                to.append("""<p class="object-type">${info.type.kclass.friendlyName}</p>""")
                for (prop in subPropertiesHidden) {
                    val subInfo = ItemConversionInfo(
                            context = info.context,
                            depth = info.depth + 1,
                            callDepth = info.callDepth,
                            type = prop.type,
                            property = prop.property,
                            name = info.name + "." + prop.name,
                            data = prop.property.get(info.data!!)
                    )
                    val toMap = HashMap<String, String>()
                    prop.subConverter.renderParametersSafe(subInfo, toMap)
                    for ((key, value) in toMap) {
                        to.append("""<input name="$key" type="hidden" value="$value"/>""")
                    }
                }
                to.append("<dl>")
                for (prop in subProperties) {
                    val properName = prop.property.friendlyName
                    to.append("<dt>$properName</dt>")
                    to.append("<dd>")
                    val subInfo = ItemConversionInfo(
                            context = info.context,
                            depth = info.depth + 1,
                            callDepth = info.callDepth,
                            type = prop.type,
                            property = prop.property,
                            name = info.name + "." + prop.name,
                            data = prop.property.get(info.data!!)
                    )
                    prop.subConverter.renderFormSafe(subInfo, to)
                    to.append("</dd>")
                }
                to.append("</dl>")
                to.append("</div>")
            }
        }

        override fun parseNonNull(info: ItemConversionInfo<Any>): Any? {
            val instance = info.type.kclass.createInstance()
            for (prop in subProperties) {
                try {
                    val subInfo = ItemConversionInfo(
                            context = info.context,
                            depth = info.depth + 1,
                            callDepth = info.callDepth,
                            type = prop.type,
                            property = prop.property,
                            name = info.name + "." + prop.name,
                            data = null
                    )
                    val value = prop.subConverter.parseSafe(subInfo)
                    if (value != null || prop.type.nullable)
                        prop.property.setUntyped(instance, value)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            for (prop in subPropertiesHidden) {
                try {
                    val subInfo = ItemConversionInfo(
                            context = info.context,
                            depth = info.depth + 1,
                            callDepth = info.callDepth,
                            type = prop.type,
                            property = prop.property,
                            name = info.name + "." + prop.name,
                            data = null
                    )
                    val value = prop.subConverter.parseSafe(subInfo)
                    if (value != null || prop.type.nullable)
                        prop.property.setUntyped(instance, value)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return instance
        }

        override fun renderParametersNonNull(info: ItemConversionInfo<Any>, to: MutableMap<String, String>) {
            if (info.data == null) return
            for (prop in subProperties) {
                val value = prop.property.get(info.data)
                val subInfo = ItemConversionInfo(
                        context = info.context,
                        depth = info.depth + 1,
                        callDepth = info.callDepth,
                        type = prop.type,
                        property = prop.property,
                        name = info.name + "." + prop.name,
                        data = value
                )
                prop.subConverter.renderParametersSafe(subInfo, to)
            }
            for (prop in subPropertiesHidden) {
                val value = prop.property.get(info.data)
                val subInfo = ItemConversionInfo(
                        context = info.context,
                        depth = info.depth + 1,
                        callDepth = info.callDepth,
                        type = prop.type,
                        property = prop.property,
                        name = info.name + "." + prop.name,
                        data = value
                )
                prop.subConverter.renderParametersSafe(subInfo, to)
            }
        }

    }
}