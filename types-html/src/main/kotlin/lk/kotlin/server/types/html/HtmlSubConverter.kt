package lk.kotlin.server.types.html

interface HtmlSubConverter<T> {
    fun render(info: ItemConversionInfo<T>, to: Appendable)
    fun renderForm(info: ItemConversionInfo<T>, to: Appendable)
    fun parse(info: ItemConversionInfo<T>): T?
    fun renderParameters(info: ItemConversionInfo<T>, to: MutableMap<String, String>)
}