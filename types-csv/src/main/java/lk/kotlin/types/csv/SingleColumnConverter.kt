package lk.kotlin.types.csv

import lk.kotlin.reflect.TypeInformation

class SingleColumnConverter(val renderToString:(Any)->String = {it.toString()}, val parseFromString:(String)->Any?) : CsvSubConverterTyped<Any?> {

    override fun columnCount(typeInformation: TypeInformation): Int = 1

    override fun renderHeader(name: String, typeInformation: TypeInformation, to: (String) -> Unit) {
        to.invoke(name)
    }

    override fun renderTyped(typeInformation: TypeInformation, data: Any?, to: (String) -> Unit) {
        to.invoke(data?.let(renderToString) ?: "")
    }

    override fun parse(typeInformation: TypeInformation, csv: () -> String): Any? {
        val value = csv.invoke()
        if(typeInformation.nullable && value.isBlank()) return null
        else return value.let(parseFromString)
    }
}