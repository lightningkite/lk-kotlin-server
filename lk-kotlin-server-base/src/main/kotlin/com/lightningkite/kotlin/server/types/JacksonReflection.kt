package com.lightningkite.kotlin.server.types

import com.fasterxml.jackson.databind.JavaType
import lk.kotlin.jackson.MyJackson
import lk.kotlin.reflect.TypeInformation


fun TypeInformation.toJavaType(): JavaType {
    return if (this.typeParameters.isNotEmpty()) {
        MyJackson.mapper.typeFactory.constructSimpleType(this.kclass.java, this.typeParameters.map { it.toJavaType() }.toTypedArray())
    } else {
        MyJackson.mapper.typeFactory.constructType(this.kclass.java)
    }
}