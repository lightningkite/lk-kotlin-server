package com.lightningkite.kotlin.server.types

/*
POINTER OPTIONS

Contains access object
String with annotation
Pointer with annotation
Pointer with type

*/
data class Pointer<TYPE, KEY>(
        var key: KEY = null as KEY,
        var value: TYPE? = null
)


