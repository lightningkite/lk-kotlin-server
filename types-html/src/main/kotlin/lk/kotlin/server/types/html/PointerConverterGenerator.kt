package lk.kotlin.server.types.html

import lk.kotlin.jackson.MyJackson
import lk.kotlin.jackson.jacksonToString
import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.annotations.friendlyName
import lk.kotlin.reflect.fastAllSuperclasses
import lk.kotlin.reflect.fastAllSupertypes
import lk.kotlin.server.base.parameter
import lk.kotlin.server.base.use
import lk.kotlin.server.types.common.HasId
import lk.kotlin.server.types.common.PointerServerFunction
import lk.kotlin.server.types.common.annotations.query
import lk.kotlin.server.types.invoke
import lk.kotlin.server.types.toJavaType
import lk.kotlin.server.types.urlName
import java.util.HashMap
import kotlin.reflect.full.createInstance
import kotlin.reflect.jvm.jvmErasure

fun PointerConverterGenerator(htmlConverter: HtmlConverter): HtmlSubConverterGenerator = { pointerServerFunctionKClass ->
    if (!pointerServerFunctionKClass.fastAllSuperclasses.contains(PointerServerFunction::class)) null
    else object : HtmlSubConverterNullHandling<PointerServerFunction<*, *>> {

        val psfk = pointerServerFunctionKClass.fastAllSupertypes
                .find { it.jvmErasure == PointerServerFunction::class }!!
        val pointedType = TypeInformation.fromKotlin(psfk.arguments[0].type!!)
        val keyType = TypeInformation.fromKotlin(psfk.arguments[1].type!!)
        val pointedSub = htmlConverter.retrieveAny(pointedType.kclass)
        val keySub = htmlConverter.retrieveAny(keyType.kclass)

        override fun renderNonNull(info: ItemConversionInfo<PointerServerFunction<*, *>>, to: Appendable) {
            if (info.data == null)
                to.append("No Pointer")
            else {
                val max = info.context.htmlConverter.maxCallDepth
                val depth = info.callDepth
                when {
                    depth < max -> {
                        val value = info.context.getTransaction.invoke().use {
                            info.data!!.invoke(it)
                        }
                        val sub = ItemConversionInfo(
                                context = info.context,
                                depth = info.depth + 1,
                                callDepth = info.callDepth + 1,
                                type = pointedType,
                                property = info.property,
                                name = "${info.name}.value",
                                data = value
                        )
                        pointedSub.renderSafe(sub, to)
                    }
                    depth == max -> {
                        //Todo: Convert to link
                        val linkTo = info.data!!
                        val sub = ItemConversionInfo(
                                context = info.context,
                                depth = info.depth + 1,
                                callDepth = info.callDepth,
                                type = info.type,
                                property = info.property,
                                name = "value",
                                data = linkTo
                        )
                        val params = HashMap<String, String>()
                        info.context.htmlConverter.retrieveAny(linkTo.javaClass.kotlin)
                                .renderParametersSafe(sub, params)
                        val paramString = HtmlConverter.paramsToQueryString(params)
                        to.append("""<a href="${linkTo.javaClass.kotlin.urlName() + paramString}">""")
                        to.append(pointedType.kclass.friendlyName)
                        to.append(" (")
                        to.append(info.data.id?.toString())
                        to.append(")")
                        to.append("</a>")
                    }
                    else -> {
                        val linkTo = info.data!!
                        val sub = ItemConversionInfo(
                                context = info.context,
                                depth = info.depth + 1,
                                callDepth = info.callDepth,
                                type = info.type.typeParameters[1],
                                property = info.property,
                                name = "${info.name}.key",
                                data = linkTo
                        )
                        to.append("Key: ")
                        keySub.renderSafe(sub, to)
                    }
                }
            }

        }

        override fun renderFormNonNull(info: ItemConversionInfo<PointerServerFunction<*, *>>, to: Appendable) {
            val options = info.context.getTransaction.invoke().use {
                pointedType.kclass.query()?.invoke(it)
            } ?: listOf()
            to.append("<select name=\"${info.name}\">")
            for (item in options) {
                val key = item.id
                val keyString = keyToString(key)
                to.append("<option ")
                if (key == info.data?.id) {
                    to.append("selected=\"selected\" ")
                }
                to.append("value=\"$keyString\">")
                to.append(item.toString())
                to.append("</option>")
            }
            to.append("</select>")
        }

        override fun parseNonNull(info: ItemConversionInfo<PointerServerFunction<*, *>>): PointerServerFunction<*, *>? {
            val stringValue = info.context.httpRequest.parameter(info.name)
            @Suppress("IMPLICIT_CAST_TO_ANY")
            val key = when (keyType.kclass) {
                Byte::class -> stringValue?.toByteOrNull()
                Short::class -> stringValue?.toShortOrNull()
                Int::class -> stringValue?.toIntOrNull()
                Long::class -> stringValue?.toLongOrNull()
                Float::class -> stringValue?.toFloatOrNull()
                Double::class -> stringValue?.toDoubleOrNull()
                String::class -> stringValue
                else -> try {
                    MyJackson.mapper.readerFor(info.type.typeParameters[1].toJavaType()).readValue<Any?>(stringValue)
                } catch (e: Exception) {
                    null
                }
            } ?: return null
            return pointerServerFunctionKClass.createInstance()
                    .let {
                        @Suppress("UNCHECKED_CAST")
                        it as PointerServerFunction<HasId<Any>, Any>
                    }
                    .apply { this.id = key }
        }

        fun keyToString(key: Any?): String? {
            return when (key) {
                null -> null
                is Byte,
                is Short,
                is Int,
                is Long,
                is Float,
                is Double,
                is String -> key.toString()
                else -> key.jacksonToString()
            }
        }

        override fun renderParametersNonNull(info: ItemConversionInfo<PointerServerFunction<*, *>>, to: MutableMap<String, String>) {
            keyToString(info.data?.id)?.let { to[info.name] = it }
        }
    }
}