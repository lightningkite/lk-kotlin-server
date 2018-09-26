package lk.kotlin.types.csv

import kotlin.reflect.KClass


typealias CsvSubConverterGenerator = (KClass<*>)-> CsvSubConverter?