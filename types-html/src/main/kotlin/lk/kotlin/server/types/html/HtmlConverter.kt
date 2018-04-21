package lk.kotlin.server.types.html

import lk.kotlin.reflect.*
import lk.kotlin.server.base.*
import lk.kotlin.server.types.*
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.URLEncoder
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1


class HtmlConverter : Parser, Renderer {

    fun setup() {
        CentralContentTypeMap.parsers[ContentType.Application.FormUrlEncoded.parameterless()] = this
        CentralContentTypeMap.parsers[ContentType.Multipart.parameterless()] = this
        CentralContentTypeMap.renderers[ContentType.Text.Html.parameterless()] = this
    }

    var pageWrapperPrepend = """<!DOCTYPE html><html><head><meta charset="utf-8"/><link rel="stylesheet" href="/style.css"/></head><body>"""
    var pageWrapperAppend = """</body></html>"""

    private val subGenerators = ArrayList<(KClass<*>) -> HtmlSubConverter<*>?>()
    private val subs = HashMap<KClass<*>, HtmlSubConverter<*>>()

    /**
     * Register a [subConverter] to be used for converting objects of type [kclass].
     */
    fun <T : Any> register(kclass: KClass<T>, subConverter: HtmlSubConverter<T>) {
        subs[kclass] = subConverter
    }

    @Suppress("UNCHECKED_CAST")
            /**
     * Retrieve the [HtmlSubConverter] used for converting objects to/from HTML for type [kclass].
     */
    fun <T : Any> retrieve(kclass: KClass<T>): HtmlSubConverter<T> = subs.getOrPut(kclass) {
        subGenerators.asSequence().mapNotNull { it.invoke(kclass) }.firstOrNull()
                ?: throw IllegalArgumentException("Sub for ${kclass.qualifiedName} not found!")
    } as HtmlSubConverter<T>


    @Suppress("UNCHECKED_CAST")
            /**
     * Retrieve the [HtmlSubConverter] used for converting objects to/from HTML for type [kclass].
     * The returned item is casted to work on any object.
     */
    fun retrieveAny(kclass: KClass<*>): HtmlSubConverter<Any> = retrieve(kclass) as HtmlSubConverter<Any>

    /**
     * The maximum number of retrieval calls the converter can make depth-wise.
     */
    var maxCallDepth = 1

    /**
     * Parses the request as form/query parameters.
     */
    override fun <T> parse(type: TypeInformation, httpRequest: HttpRequest, getTransaction: () -> Transaction): T {
        val context = ConversionContext(this, httpRequest, getTransaction)
        val info = ItemConversionInfo(context, 0, 0, type, null, "value", null)
        @Suppress("UNCHECKED_CAST")
        return retrieve(type.kclass).parseSafe(info) as T
    }

    /**
     * Renders the given [data] as HTML.
     */
    override fun <T> render(type: TypeInformation, data: T, httpRequest: HttpRequest, getTransaction: () -> Transaction, out: OutputStream) {
        @Suppress("UNCHECKED_CAST")
        val renderer = retrieve(type.kclass) as HtmlSubConverter<Any?>
        val context = ConversionContext(this, httpRequest, getTransaction)
        val info = ItemConversionInfo(context, 0, 0, type, null, "value", data)
        OutputStreamWriter(out).use {
            it.append(pageWrapperPrepend)
            renderer.renderSafe(info, it)
            it.append(pageWrapperAppend)
        }
    }

    //Some defaults

    var defaultGenerator: HtmlSubConverterGenerator = DefaultGenerator(this)
    init {
        register(Byte::class, numberConverter { it.toByteOrNull() })
        register(Short::class, numberConverter { it.toShortOrNull() })
        register(Int::class, numberConverter { it.toIntOrNull() })
        register(Long::class, numberConverter { it.toLongOrNull() })
        register(Float::class, numberConverter { it.toFloatOrNull() })
        register(Double::class, numberConverter { it.toDoubleOrNull() })

        register(Boolean::class, BooleanConverter())
        register(String::class, StringConverter())
        register(Date::class, DateConverter())
        register(List::class, ListConverter())
        register(Map::class, MapConverter())

        subGenerators += EnumConversionGenerator
        subGenerators += ListSubtypeConverterGenerator
        subGenerators += PointerConverterGenerator(this)
        subGenerators += ExceptionConversionGenerator
        subGenerators += defaultGenerator
    }

    data class SubField(
            val property: KMutableProperty1<Any, Any?>,
            val subConverter: HtmlSubConverter<Any>
    ) {
        val name = property.name
        val type = property.fastType
    }

    companion object {

        /**
         * Converts the given parameters to a query string, like ?name=value&second=other
         */
        fun paramsToQueryString(params: Map<String, String>): String {
            return params.entries.joinToString("&", "?") {
                URLEncoder.encode(it.key, Charsets.UTF_8.name()) +
                        "=" +
                        URLEncoder.encode(it.value, Charsets.UTF_8.name())
            }
        }

        /**
         * Outputs javascript to set a cookie.
         */
        fun setCookie(to: Appendable, name: String, value: String) {
            to.append("""<script>document.cookie = "$name=$value;path=/"</script>""")
        }
    }
}

