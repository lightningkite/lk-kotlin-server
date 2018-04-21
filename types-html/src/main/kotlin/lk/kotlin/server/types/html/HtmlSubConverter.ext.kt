package lk.kotlin.server.types.html



fun <T> HtmlSubConverter<T>.renderSafe(info: ItemConversionInfo<T>, to: Appendable) = try {
    render(info, to)
} catch (e: Exception) {
    e.printStackTrace()
}

fun <T> HtmlSubConverter<T>.renderFormSafe(info: ItemConversionInfo<T>, to: Appendable) = try {
    renderForm(info, to)
} catch (e: Exception) {
    e.printStackTrace()
}

fun <T> HtmlSubConverter<T>.parseSafe(info: ItemConversionInfo<T>): T? = try {
    parse(info)
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun <T> HtmlSubConverter<T>.renderParametersSafe(info: ItemConversionInfo<T>, to: MutableMap<String, String>) = try {
    renderParameters(info, to)
} catch (e: Exception) {
    e.printStackTrace()
}