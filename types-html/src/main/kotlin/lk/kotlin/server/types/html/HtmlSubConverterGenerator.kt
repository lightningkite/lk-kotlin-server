package lk.kotlin.server.types.html

import kotlin.reflect.KClass


typealias HtmlSubConverterGenerator = (KClass<*>) -> HtmlSubConverter<*>?