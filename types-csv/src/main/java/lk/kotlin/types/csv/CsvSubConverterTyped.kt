package lk.kotlin.types.csv

import lk.kotlin.reflect.TypeInformation

interface CsvSubConverterTyped<T> : CsvSubConverter {
    override fun columnCount(typeInformation: TypeInformation): Int
    override fun renderHeader(name:String, typeInformation: TypeInformation, to:(String)->Unit)
    override fun render(typeInformation: TypeInformation, data:Any?, to:(String)->Unit)
        = renderTyped(typeInformation, data as T, to)
    fun renderTyped(typeInformation: TypeInformation, data:T, to:(String)->Unit)
    override fun parse(typeInformation: TypeInformation, csv:()->String):T
}