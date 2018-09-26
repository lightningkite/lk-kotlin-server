package lk.kotlin.types.csv

import lk.kotlin.reflect.TypeInformation

interface CsvSubConverter{
    fun columnCount(typeInformation: TypeInformation):Int
    fun renderHeader(name:String, typeInformation: TypeInformation, to:(String)->Unit)
    fun render(typeInformation: TypeInformation, data:Any?, to:(String)->Unit)
    fun parse(typeInformation: TypeInformation, csv:()->String):Any?
}