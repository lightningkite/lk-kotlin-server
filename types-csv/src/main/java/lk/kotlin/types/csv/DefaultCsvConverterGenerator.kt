package lk.kotlin.types.csv

import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.fastMutableProperties
import lk.kotlin.reflect.fastType
import lk.kotlin.reflect.getUntyped
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createInstance

class DefaultCsvConverterGenerator(val csvConverter: CsvConverter): CsvSubConverterGenerator {
    data class SubInfo(
            val name:String,
            val type:TypeInformation,
            val property: KMutableProperty1<*, *>,
            val converter: CsvSubConverter
    )
    override fun invoke(kclass: KClass<*>): CsvSubConverter? {
        val subConverters = kclass.fastMutableProperties.map {
            SubInfo(
                    name = it.key,
                    type = it.value.fastType,
                    property = it.value,
                    converter = csvConverter[it.value.fastType.kclass]
            )
        }

        return object : CsvSubConverter{
            override fun columnCount(typeInformation: TypeInformation): Int {
                return subConverters.sumBy { it.converter.columnCount(it.type) }
            }

            override fun renderHeader(name: String, typeInformation: TypeInformation, to: (String) -> Unit) {
                val getName = if(name.isBlank()) {{it:String -> it}} else {{it:String -> name + " - " + it}}
                for(sub in subConverters){
                    sub.converter.renderHeader(getName(sub.name), sub.type, to)
                }
            }

            override fun render(typeInformation: TypeInformation, data: Any?, to: (String) -> Unit) {
                if(data == null){
                    repeat(columnCount(typeInformation)){ to.invoke("") }
                } else {
                    for(sub in subConverters){
                        sub.converter.render(sub.type, sub.property.getUntyped(data), to)
                    }
                }
            }

            override fun parse(typeInformation: TypeInformation, csv: () -> String): Any? {
                val getter:()->String = if(typeInformation.nullable){
                    val cols = (1 .. columnCount(typeInformation)).map { csv.invoke() }
                    if(cols.all { it.isBlank() }) {
                        return null
                    }
                    var current = 0
                    { cols[current++] }
                } else {
                    csv
                }
                val instance = typeInformation.kclass.createInstance()
                for(sub in subConverters){
                    sub.converter.parse(sub.type, getter)
                }
                return instance
            }

        }
    }
}
