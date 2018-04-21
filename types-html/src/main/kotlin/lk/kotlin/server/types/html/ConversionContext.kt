package lk.kotlin.server.types.html

import lk.kotlin.server.base.HttpRequest
import lk.kotlin.server.base.Transaction

data class ConversionContext(
        val htmlConverter: HtmlConverter,
        val httpRequest: HttpRequest,
        val getTransaction: () -> Transaction
)